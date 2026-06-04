package com.sys.designer.framework.web.service;

import com.sys.designer.framework.api.cache.CacheService;

public class CaffeineSessionServiceImpl extends RedisSessionServiceImpl {
    public CaffeineSessionServiceImpl(CacheService cacheService) {
        super(cacheService);
    }
}
