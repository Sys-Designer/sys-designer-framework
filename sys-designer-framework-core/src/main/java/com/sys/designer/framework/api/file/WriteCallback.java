package com.sys.designer.framework.api.file;

import java.util.List;

public interface WriteCallback<T extends DataRow> {
    List<T> getRows();
}
