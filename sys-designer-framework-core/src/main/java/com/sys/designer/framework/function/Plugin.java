package com.sys.designer.framework.function;

public interface Plugin {
    default String pluginId() {
        return null;
    }

    default boolean isFunction() {
        return true;
    }
}
