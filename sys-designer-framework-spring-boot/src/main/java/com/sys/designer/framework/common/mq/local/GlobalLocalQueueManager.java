/**
 * Copyright (C) Code Cloud Platform. 2024-2023 .All Rights Reserved.
 */

package com.sys.designer.framework.common.mq.local;

import com.sys.designer.framework.api.mq.Message;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.util.ValueUtil;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalLocalQueueManager {
    private final static Map<String, QueueEntry> GLOBAL_QUEUE = new ConcurrentHashMap<>();

    public static void add(String messageKey, QueueEntry queueEntry) {
        if (GLOBAL_QUEUE.containsKey(messageKey)) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, "%s already exists.", messageKey);
        }
        GLOBAL_QUEUE.put(messageKey, queueEntry);
    }

    public static QueueEntry get(String messageKey) {
        return GLOBAL_QUEUE.get(messageKey);
    }

    public static void send(Message<?> message) throws InterruptedException {
        if (Objects.isNull(message)) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, "message should not be null");
        }
        if (ValueUtil.isEmpty(message.getKey())) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, "message key should not be null empty.");
        }
        QueueEntry queueEntry = get(message.getKey());
        if (Objects.isNull(queueEntry)) {
            queueEntry = get("default");
        }
        queueEntry.getBlockingQueue().put(message);
    }

    public static void close() {
        GLOBAL_QUEUE.clear();
    }
}
