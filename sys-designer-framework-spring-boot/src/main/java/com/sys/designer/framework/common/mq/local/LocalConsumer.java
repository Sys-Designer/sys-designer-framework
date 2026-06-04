package com.sys.designer.framework.common.mq.local;

import com.sys.designer.framework.api.mq.Consumer;
import com.sys.designer.framework.common.util.ComponentUtil;

public class LocalConsumer extends AbstractLocalBlockQueue {
    @Override
    protected String getKey() {
        return "default";
    }

    @Override
    protected Consumer getConsumer() {
        return ComponentUtil.getBean(Consumer.class);
    }
}
