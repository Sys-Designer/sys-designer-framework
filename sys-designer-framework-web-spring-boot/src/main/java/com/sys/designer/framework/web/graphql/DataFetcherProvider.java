package com.sys.designer.framework.web.graphql;

import graphql.schema.DataFetcher;

public interface DataFetcherProvider<T> {

    DataFetcher<T> get(DataFetcher<T> dataFetcher);
}
