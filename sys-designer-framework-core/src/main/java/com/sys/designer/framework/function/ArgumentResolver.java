package com.sys.designer.framework.function;

public interface ArgumentResolver {
    boolean support(Class<?> type);

    Object resolve(Class<?> targetClass, Object value);
}
