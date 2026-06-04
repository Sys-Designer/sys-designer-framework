package com.sys.designer.framework.api.security;

public interface KeyManager {
    /**
     * get keys
     *
     * @param id
     */
    String getKey(String id, KeyType keyType);

    /**
     * update keys
     *
     * @param id
     */
    void updateKeys(String id, KeyType keyType, String key);

    void deleteKeys(String id, KeyType keyType);
}
