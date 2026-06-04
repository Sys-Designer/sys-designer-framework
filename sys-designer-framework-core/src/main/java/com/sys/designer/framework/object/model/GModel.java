package com.sys.designer.framework.object.model;

import com.sys.designer.framework.object.GDescription;
import com.sys.designer.framework.object.GName;
import com.sys.designer.framework.object.GTypeObject;


public class GModel<T> extends GTypeObject<T> implements GDescription, GName {

    public GModel(T object) {
        super(object);
    }

    public String modelId() {
        return propAsString("modelId");
    }
}
