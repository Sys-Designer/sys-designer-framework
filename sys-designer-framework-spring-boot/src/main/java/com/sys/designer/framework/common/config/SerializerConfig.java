package com.sys.designer.framework.common.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.sys.designer.framework.api.mq.MessageType;
import com.sys.designer.framework.api.session.UserType;
import com.sys.designer.framework.chat.ChatMessageType;
import com.sys.designer.framework.chat.MessageLifeCycle;
import com.sys.designer.framework.common.util.JsonUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializerConfig {

    @PostConstruct
    public void init() {
        JsonUtil.registerTypeEnum(UserType.class);
        JsonUtil.registerTypeEnum(MessageType.class);
        JsonUtil.registerTypeEnum(ChatMessageType.class);
        JsonUtil.registerTypeEnum(MessageLifeCycle.class);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
        };
    }
}
