package com.sys.designer.framework.api.tool;

public interface CallFunction {
    String getName();

    Object call(String id, ToolParam param);
}
