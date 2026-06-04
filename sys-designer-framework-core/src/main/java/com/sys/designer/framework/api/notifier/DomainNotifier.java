package com.sys.designer.framework.api.notifier;


import com.sys.designer.framework.common.entity.Domain;
import com.sys.designer.framework.entity.EventNotifier;

public interface DomainNotifier extends Notifier {

    @Override
    default boolean support(EventNotifier eventNotifier) {
        if (eventNotifier.hasClassType(Domain.class)) {
            return true;
        }
        if (eventNotifier.getNewValue() instanceof Domain || eventNotifier.getOldValue() instanceof Domain) {
            return true;
        }
        return false;
    }
}
