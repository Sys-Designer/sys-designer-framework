package com.sys.designer.framework.common.config;

import com.sys.designer.framework.api.cache.CacheService;
import com.sys.designer.framework.api.cache.LocalCacheService;
import com.sys.designer.framework.api.cache.LockService;
import com.sys.designer.framework.api.cache.RedisCacheService;
import com.sys.designer.framework.api.express.ExpressExecute;
import com.sys.designer.framework.api.file.FileService;
import com.sys.designer.framework.api.mq.Consumer;
import com.sys.designer.framework.api.mq.Producer;
import com.sys.designer.framework.api.security.AesCryptoService;
import com.sys.designer.framework.api.security.Rsa2CryptoService;
import com.sys.designer.framework.common.cache.LockServiceImpl;
import com.sys.designer.framework.common.cache.caffeine.CaffeineLockServiceImpl;
import com.sys.designer.framework.common.cache.redis.RedisLockServiceImpl;
import com.sys.designer.framework.common.express.ExpressEnginManager;
import com.sys.designer.framework.common.id.RedisIncrementIdGenerator;
import com.sys.designer.framework.common.mq.local.LocalConsumer;
import com.sys.designer.framework.common.mq.local.LocalProducer;
import com.sys.designer.framework.common.mq.redis.RedisProducer;
import com.sys.designer.framework.common.security.AesCrypto;
import com.sys.designer.framework.common.security.Rsa2Crypto;
import com.sys.designer.framework.file.LocalFileServiceImpl;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

@Configuration
public class AutoConfigService {

    @Bean
    @ConditionalOnMissingBean(BCryptPasswordEncoder.class)
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(Rsa2CryptoService.class)
    public Rsa2CryptoService cryptoService() {
        return new Rsa2Crypto();
    }

    @Bean
    @ConditionalOnMissingBean(AesCryptoService.class)
    public AesCrypto aesCrypto() {
        return new AesCrypto();
    }


    @Bean
    @ConditionalOnMissingBean(Producer.class)
    @ConditionalOnBean(RedisCacheService.class)
    public RedisProducer redisProducer() {
        return new RedisProducer();
    }


    @Bean
    @ConditionalOnMissingBean(Producer.class)
    @ConditionalOnBean({Consumer.class})
    public LocalProducer localProducer() {
        return new LocalProducer();
    }

    @Bean
    @ConditionalOnBean(LocalProducer.class)
    public LocalConsumer localConsumer() {
        return new LocalConsumer();
    }

    @Bean
    @ConditionalOnClass({CacheService.class, RedisTemplate.class})
    @ConditionalOnMissingBean
    @ConditionalOnBean
    public RedisIncrementIdGenerator redisIncrementIdGenerator() {
        return new RedisIncrementIdGenerator();
    }

    @Bean
    @ConditionalOnBean(RedisCacheService.class)
    public LockService redisLock() {
        return new RedisLockServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(LockService.class)
    @ConditionalOnBean(LocalCacheService.class)
    public LockService localLock() {
        return new CaffeineLockServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(LockService.class)
    public LockService lockService() {
        return new LockServiceImpl();
    }

    @Bean
    @ConditionalOnBean(ExpressExecute.class)
    public ExpressEnginManager expressEnginManager(Set<ExpressExecute> executes) {
        return new ExpressEnginManager(executes);
    }

    @Bean
    @ConditionalOnMissingBean(FileService.class)
    public LocalFileServiceImpl localFileService() {
        return new LocalFileServiceImpl();
    }
}
