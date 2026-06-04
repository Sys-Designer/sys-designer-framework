package com.sys.designer.framework.common.service;


import com.sys.designer.framework.api.security.PasswordCryptoService;
import com.sys.designer.framework.common.util.PasswordUtil;

public final class PasswordServiceImpl implements PasswordCryptoService {
    @Override
    public String encode(String input) {
        return PasswordUtil.encode(input);
    }

    @Override
    public boolean matches(String raw, String encoded) {
        return PasswordUtil.matches(raw, encoded);
    }
}
