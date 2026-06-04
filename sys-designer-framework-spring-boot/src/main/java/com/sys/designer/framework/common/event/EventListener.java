package com.sys.designer.framework.common.event;

import com.sys.designer.framework.api.event.EventHandler;
import com.sys.designer.framework.api.event.EventParam;
import com.sys.designer.framework.api.notifier.Notifier;
import com.sys.designer.framework.entity.EventNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class EventListener implements ApplicationListener<CustomEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

    private List<EventHandler> notifiers;

    public EventListener(List<EventHandler> notifiers) {
        this.notifiers = notifiers;
    }

    @Override
    public void onApplicationEvent(CustomEvent event) {
        if (!(event.getSource() instanceof EventParam eventParam)) {
            return;
        }
        notifiers.stream().filter(notifier -> {
            try {
                return notifier.support(eventParam);
            } catch (Throwable throwable) {
                LOGGER.error("error.", throwable);
                return false;
            }
        }).forEach(notifier -> {
            try {
                notifier.handler(eventParam);
            } catch (Throwable throwable) {
                LOGGER.error("error.", throwable);
            }
        });
    }
}
