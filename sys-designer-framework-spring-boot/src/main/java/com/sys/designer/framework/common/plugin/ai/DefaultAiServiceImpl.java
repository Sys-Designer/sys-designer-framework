package com.sys.designer.framework.common.plugin.ai;

import com.sys.designer.framework.api.ai.AiResponse;
import com.sys.designer.framework.chat.ChatCallback;
import com.sys.designer.framework.chat.ChatMessageChoice;
import com.sys.designer.framework.chat.ChatMessageInput;
import com.sys.designer.framework.chat.ChatMessageResponse;
import com.sys.designer.framework.chat.ChatMessageResponseContent;
import com.sys.designer.framework.chat.ChatService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Primary
@Component
@ConditionalOnClass(ChatClient.class)
public class DefaultAiServiceImpl implements ChatService {
    @Resource
    private ChatClient chatClient;

    @Override
    public String getType() {
        return "";
    }

    @Override
    public AiResponse chat(ChatMessageInput message, ChatCallback callback) {
        Prompt prompt = new PromptTemplate("执行初始化服务: 1.初始化缓存.").create();
        chatClient.prompt(prompt)
                .stream()
                .chatResponse()
                .subscribe(data -> {
                    ChatMessageResponse response = new ChatMessageResponse();
                    response.setId(data.getMetadata().getId());
                    response.setModel(data.getMetadata().getModel());
                    Object object = data.getMetadata().get("created");
                    if (Objects.nonNull(object)) {
                        Long timestamp = Long.parseLong(String.valueOf(object));
                        if (String.valueOf(timestamp).length() == 10) {
                            timestamp = timestamp * 1000;
                        }
                        response.setCreated(timestamp);
                    }
                    AssistantMessage output = data.getResult().getOutput();

                    response.setChoices(new ArrayList<>());

                    ChatMessageChoice choice = new ChatMessageChoice();
                    response.getChoices().add(choice);
                    choice.setRole(output.getMessageType().getValue());
                    choice.setMessage(new ChatMessageResponseContent());

                    choice.getMessage().setContent(output.getText());

                    Object index = output.getMetadata().get("index");
                    if (Objects.nonNull(index)) {
                        Integer target = index instanceof Integer val ? val : Integer.parseInt(String.valueOf(index));
                        choice.setIndex(target);
                    }
                    callback.call(choice);
                });
        return null;
    }
}
