package com.sys.designer.framework.api.cache;

import com.sys.designer.framework.api.LockActionCallback;
import com.sys.designer.framework.api.TypeEnum;
import com.sys.designer.framework.api.strategy.StrategyEnumAdaptor;

public interface LocalCacheService extends CacheService, StrategyEnumAdaptor {

    @Override
    default TypeEnum<?> support() {
        return CacheType.LOCAL;
    }

    void tryLockWith(CacheKey key, long timeout, LockActionCallback callback);
}
