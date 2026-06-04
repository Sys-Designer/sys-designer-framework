/**
 * Copyright (C) Oceancode Cloud Technologies Co., Ltd. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.api.strategy;

/**
 * @param <T>
 * @author qinjiawang
 */
public interface StrategyAdaptor<T> {

    /**
     * match target implement class
     *
     * @param type data type
     * @return true if matched else false
     */
    boolean isSupport(T type);
}
