package com.xiangyun.common.security;

public interface InternalNonceStore {
    boolean markIfNew(String serviceName, String nonce, long ttlSeconds);
}
