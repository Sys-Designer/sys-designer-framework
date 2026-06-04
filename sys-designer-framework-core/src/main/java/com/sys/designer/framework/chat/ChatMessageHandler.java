package com.sys.designer.framework.chat;

public interface ChatMessageHandler {
    void onMessage(ChatMessage message, ChatMessageCallback callback);
}
