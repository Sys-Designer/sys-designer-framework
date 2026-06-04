package com.sys.designer.framework.chat;

import com.sys.designer.framework.api.ai.AiResponse;

public interface ChatService {
    String getType();

    AiResponse chat(ChatMessageInput message, ChatCallback callback);
}
