package com.sys.designer.framework.api.task;

public interface TaskHandler {
    void run(TaskContext context);

    String getId();
}
