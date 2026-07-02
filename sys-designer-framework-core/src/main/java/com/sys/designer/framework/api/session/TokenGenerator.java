package com.sys.designer.framework.api.session;

public interface TokenGenerator {
    TokenInfo get(String deviceId, Long userId, String openid);

    TokenInfo parse(String token);
}
