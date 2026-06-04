package com.sys.designer.framework.api.security;

public interface PasswordCryptoService {

    String encode(String input);

    boolean matches(String raw, String encoded);
}
