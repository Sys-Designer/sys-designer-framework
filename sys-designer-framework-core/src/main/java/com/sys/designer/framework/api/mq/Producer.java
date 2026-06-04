/**
 * Copyright (C) Code Cloud Platform. 2024-2023 .All Rights Reserved.
 */

package com.sys.designer.framework.api.mq;

import com.sys.designer.framework.chat.ChatMessage;

public interface Producer {
    void send(Message<?> message, Callback callback);

    default void sendWithBusiness(Message<?> message, Callback callback) {
        MessageUtil.initMessage(message);
        send(message, callback);
    }

    RecordMeta send(Message<?> message);

    default RecordMeta sendWithBusiness(Message<?> message) {
        MessageUtil.initMessage(message);
        return send(message);
    }

    default void fillMessage(Message<ChatMessage> msg) {
    }
}
