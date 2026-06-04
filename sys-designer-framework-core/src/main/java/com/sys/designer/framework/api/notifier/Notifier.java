package com.sys.designer.framework.api.notifier;

import com.sys.designer.framework.entity.EventNotifier;

public interface Notifier {

    boolean support(EventNotifier eventNotifier);

    void notifier(EventNotifier eventNotifier);
}
