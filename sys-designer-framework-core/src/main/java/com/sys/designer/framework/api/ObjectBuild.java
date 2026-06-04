package com.sys.designer.framework.api;

public interface ObjectBuild<T> {
    T build();

    T build(T data);
}
