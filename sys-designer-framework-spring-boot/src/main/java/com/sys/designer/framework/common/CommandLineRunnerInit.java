/**
 * Copyright (C) Oceancode Cloud. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.common;

import com.sys.designer.framework.api.ApplicationLifeCycleService;
import com.sys.designer.framework.common.cache.CommonCacheManager;
import com.sys.designer.framework.common.config.CommonConfig;
import com.sys.designer.framework.common.util.CacheUtil;
import com.sys.designer.framework.common.util.ComponentUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Order(1)
@Component
@Primary
public class CommandLineRunnerInit implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineRunnerInit.class);

    @Resource
    private CommonConfig commonConfig;

    @Override
    public void run(String... args) throws Exception {
        CommonCacheManager.getAllCacheManager();
        doCheckConfig();
        for (ApplicationLifeCycleService service : ComponentUtil.getBeans(ApplicationLifeCycleService.class).values()) {
            service.onReady();
        }
    }

    public void doCheckConfig() {
        commonConfig.getStripPrefixes();
        try {
            CacheUtil.init();
        } catch (Exception e) {
            LOGGER.warn("CacheUtil.init failed", e);
        }
    }
}
