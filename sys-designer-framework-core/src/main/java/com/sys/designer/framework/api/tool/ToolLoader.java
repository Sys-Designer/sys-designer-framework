package com.sys.designer.framework.api.tool;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ToolLoader {
    /**
     *
     * @param models 模块列表
     * @return
     */
    List<Map<String, Object>> getTools(List<String> models);
}
