/**
 * Copyright (C) Code Cloud Platform. 2024-2023 .All Rights Reserved.
 */

package com.sys.designer.framework.common.mq.local;


import com.sys.designer.framework.api.mq.Callback;
import com.sys.designer.framework.api.mq.Message;
import com.sys.designer.framework.api.mq.Producer;
import com.sys.designer.framework.api.mq.RecordMeta;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.util.SessionUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LocalProducer implements Producer {
    @Override
    public void send(Message<?> message, Callback callback) {
        List<Object> sessionValues = SessionUtil.getValues();
        CompletableFuture.runAsync(() -> {
            try {
                SessionUtil.setValues(sessionValues);
                GlobalLocalQueueManager.send(message);
                callback.onSuccess(null);
            } catch (InterruptedException e) {
                callback.onException(e);
            } finally {
                SessionUtil.remove();
            }
        });

    }

    @Override
    public RecordMeta send(Message<?> message) {
        try {
            GlobalLocalQueueManager.send(message);
            return null;
        } catch (InterruptedException e) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, e);
        }
    }
}
