package com.sys.designer.framework.api;

public interface IdGenerator {
    Object nextId();

    Class<?> getType();
}
