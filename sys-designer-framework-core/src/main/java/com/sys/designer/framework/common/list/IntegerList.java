/**
 * Copyright (C) Oceancode Cloud Technologies Co., Ltd. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.common.list;

import java.util.Collection;

/**
 * @author qinjiawang
 */
public class IntegerList extends WrapperArrayList<Integer> {
    public IntegerList() {
    }

    public IntegerList(Integer element) {
        add(element);
    }

    public IntegerList(Collection<Integer> collection) {
        addAll(collection);
    }
}
