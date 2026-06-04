package com.sys.designer.framework.function;

public interface SimplePlugin extends Plugin {
    @Override
    default boolean isFunction() {
        return false;
    }
}
