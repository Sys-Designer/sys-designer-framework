package com.sys.designer.framework.common.express;

import com.sys.designer.framework.api.express.ExpressExecute;
import com.sys.designer.framework.common.config.CommonConfig;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.util.ValueUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ExpressEnginManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(ExpressEnginManager.class);

    private Map<String, ExpressExecute> executeCache;

    @Resource
    private CommonConfig commonConfig;

    public ExpressEnginManager(Set<ExpressExecute> executes) {
        if (!executes.isEmpty()) {
            executeCache = new HashMap<>();
        }
        ExpressExecute defaultEngin = null;
        for (ExpressExecute it : executes) {
            String type = it.getType();
            if (ValueUtil.isEmpty(type)) {
                type = "default";
            }

            if (executeCache.containsKey(type)) {
                LOGGER.error("express engine[{}] already exits.{}", type, it.getClass().getName());
                continue;
            }
            if (Objects.isNull(defaultEngin) || "default".equals(type)) {
                defaultEngin = it;
            }
            executeCache.put(type, it);
            LOGGER.info("express engine[{}] register success.{}", type, it.getClass().getName());
        }
        if (Objects.nonNull(defaultEngin) && executeCache.size() == 1) {
            executeCache.put("default", defaultEngin);
        }
    }

    public Object execute(String type, String express, Map<String, Object> env) {
        ExpressExecute expressExecute = executeCache.get(type);
        if (Objects.isNull(expressExecute)) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, "express engin[" + type + "] not found.");
        }
        return expressExecute.execute(express, env);
    }

    public Object execute(String express, Map<String, Object> env) {
        return execute(commonConfig.getValue("oc.system.express.engin", "default"), express, env);
    }
}
