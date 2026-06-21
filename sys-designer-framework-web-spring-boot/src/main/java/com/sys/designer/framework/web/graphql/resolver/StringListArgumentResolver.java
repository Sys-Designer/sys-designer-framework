package com.sys.designer.framework.web.graphql.resolver;

import com.sys.designer.framework.common.list.StringList;
import com.sys.designer.framework.function.ArgumentResolver;
import graphql.language.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class StringListArgumentResolver implements ArgumentResolver {
    @Override
    public boolean support(Class<?> type) {
        return StringList.class.equals(type);
    }

    @Override
    public Object resolve(Class<?> targetClass, Object value) {
        StringList list = new StringList();
        if (value instanceof StringValue) {
            list.add(((StringValue) value).getValue());
        } else if (value instanceof ArrayValue) {
            ArrayValue arrayValue = (ArrayValue) value;
            List<Value> values = arrayValue.getValues();
            for (Value v : values) {
                if (Objects.isNull(v)) {
                    continue;
                }
                if (v instanceof StringValue) {
                    list.add(((StringValue) v).getValue());
                } else if (v instanceof IntValue) {
                    list.add(((IntValue) v).getValue() + "");
                } else if (v instanceof BooleanValue) {
                    list.add(((BooleanValue) v).isValue() + "");
                } else if (v instanceof FloatValue) {
                    list.add(((FloatValue) v).getValue() + "");
                }
            }
        }
        return list;
    }
}
