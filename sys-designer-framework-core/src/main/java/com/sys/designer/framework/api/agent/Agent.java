package com.sys.designer.framework.api.agent;

import com.sys.designer.framework.chat.ChatMessage;
import com.sys.designer.framework.chat.ChatMessageInput;
import com.sys.designer.framework.chat.ChatMessageResponse;

public interface Agent {
    String getId();

    String getName();

    void chat(ChatMessageResponse response, ChatMessage chatMessage, ChatMessageInput messageInput);
}
