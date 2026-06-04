package com.sys.designer.framework.api.security;

import com.sys.designer.framework.api.strategy.StrategyAdaptor;

public interface CryptoService<T,R> extends StrategyAdaptor<String> {

    R encrypt(T input,String key);

    R decrypt(T input,String key);
}
