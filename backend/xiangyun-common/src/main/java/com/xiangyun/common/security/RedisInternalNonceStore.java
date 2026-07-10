package com.xiangyun.common.security;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

public class RedisInternalNonceStore implements InternalNonceStore {
    private final StringRedisTemplate redisTemplate;

    public RedisInternalNonceStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean markIfNew(String serviceName, String nonce, long ttlSeconds) {
        String key = "internal:nonce:" + serviceName + ":" + nonce;
        Boolean stored = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds));
        return Boolean.TRUE.equals(stored);
    }
}
