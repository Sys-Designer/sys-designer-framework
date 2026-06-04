package com.sys.designer.framework.api.diff;

import java.util.List;

public interface DiffCompare {

    default List<DiffDetail> diff(Object o) {
        return null;
    }
}
