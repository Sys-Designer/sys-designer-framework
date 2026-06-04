package com.sys.designer.framework.web.graphql;

import com.sys.designer.framework.function.Context;
import graphql.schema.DataFetchingEnvironment;

public class GraphQlContext implements Context {
    private DataFetchingEnvironment environment;

    public GraphQlContext(DataFetchingEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public boolean hasSelectionFields(String field, String... fields) {
        return this.environment.getSelectionSet().containsAllOf(field, fields);
    }
}
