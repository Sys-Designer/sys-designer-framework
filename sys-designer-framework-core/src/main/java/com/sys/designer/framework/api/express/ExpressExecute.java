package com.sys.designer.framework.api.express;

import java.util.Map;

public interface ExpressExecute {
    Object execute(String express, Map<String, Object> env);

    default String getType() {
        return "default";
    }
}
