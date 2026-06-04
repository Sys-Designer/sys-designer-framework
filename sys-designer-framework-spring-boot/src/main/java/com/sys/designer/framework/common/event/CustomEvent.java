package com.sys.designer.framework.common.event;

import com.sys.designer.framework.api.event.EventParam;
import org.springframework.context.ApplicationEvent;

public class CustomEvent extends ApplicationEvent {
    public CustomEvent(EventParam source) {
        super(source);
    }
}
