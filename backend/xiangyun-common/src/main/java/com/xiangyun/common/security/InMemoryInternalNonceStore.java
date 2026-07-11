package com.xiangyun.common.security;

import java.time.Clock;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInternalNonceStore implements InternalNonceStore {
    private final Map<String, Long> seenNonces = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryInternalNonceStore() {
        this(Clock.systemUTC());
    }

    InMemoryInternalNonceStore(Clock clock) {
        this.clock = clock;
    }

    @Override
    public boolean markIfNew(String serviceName, String nonce, long ttlSeconds) {
        cleanup();
        String key = serviceName + ":" + nonce;
        long expiresAt = Instant.now(clock).getEpochSecond() + ttlSeconds;
        return seenNonces.putIfAbsent(key, expiresAt) == null;
    }

    private void cleanup() {
        long now = Instant.now(clock).getEpochSecond();
        Iterator<Map.Entry<String, Long>> iterator = seenNonces.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= now) {
                iterator.remove();
            }
        }
    }
}
