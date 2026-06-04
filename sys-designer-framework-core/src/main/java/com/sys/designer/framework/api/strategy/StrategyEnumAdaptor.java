/**
 * Copyright (C) Oceancode Cloud Technologies Co., Ltd. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.api.strategy;

import com.sys.designer.framework.api.TypeEnum;

/**
 * @author qinjiawang
 */
public interface StrategyEnumAdaptor extends StrategyAdaptor<TypeEnum<?>> {
    @Override
    default boolean isSupport(TypeEnum<?> type) {
        return type.equals(support());
    }

    TypeEnum<?> support();
}
