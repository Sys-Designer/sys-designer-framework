package com.sys.designer.framework.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sys.designer.framework.api.security.Rsa2CryptoService;
import com.sys.designer.framework.common.util.ComponentUtil;
import com.sys.designer.framework.common.util.ValueUtil;

public class DatasourceWrapper extends DruidDataSource {
    private String datasourceId;

    public DatasourceWrapper(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    @Override
    public void setPassword(String password) {
        CommonConfig commonConfig = ComponentUtil.getBean(CommonConfig.class);
        String publicKey = commonConfig.getValue("spring.datasource." + datasourceId + ".publicKey");
        if (ValueUtil.isEmpty(publicKey)) {
            super.setPassword(password);
            return;
        }
        Rsa2CryptoService rsa2CryptoService = ComponentUtil.getBean(Rsa2CryptoService.class);
        String pass = rsa2CryptoService.decryptByPublicKey(super.getPassword(), publicKey);
        super.setPassword(pass);
    }
}
