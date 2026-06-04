package com.sys.designer.framework.common.event;

import com.sys.designer.framework.api.event.EventParam;
import com.sys.designer.framework.common.util.ComponentUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EventImpl {
    @Async
    public void sendAsync(EventParam event) {
        ComponentUtil.getApplicationContext().publishEvent(new CustomEvent(event));
    }

    public void send(EventParam event) {
        ComponentUtil.getApplicationContext().publishEvent(new CustomEvent(event));
    }
}
