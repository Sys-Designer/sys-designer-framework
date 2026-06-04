package com.sys.designer.framework.object.model;

public interface ModelRecord {
    void record(ModifyType type, String source, Object newValue, Object oldValue);
}
