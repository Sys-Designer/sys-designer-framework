package com.sys.designer.framework.object;

import com.sys.designer.framework.common.util.ValueUtil;

import java.util.Objects;

public class GTypeObject<T> extends GObject {
    private T object;

    public GTypeObject(T object) {
        super();
        this.object = object;
    }

    public T object() {
        validate();
        return object;
    }


    public String getGroup() {
        Class<?> aClass = Objects.nonNull(object()) ? object().getClass() : this.getClass();
        return ValueUtil.camelTo(aClass.getSimpleName(), "_");
    }
}
