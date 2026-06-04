package com.sys.designer.framework.api.task;

import com.sys.designer.framework.api.Result;
import com.sys.designer.framework.api.strategy.StrategyAdaptor;

public interface TaskService extends StrategyAdaptor<TaskType> {
    Result<TaskInfo> addTask(Task task);

    Result<TaskInfo> startTask(String id);

    Result<TaskInfo> stopTask(String id);

    Result<TaskInfo> removeTask(String id);

    Result<TaskInfo> getInfo(String id);
}
