package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.PoolFactoryBean;
import org.springframework.data.gemfire.support.ConnectionEndpointList;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by azwickey on 7/22/16.
 */
@Configuration
@EnableConfigurationProperties(GemfireConfigProperties.class)
@Import(InetSocketAddressConverterConfiguration.class)
public class GemfireConfig {

    Logger LOG = LoggerFactory.getLogger(getClass());

    private static InetSocketAddress[] hostAddresses = { new InetSocketAddress("localhost", 10334) };

    @Autowired
    private GemfireConfigProperties gemfireConfigProperties;

    @Bean
    public PoolFactoryBean gemfirePool() {
        PoolFactoryBean poolFactoryBean = new PoolFactoryBean();
        ConnectionEndpointList endpointList = ConnectionEndpointList.from(Arrays.asList(gemfireConfigProperties.getHostAddresses()));
        poolFactoryBean.setLocatorEndpointList(endpointList);
        poolFactoryBean.setName("gemfirePool");
        poolFactoryBean.setSubscriptionEnabled(true);
        poolFactoryBean.setMaxConnections(100);
        poolFactoryBean.setPingInterval(TimeUnit.SECONDS.toMillis(15));
        poolFactoryBean.setRetryAttempts(1);
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
}
