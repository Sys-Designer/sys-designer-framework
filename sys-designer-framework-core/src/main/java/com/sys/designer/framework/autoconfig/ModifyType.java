package com.sys.designer.framework.autoconfig;

import com.sys.designer.framework.api.TypeEnum;

public interface ModifyType extends TypeEnum<Integer> {
    default Integer getType() {
        return getValue();
    }
}
