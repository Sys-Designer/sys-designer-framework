package com.sys.designer.framework.common.task.local;

import com.sys.designer.framework.api.task.Task;

import java.util.concurrent.ScheduledFuture;

public final class ScheduledTask {

    volatile ScheduledFuture<?> future;
    private Task task;

    public ScheduledTask(Task task) {
        this.task = task;
    }

    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(true);
        }
    }

    public Task getTask() {
        return task;
    }
}