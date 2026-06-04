package com.sys.designer.framework.common.util;

import com.sys.designer.framework.api.task.TaskService;
import com.sys.designer.framework.api.task.TaskType;

public final class TaskUtil {
    private TaskUtil() {
    }

    public static TaskService getTaskService(TaskType taskType) {
        return ComponentUtil.getStrategyBean(TaskService.class, taskType);
    }

    public static TaskService getTaskService() {
        return getTaskService(TaskType.LOCAL);
    }
}
