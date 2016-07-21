package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * Created by azwickey on 7/21/16.
 */
@Configuration
@Profile("gemfire")
public class GemfireTokenStoreConfig {

    @Bean
    public TokenStore tokenStore() {
        return new GemfireTokenStore();
    }
}
