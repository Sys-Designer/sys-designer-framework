/**
 * Copyright (C) Code Cloud Platform. 2024-2023 .All Rights Reserved.
 */

package com.sys.designer.framework.api.mq;

import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;

public interface Callback {

    void onSuccess(Object data);

    default void onException(Throwable e) {
        throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, e);
    }
}
