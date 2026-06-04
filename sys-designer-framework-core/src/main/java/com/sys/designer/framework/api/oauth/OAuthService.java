package com.sys.designer.framework.api.oauth;

public interface OAuthService {
    void authorize(String accountId);

    default AccessTokenInfo authorizeCallback(String accountId, String code) {
        return authorizeCallback(accountId, code, true);
    }

    AccessTokenInfo authorizeCallback(String accountId, String code, boolean cache);

    String getAccessToken(String accountId);

    AccessTokenInfo refreshAccessToken(String accountId);

    OAuthUserInfo getUserInfo(String accountId);
}
