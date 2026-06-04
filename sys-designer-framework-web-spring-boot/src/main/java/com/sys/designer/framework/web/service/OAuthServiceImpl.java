package com.sys.designer.framework.web.service;

import com.sys.designer.framework.api.ApiClient;
import com.sys.designer.framework.api.ClientResult;
import com.sys.designer.framework.api.account.Account;
import com.sys.designer.framework.api.account.AccountService;
import com.sys.designer.framework.api.cache.CacheKey;
import com.sys.designer.framework.api.cache.CacheService;
import com.sys.designer.framework.api.oauth.AccessTokenInfo;
import com.sys.designer.framework.api.oauth.OAuthService;
import com.sys.designer.framework.api.oauth.OAuthUserInfo;
import com.sys.designer.framework.api.oauth.OauthorizeAccount;
import com.sys.designer.framework.api.session.TokenInfo;
import com.sys.designer.framework.common.cache.KeyParam;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.exception.ErrorCodeRuntimeException;
import com.sys.designer.framework.common.util.ComponentUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import com.sys.designer.framework.web.util.ApiUtil;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Service
public class OAuthServiceImpl implements OAuthService {
    @Resource
    private ApiClient apiClient;

    @Resource
    private CacheService cacheService;

    private OauthorizeAccount getAccount(String id) {
        return new OauthorizeAccount(ComponentUtil.getBean(AccountService.class).getAccount(id).getConfig());
    }

    @Override
    public void authorize(String accountId) {
        OauthorizeAccount account = getAccount(accountId);
        String url = account.getUrl() + "?client_id=" +
                account.getClientId() + "&redirect_uri=" +
                URLEncoder.encode(account.getRedirectUrl()) +
                "&response_type=" + account.getResponseType();
        if (ValueUtil.isNotEmpty(account.getState())) {
            url += "&state=" + account.getState();
        }
        if (ValueUtil.isNotEmpty(account.getScope())) {
            url += "&scope=" + account.getScope();
        }
        try {
            ApiUtil.getResponse().sendRedirect(url);
        } catch (IOException e) {
            throw new BusinessRuntimeException(CommonErrorCode.API_REQUEST_FAILED);
        }
    }

    @Override
    public AccessTokenInfo authorizeCallback(String accountId, String code, boolean cache) {
        OauthorizeAccount account = getAccount(accountId);
        String url = account.getAccessTokenUrl();

        Map<String, Object> params = new HashMap<>();
        params.put("client_id", account.getClientId());
        params.put("client_secret", account.getClientSecret());
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", account.getRedirectUrl());
        ClientResult<Map> result = apiClient.postFor(url, params, Map.class, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        if (!result.isSuccess()) {
            throw new ErrorCodeRuntimeException(result.getCode(), result.getMessage());
        }
        Map<String, Object> map = result.getResults();
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setAccessToken((String) map.get("access_token"));

        accessTokenInfo.setScope((String) map.get("scope"));
        accessTokenInfo.setExpiresIn(Integer.parseInt(map.get("expires_in") + "") * 1000);

        if (cache) {
            String key = "user:login:oauth:" + account.getId() + ":access_token";
            CacheKey cacheKey = KeyParam.of(key, false, accessTokenInfo.getExpiresIn().longValue());
            cacheService.setString(cacheKey, accessTokenInfo.getAccessToken());
        }

        accessTokenInfo.setData(map);
        accessTokenInfo.setRefreshToken((String) map.get("refresh_token"));

        if (cache) {
            String refreshTokenKey = "user:login:oauth:" + account.getId() + ":refresh_token";
            cacheService.setString(KeyParam.of(refreshTokenKey, false, accessTokenInfo.getExpiresIn().longValue() * 2), accessTokenInfo.getRefreshToken());
        }

        String homeUrl = account.getHomeUrl();
        try {
            ApiUtil.getResponse().sendRedirect(homeUrl);
        } catch (IOException e) {
            throw new BusinessRuntimeException(CommonErrorCode.API_REQUEST_FAILED);
        }
        return accessTokenInfo;
    }

    @Override
    public String getAccessToken(String accountId) {
        String key = "user:login:oauth:" + accountId + ":access_token";
        CacheKey cacheKey = KeyParam.of(key, false);
        String entity = cacheService.getString(cacheKey).getResults();
        if (ValueUtil.isEmpty(entity)) {
            String refreshKeyExpress = "user:login:oauth:" + accountId + ":refresh_token";
            CacheKey refreshKey = KeyParam.of(refreshKeyExpress, false);
            String refreshToken = cacheService.getString(refreshKey).getResults();
            if (ValueUtil.isNotEmpty(refreshToken)) {
                entity = refreshAccessToken(accountId).getAccessToken();
            }
        }
        return entity;
    }

    @Override
    public AccessTokenInfo refreshAccessToken(String accountId) {
        OauthorizeAccount account = getAccount(accountId);
        String url = account.getAccessTokenUrl();

        String refreshToken = account.getRefreshToken();
        boolean cache = ValueUtil.isEmpty(refreshToken);
        if (cache) {
            String key = "user:login:oauth:" + account.getId() + ":refresh_token";
            CacheKey cacheKey = KeyParam.of(key, false);
            refreshToken = cacheService.getString(cacheKey).getResults();
        }

        if (ValueUtil.isEmpty(refreshToken)) {
            throw new BusinessRuntimeException(CommonErrorCode.PARAMETER_INVALID, "refresh token is required.");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("client_id", account.getClientId());
        params.put("client_secret", account.getClientSecret());
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        params.put("redirect_uri", account.getRedirectUrl());
        ClientResult<Map> result = apiClient.postFor(url, params, Map.class, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        if (!result.isSuccess()) {
            throw new ErrorCodeRuntimeException(result.getCode(), result.getMessage());
        }

        Map<String, Object> map = result.getResults();
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setAccessToken((String) map.get("access_token"));

        accessTokenInfo.setScope((String) map.get("scope"));
        accessTokenInfo.setExpiresIn(Integer.parseInt(map.get("expires_in") + "") * 1000);

        if (cache) {
            String key = "user:login:oauth:" + account.getId() + ":access_token";
            CacheKey cacheKey = KeyParam.of(key, false, accessTokenInfo.getExpiresIn().longValue());
            cacheService.setString(cacheKey, accessTokenInfo.getAccessToken());
        }

        accessTokenInfo.setData(map);
        accessTokenInfo.setRefreshToken((String) map.get("refresh_token"));

        if (cache) {
            String refreshTokenKey = "user:login:oauth:" + account.getId() + ":refresh_token";
            cacheService.setString(KeyParam.of(refreshTokenKey, false, accessTokenInfo.getExpiresIn().longValue() * 2), accessTokenInfo.getRefreshToken());
        }
        return accessTokenInfo;
    }

    @Override
    public OAuthUserInfo getUserInfo(String accountId) {
        Account account = ComponentUtil.getBean(AccountService.class).getAccount(accountId);
        OauthorizeAccount oauthorizeAccount = new OauthorizeAccount(account.getConfig());
        String url = oauthorizeAccount.getUserInfoUrl();
        OAuthUserInfo oAuthUserInfo = new OAuthUserInfo();
        ClientResult<Map> result = apiClient.getFor(url + "?access_token=" + getAccessToken(accountId), null, Map.class);
        Map<String, Object> map = result.getResults();
        oAuthUserInfo.setUsername((String) map.get("login"));
        oAuthUserInfo.setNickname((String) map.get("remark"));
        oAuthUserInfo.setAvatar((String) map.get("avatar_url"));
        oAuthUserInfo.setData(map);
        oAuthUserInfo.setId(map.get("id") + "");
        return oAuthUserInfo;
    }
}
