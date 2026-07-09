/**
 * Copyright (C) Oceancode Cloud Technologies Co., Ltd. 2024-2024 .All Rights Reserved.
 */
package com.sys.designer.framework.common.util;

import com.sys.designer.framework.api.TypeEnum;
import com.sys.designer.framework.api.strategy.StrategyAdaptor;
import com.sys.designer.framework.common.config.CommonConfig;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.function.BaseFunction;
import com.sys.designer.framework.common.function.ClientFunction;
import com.sys.designer.framework.common.function.LocalFunction;
import com.sys.designer.framework.common.function.RemoteFunction;
import com.sys.designer.framework.function.Plugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public final class ComponentUtil implements ApplicationContextAware {

    // 不再静态常驻上下文，改用实例持有，每次静态方法从实例取最新就绪上下文
    private static ComponentUtil INSTANCE;
    private ApplicationContext context;
    private static ApplicationContext CONTEXT;

    private ComponentUtil() {
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.context = ctx;
        INSTANCE = this;
    }

    private static ApplicationContext getContext() {
        if (INSTANCE == null || INSTANCE.context == null) {
            if (Objects.nonNull(CONTEXT)) {
                return CONTEXT;
            }
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, "Spring上下文未初始化");
        }
        return INSTANCE.context;
    }

    // ===================== 下面所有方法只改一行：applicationContext → getContext() =====================

    public static <T> Map<String, T> getBeans(Class<T> beanTypeClassType) {
        return getContext().getBeansOfType(beanTypeClassType);
    }

    public static <T> T getBean(Class<T> beanTypeClassType) {
        return getContext().getBean(beanTypeClassType);
    }

    public static <T> T getBean(Class<T> beanTypeClassType, boolean throwEx) {
        try {
            return getContext().getBean(beanTypeClassType);
        } catch (Throwable throwable) {
            if (throwEx) {
                throw throwable;
            }
        }
        return null;
    }

    public static <T> T getBean(String key, Class<T> returnClassType) {
        return getContext().getBean(key, returnClassType);
    }

    public static <T> T getBean(String key, Class<T> returnClassType, boolean throwEx) {
        try {
            return getContext().getBean(key, returnClassType);
        } catch (Throwable t) {
            if (throwEx) {
                throw t;
            }
        }
        return null;
    }

    public static <T> T getBean(Class<T> beanTypeClassType, Function<T, Boolean> function) {
        Map<String, T> map = getBeans(beanTypeClassType);
        if (map == null || map.isEmpty()) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, beanTypeClassType.getName() + " not found.");
        }
        for (T value : map.values()) {
            Boolean target = function.apply(value);
            if (Objects.nonNull(target) && target) {
                return value;
            }
        }
        return null;
    }

    public static <T> T getStrategyBean(Class<T> typeClass, TypeEnum<?> type) {
        return getStrategyBean0(typeClass, type);
    }

    public static <T> T getStrategyBean(Class<T> typeClass, String id) {
        return getStrategyBean0(typeClass, id);
    }

    private static <T> T getStrategyBean0(Class<T> typeClass, Object type) {
        T bean = getBean(typeClass, service -> StrategyAdaptor.class.isAssignableFrom(service.getClass()) && ((StrategyAdaptor) service).isSupport(type));
        if (Objects.isNull(bean)) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, typeClass.getName() + "[" + type + "] not found");
        }
        return bean;
    }

    public static void setApplicationContextBean(ApplicationContext context) {
        CONTEXT = context;
    }

    public static ApplicationContext getApplicationContext() {
        return getContext();
    }

    public static <T> T getLocalFunction(Class<T> typeClass, List<T> functions) {
        if (functions.size() == 1) {
            return functions.get(0);
        }
        T testFunction = null;
        T localFunction = null;
        boolean isTest = ComponentUtil.getBean(CommonConfig.class).isTest();
        for (T function : functions) {
            Class<?>[] interfaces = function.getClass().getInterfaces();
            if (interfaces.length == 1 && interfaces[0].equals(Plugin.class)) {
                if (Objects.nonNull(function.getClass().getSuperclass())) {
                    interfaces = function.getClass().getSuperclass().getInterfaces();
                }
            }
            for (Class<?> it : interfaces) {
                if ("com.sys.designer.framework.test.TestFunction".equals(it.getName())) {
                    testFunction = function;
                } else if (it.equals(typeClass)) {
                    localFunction = function;
                }
            }
        }
        if (isTest) {
            return Objects.nonNull(testFunction) ? testFunction : localFunction;
        }
        return localFunction;
    }

    public static <T> T getLocalFunction(Class<T> functionClass) {
        return getLocalFunction(functionClass, true);
    }

    public static <T> T getLocalFunction(Class<T> functionClass, boolean throwEx) {
        try {
            return getBean(functionClass, e -> {
                if (e instanceof LocalFunction) {
                    return true;
                }
                if (e instanceof RemoteFunction || e instanceof ClientFunction) {
                    return false;
                }
                return !(e instanceof BaseFunction<?>);
            });
        } catch (Throwable throwable) {
            if (throwEx) {
                throw throwable;
            }
        }
        return null;
    }

    public static <T> T getRemoteFunction(Class<T> functionClass, boolean onlyGetRemoteFunction) {
        return getRemoteFunction(functionClass, onlyGetRemoteFunction, true);
    }

    public static <T> T getRemoteFunction(Class<T> functionClass, boolean onlyGetRemoteFunction, boolean throwEx) {
        try {
            List<T> clientFunction = new ArrayList<>();
            List<T> remoteFunction = new ArrayList<>();
            for (T value : getBeans(functionClass).values()) {
                if (ClientFunction.class.isInstance(value)) {
                    clientFunction.add(value);
                } else if (RemoteFunction.class.isInstance(value)) {
                    remoteFunction.add(value);
                }
            }
            clientFunction.sort(Comparator.comparingInt(o -> ((ClientFunction) o).getType().getOrder()));
            if (onlyGetRemoteFunction) {
                return clientFunction.isEmpty() ? null : clientFunction.get(0);
            }
            if (!remoteFunction.isEmpty() && !clientFunction.isEmpty()) {
                return getLocalFunction(functionClass);
            }
            if (clientFunction.isEmpty()) {
                return getLocalFunction(functionClass);
            }
            return clientFunction.get(0);
        } catch (Throwable throwable) {
            if (throwEx) {
                throw throwable;
            }
        }
        return null;
    }

    public static void checkPlugin(Plugin plugin) {

    }
}