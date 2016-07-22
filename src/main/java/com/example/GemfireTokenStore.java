package com.example;

import com.gemstone.gemfire.cache.Region;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.*;

/**
 * Created by azwickey on 7/21/16.
 */
public class GemfireTokenStore implements TokenStore {

    private static final String ACCESS = "access:";
    private static final String AUTH_TO_ACCESS = "auth_to_access:";
    private static final String AUTH = "auth:";
    private static final String REFRESH_AUTH = "refresh_auth:";
    private static final String ACCESS_TO_REFRESH = "access_to_refresh:";
    private static final String REFRESH = "refresh:";
    private static final String REFRESH_TO_ACCESS = "refresh_to_access:";
    private static final String CLIENT_ID_TO_ACCESS = "client_id_to_access:";
    private static final String UNAME_TO_ACCESS = "uname_to_access:";

    private GemfireTemplate gemfireTemplate;
    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    public GemfireTokenStore(GemfireTemplate gemfireTemplate) {
        this.gemfireTemplate = gemfireTemplate;
    }

    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        OAuth2Authentication auth = gemfireTemplate.get(AUTH + token);
        return auth;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        String accessKey = ACCESS + token.getValue();
        String authKey = AUTH + token.getValue();
        String authToAccessKey = AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(authentication);
        String approvalKey = UNAME_TO_ACCESS + getApprovalKey(authentication);
        String clientId = CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId();



        gemfireTemplate.put(accessKey, token);
        gemfireTemplate.put(authKey, authentication);
        gemfireTemplate.put(authToAccessKey, token);
        if(!authentication.isClientOnly()) {
                gemfireTemplate.put(approvalKey, token);
        }

        if(gemfireTemplate.containsKey(clientId)) {
            List tokenList = gemfireTemplate.get(clientId);
            tokenList.add(token);
            gemfireTemplate.replace(clientId, tokenList);
        } else {
            gemfireTemplate.put(clientId, Arrays.asList(token));
        }

        if(token.getExpiration() != null) {
            //TODO...
            int seconds = token.getExpiresIn();
//            conn.expire(accessKey, seconds);
//            conn.expire(authKey, seconds);
//            conn.expire(authToAccessKey, seconds);
//            conn.expire(clientId, seconds);
//            conn.expire(approvalKey, seconds);
        }

        OAuth2RefreshToken refreshToken = token.getRefreshToken();
        if (refreshToken != null && refreshToken.getValue() != null) {
            //byte[] refresh = serialize(token.getRefreshToken().getValue());
            //byte[] auth = serialize(token.getValue());
            String refreshToAccessKey = REFRESH_TO_ACCESS + token.getRefreshToken().getValue();
            String accessToRefreshKey = ACCESS_TO_REFRESH + token.getValue();
            gemfireTemplate.put(refreshToAccessKey, token.getValue());
            gemfireTemplate.put(accessToRefreshKey, token.getRefreshToken().getValue());

            if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
                Date expiration = expiringRefreshToken.getExpiration();
                if (expiration != null) {
                    int seconds = Long.valueOf((expiration.getTime() - System.currentTimeMillis()) / 1000L).intValue();
                    //TODO...
//                    conn.expire(refreshToAccessKey, seconds);
//                    conn.expire(accessToRefreshKey, seconds);
                }
            }
        }
    }

    private static String getApprovalKey(OAuth2Authentication authentication) {
        String userName = authentication.getUserAuthentication() == null ? "" : authentication.getUserAuthentication().getName();
        return getApprovalKey(authentication.getOAuth2Request().getClientId(), userName);
    }

    private static String getApprovalKey(String clientId, String userName) {
        return clientId + (userName == null ? "" : ":" + userName);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        String key = ACCESS + tokenValue;
        OAuth2AccessToken accessToken = gemfireTemplate.get(key);
        return accessToken;
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        removeAccessToken(token.getValue());
    }

    public void removeAccessToken(String token) {
        String accessKey = ACCESS + token;
        String authKey = AUTH + token;
        String accessToRefreshKey = ACCESS_TO_REFRESH + token;

        OAuth2AccessToken access = gemfireTemplate.remove(accessKey);
        OAuth2Authentication authentication = gemfireTemplate.remove(authKey);
        gemfireTemplate.remove(accessToRefreshKey);


        //byte[] access = (byte[]) results.get(0);
        if (authentication != null) {
            String key = authenticationKeyGenerator.extractKey(authentication);
            String authToAccessKey = AUTH_TO_ACCESS + key;
            String unameKey = UNAME_TO_ACCESS + getApprovalKey(authentication);
            String clientId = CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId();
            gemfireTemplate.remove(authToAccessKey);

            gemfireTemplate.remove(unameKey);
            List<OAuth2AccessToken> tokens = gemfireTemplate.get(clientId);
            tokens.removeIf((OAuth2AccessToken t) -> {
                return t.equals(access);
            });
            gemfireTemplate.replace(clientId,tokens);
            gemfireTemplate.remove(ACCESS + key);
        }
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        String refreshKey = REFRESH + refreshToken.getValue();
        String refreshAuthKey = REFRESH_AUTH + refreshToken.getValue();

        gemfireTemplate.put(refreshKey, refreshToken);
        gemfireTemplate.put(refreshAuthKey, authentication);
        if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
            ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
            Date expiration = expiringRefreshToken.getExpiration();
            if (expiration != null) {
                //TODO...
                int seconds = Long.valueOf((expiration.getTime() - System.currentTimeMillis()) / 1000L).intValue();
//                conn.expire(refreshKey, seconds);
//                conn.expire(refreshAuthKey, seconds);
            }
        }
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        OAuth2RefreshToken refreshToken = gemfireTemplate.get(REFRESH + tokenValue);
        return refreshToken;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthenticationForRefreshToken(token.getValue());
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(String token) {
        OAuth2Authentication auth = gemfireTemplate.get(REFRESH_AUTH + token);
        return auth;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        String refreshKey = REFRESH + token.getValue();
        String refreshAuthKey = REFRESH_AUTH + token.getValue();
        String refresh2AccessKey = REFRESH_TO_ACCESS + token.getValue();
        String access2RefreshKey = ACCESS_TO_REFRESH + token.getValue();
        gemfireTemplate.remove(refreshKey);
        gemfireTemplate.remove(refreshAuthKey);
        gemfireTemplate.remove(refresh2AccessKey);
        gemfireTemplate.remove(access2RefreshKey);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        String key = REFRESH_TO_ACCESS + refreshToken;
        String token = gemfireTemplate.remove(key);

        if (token == null) {
            return;
        }

        removeAccessToken(token);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        String key = authenticationKeyGenerator.extractKey(authentication);
        String combinedKey = AUTH_TO_ACCESS + key;
        OAuth2AccessToken accessToken = gemfireTemplate.get(combinedKey);
        if (accessToken != null
                && !key.equals(authenticationKeyGenerator.extractKey(readAuthentication(accessToken.getValue())))) {
            // Keep the stores consistent (maybe the same user is
            // represented by this authentication but the details have
            // changed)
            storeAccessToken(accessToken, authentication);
        }
        return accessToken;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        String approvalKey = UNAME_TO_ACCESS + getApprovalKey(clientId, userName);
        if(gemfireTemplate.containsKey(approvalKey)) {
            return gemfireTemplate.get(approvalKey);
        } else {
            return Collections.<OAuth2AccessToken>emptySet();
        }
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        String key = CLIENT_ID_TO_ACCESS + clientId;
        if(gemfireTemplate.containsKey(key)) {
            return gemfireTemplate.get(key);
        } else {
            return Collections.<OAuth2AccessToken>emptySet();
        }
    }
}
