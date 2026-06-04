package com.sys.designer.framework.api.crypto;

import com.sys.designer.framework.api.strategy.StrategyAdaptor;

public interface CryptoDataService extends StrategyAdaptor<CryptoType> {
    <T extends Encrypt> void encode(T data, CryptoType cryptoType);

    <T extends Decrypt> void decode(T data, CryptoType cryptoType);
}
