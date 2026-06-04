package com.sys.designer.framework.web.service;

import com.sys.designer.framework.api.cache.LocalCacheService;
import com.sys.designer.framework.api.cache.RedisCacheService;
import com.sys.designer.framework.api.session.SessionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class SessionConfig {

    @Bean
    @ConditionalOnBean({RedisCacheService.class, RedisTemplate.class})
    public RedisSessionServiceImpl redisSessionService(RedisCacheService redisCacheService) {
        return new RedisSessionServiceImpl(redisCacheService);
    }

    @Bean
    @ConditionalOnMissingBean(SessionService.class)
    @ConditionalOnBean({LocalCacheService.class})
    public CaffeineSessionServiceImpl caffeineSessionService(LocalCacheService localCacheService) {
        return new CaffeineSessionServiceImpl(localCacheService);
    }
}
