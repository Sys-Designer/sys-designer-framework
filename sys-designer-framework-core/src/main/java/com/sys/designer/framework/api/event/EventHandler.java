package com.sys.designer.framework.api.event;

public interface EventHandler<T extends EventParam> {
    boolean support(EventParam param);

    void handler(T param);
}
