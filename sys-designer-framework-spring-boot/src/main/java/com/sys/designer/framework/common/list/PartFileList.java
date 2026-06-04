package com.sys.designer.framework.common.list;

import com.sys.designer.framework.common.entity.PartFile;

import java.util.ArrayList;
import java.util.Collection;

public class PartFileList extends ArrayList<PartFile> {

    public PartFileList(int initialCapacity) {
        super(initialCapacity);
    }

    public PartFileList() {
    }

    public PartFileList(Collection<? extends PartFile> c) {
        super(c);
    }
}
