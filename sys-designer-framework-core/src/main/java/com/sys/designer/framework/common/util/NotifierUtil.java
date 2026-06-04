package com.sys.designer.framework.common.util;

import com.sys.designer.framework.api.NotifierType;

public final class NotifierUtil {
    private NotifierUtil() {
    }

    public static boolean isModify(NotifierType notifierType) {
        return NotifierType.ADD.equals(notifierType) ||
                NotifierType.UPDATE.equals(notifierType) ||
                NotifierType.DELETE.equals(notifierType);
    }
}
