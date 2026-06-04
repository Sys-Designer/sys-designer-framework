/**
 * Copyright (C) NA Technologies Co., Ltd. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.api.cache;

import com.sys.designer.framework.api.TypeEnum;
import com.sys.designer.framework.api.strategy.StrategyEnumAdaptor;

import java.util.Collection;
import java.util.List;

public interface RedisCacheService extends CacheService, StrategyEnumAdaptor {

    @Override
    default TypeEnum<?> support() {
        return CacheType.REDIS;
    }

    <T> T executeScript(CacheKey keyParam, String scriptText, Class<T> returnTypeClass, List<String> args, Collection values);

    <T> T executeScriptFile(CacheKey keyParam,String path, Class<T> returnTypeClass, List<String> args, Collection values);

    <T> T executeScriptFile(CacheKey keyParam,Class resourceClassType, String path, Class<T> returnTypeClass, List<String> args, Collection values);
}
