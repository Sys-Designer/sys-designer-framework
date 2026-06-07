package com.sys.designer.framework.api.tool;

import java.util.Map;

public interface McpTool {
    String getName();

    Object execute(Map<String, Object> arguments);
}
