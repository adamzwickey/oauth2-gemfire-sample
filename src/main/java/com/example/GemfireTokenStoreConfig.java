package com.example;

import com.gemstone.gemfire.cache.DataPolicy;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;

/**
 * Created by azwickey on 7/21/16.
 */
@Configuration
public class GemfireTokenStoreConfig {

    @Autowired
    private ClientCacheFactoryBean clientCache;

    @Bean
    public GemfireTokenStore tokenStore() {
        try {
            return new GemfireTokenStore(new GemfireTemplate(clientRegionFactoryBean().getObject()));
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @Bean(name = "tokenRegion")
    @SuppressWarnings({"rawtype", "unchecked"})
    public ClientRegionFactoryBean clientRegionFactoryBean() {
        ClientRegionFactoryBean clientRegionFactoryBean = new ClientRegionFactoryBean();
        clientRegionFactoryBean.setRegionName("tokens");
        clientRegionFactoryBean.setDataPolicy(DataPolicy.EMPTY);

        try {
            clientRegionFactoryBean.setCache(clientCache.getObject());
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
        return clientRegionFactoryBean;
    }
}
