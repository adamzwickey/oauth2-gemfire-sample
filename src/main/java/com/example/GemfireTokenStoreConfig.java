package com.example;

import com.gemstone.gemfire.cache.DataPolicy;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.*;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.support.ConnectionEndpointList;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Created by azwickey on 7/21/16.
 */
@Configuration
@Profile("gemfire")
public class GemfireTokenStoreConfig {

    private InetSocketAddress[] hostAddresses = { new InetSocketAddress("localhost", 10334) };

    @Bean
    public GemfireTokenStore tokenStore() {
        try {
            return new GemfireTokenStore(new GemfireTemplate(clientRegionFactoryBean().getObject()));
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean
    public PoolFactoryBean gemfirePool() {
        PoolFactoryBean poolFactoryBean = new PoolFactoryBean();
        ConnectionEndpointList endpointList = ConnectionEndpointList.from(Arrays.asList(hostAddresses));
        poolFactoryBean.setLocatorEndpointList(endpointList);
//        switch (config.getConnectType()) {
//            case locator:
//                poolFactoryBean.setLocatorEndpointList(endpointList);
//                break;
//            case server:
//                poolFactoryBean.setServerEndpointList(endpointList);
//                break;
//            default:
//                throw new IllegalArgumentException("connectType " + config.getConnectType() + " is not supported.");
//        }
//        poolFactoryBean.setSubscriptionEnabled(config.isSubscriptionEnabled());
        poolFactoryBean.setName("gemfirePool");
        return poolFactoryBean;
    }

    @Bean
    public ClientCacheFactoryBean clientCache() {
        ClientCacheFactoryBean clientCacheFactoryBean = new ClientCacheFactoryBean();
        clientCacheFactoryBean.setUseBeanFactoryLocator(false);
        clientCacheFactoryBean.setPoolName("gemfirePool");
        clientCacheFactoryBean.setReadyForEvents(true);
        return clientCacheFactoryBean;
    }

    @Bean
    @SuppressWarnings({"rawtype", "unchecked"})
    public ClientRegionFactoryBean clientRegionFactoryBean() {
        ClientRegionFactoryBean clientRegionFactoryBean = new ClientRegionFactoryBean();
        clientRegionFactoryBean.setRegionName("tokens");
        clientRegionFactoryBean.setDataPolicy(DataPolicy.EMPTY);

        try {
            clientRegionFactoryBean.setCache(clientCache().getObject());
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
        return clientRegionFactoryBean;
    }
}
