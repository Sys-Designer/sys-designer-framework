/**
 * Copyright (C) Code Cloud Platform. 2024-2023 .All Rights Reserved.
 */

package com.sys.designer.framework.api.mq;

import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;

import java.util.List;

public interface Consumer<T> {
    void poll(List<Message<T>> list);

    default void onException(Throwable throwable) {
        throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, throwable);
    }

    default Class<T> getDataType(Message<T> message) {
        return null;
    }

    default String getKey() {
        return null;
    }

    default boolean support(Message<Object> message) {
        return true;
    }

}
