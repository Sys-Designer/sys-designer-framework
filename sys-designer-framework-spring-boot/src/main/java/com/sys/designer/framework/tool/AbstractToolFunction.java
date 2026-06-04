package com.sys.designer.framework.tool;

import com.sys.designer.framework.api.tool.ToolFunction;
import com.sys.designer.framework.api.tool.ToolInputSchema;
import com.sys.designer.framework.common.util.JsonUtil;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

public abstract class AbstractToolFunction implements ToolFunction, ToolCallback {
    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ToolInputSchema getInputSchema() {
        return null;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        ToolDefinition definition = new DefaultToolDefinition(getName(), getDescription(), JsonUtil.toJson(getInputSchema()));
        return definition;
    }
}
