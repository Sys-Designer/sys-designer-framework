package com.sys.designer.framework.api.crypto;

@FunctionalInterface
public interface EncryptCryptoFunction {
    Object encrypt(Object value);
}
