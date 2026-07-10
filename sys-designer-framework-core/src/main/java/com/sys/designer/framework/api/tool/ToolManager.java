package com.sys.designer.framework.api.tool;

import com.sys.designer.framework.common.util.ValueUtil;

import java.util.*;

public class ToolManager {
    private String name;

    public List<Map<String, Object>> getTools() {
        return Collections.emptyList();
    }

    private Map<String, CallFunction> callFunctionMap = new HashMap<>();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Object execute(String name, Map<String, Object> arguments) {
        return doExecute(name, new ToolParam(arguments));
    }

    protected Object doExecute(String name, ToolParam param) {
        return name;
    }

    public String getEventName(String method) {
        if (isClientMethod(method)) {
            return "func-call";
        }
        return "message";
    }

    public boolean isClientMethod(String method) {
        if (method.startsWith("_client.")) {
            return true;
        }
        return false;
    }

    public boolean hasOutputSchema(String name) {
        return true;
    }

    public void registerCallTools(List<CallFunction> callFunctionList) {
        if (ValueUtil.isEmpty(callFunctionList)) {
            return;
        }
        for (CallFunction it : callFunctionList) {
            if (ValueUtil.isEmpty(it.getName())) {
                return;
            }
            if (callFunctionMap.containsKey(it.getName())) {
                continue;
            }
            callFunctionMap.put(it.getName(), it);
        }
    }

    public CallFunction getCallFunction(String name) {
        return callFunctionMap.get(name);
    }
}
