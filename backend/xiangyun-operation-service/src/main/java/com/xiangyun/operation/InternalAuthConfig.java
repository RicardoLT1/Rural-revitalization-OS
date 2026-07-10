package com.xiangyun.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiangyun.common.security.InMemoryInternalNonceStore;
import com.xiangyun.common.security.InternalAuthFilter;
import com.xiangyun.common.security.InternalAuthProperties;
import com.xiangyun.common.security.InternalFeignRequestInterceptor;
import com.xiangyun.common.security.InternalNonceStore;
import com.xiangyun.common.security.InternalSignatureSigner;
import com.xiangyun.common.security.InternalSignatureVerifier;
import com.xiangyun.common.security.RedisInternalNonceStore;
import feign.RequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class InternalAuthConfig {

    @Bean
    InternalNonceStore internalNonceStore(ObjectProvider<StringRedisTemplate> redisTemplate) {
        StringRedisTemplate template = redisTemplate.getIfAvailable();
        return template == null ? new InMemoryInternalNonceStore() : new RedisInternalNonceStore(template);
    }

    @Bean
    InternalAuthFilter internalAuthFilter(InternalAuthProperties properties,
                                          InternalSignatureVerifier verifier,
                                          InternalNonceStore nonceStore,
                                          ObjectMapper objectMapper) {
        return new InternalAuthFilter(properties, verifier, nonceStore, objectMapper);
    }

    @Bean
    RequestInterceptor internalFeignRequestInterceptor(InternalSignatureSigner signer) {
        return new InternalFeignRequestInterceptor(signer);
    }
}
