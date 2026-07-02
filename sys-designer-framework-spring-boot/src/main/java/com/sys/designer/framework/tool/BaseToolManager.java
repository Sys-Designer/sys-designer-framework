package com.sys.designer.framework.tool;

import com.sys.designer.framework.api.tool.ToolManager;
import com.sys.designer.framework.common.config.CommonConfig;
import com.sys.designer.framework.common.util.ComponentUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import com.sys.designer.framework.api.tool.McpTool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaseToolManager extends ToolManager {
    private Map<String, McpTool> toolMap = new HashMap();

    @Override
    public Object execute(String name, Map<String, Object> arguments) {
        String methods = ComponentUtil.getBean(CommonConfig.class).getValue("oc.mcp.methods");
        if (name.startsWith("user_") && ValueUtil.isNotEmpty(methods)) {
            String[] split = methods.split(",");
            for (String it : split) {

                if (!Objects.equals(it, name)) {
                    continue;
                }
                McpTool mcpTool = toolMap.get(name);
                if (!toolMap.containsKey(name)) {
                    synchronized (toolMap) {
                        mcpTool = toolMap.computeIfAbsent(name, k -> {
                            Collection<McpTool> values = ComponentUtil.getBeans(McpTool.class).values();
                            for (McpTool value : values) {
                                if (Objects.equals(value.getName(), name)) {
                                    return value;
                                }
                            }
                            return null;
                        });
                    }
                }
                if (Objects.isNull(mcpTool)) {
                    return null;
                }
                return mcpTool.execute(arguments);
            }
        }
        return super.execute(name, arguments);
    }
}
