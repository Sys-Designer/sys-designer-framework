package com.sys.designer.framework.api.interceptor;

public interface Interceptor {
    void before(String resourceId, int resourceType);

    void after(String resourceId, int resourceType);
}
