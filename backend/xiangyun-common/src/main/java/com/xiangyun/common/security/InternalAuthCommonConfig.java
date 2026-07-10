package com.xiangyun.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalAuthCommonConfig {

    @Bean
    InternalSignatureSigner internalSignatureSigner(InternalAuthProperties properties) {
        return new InternalSignatureSigner(properties);
    }

    @Bean
    InternalSignatureVerifier internalSignatureVerifier(InternalAuthProperties properties,
                                                        InternalSignatureSigner signer) {
        return new InternalSignatureVerifier(properties, signer);
    }
}
