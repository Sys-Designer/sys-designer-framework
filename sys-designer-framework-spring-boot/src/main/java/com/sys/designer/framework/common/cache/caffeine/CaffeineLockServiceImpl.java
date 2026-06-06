package com.sys.designer.framework.common.cache.caffeine;

import com.sys.designer.framework.api.LockActionCallback;
import com.sys.designer.framework.api.cache.CacheKey;
import com.sys.designer.framework.api.cache.LocalCacheService;
import com.sys.designer.framework.api.cache.LockService;
import jakarta.annotation.Resource;

public class CaffeineLockServiceImpl implements LockService {

    @Resource
    private LocalCacheService localCacheService;

    @Override
    public void tryLockWith(CacheKey cacheKey, long timeout, LockActionCallback callback) {
        localCacheService.tryLockWith(cacheKey,timeout, callback);
    }
}
