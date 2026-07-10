package com.xiangyun.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "xiangyun.internal-auth")
public class InternalAuthProperties {
    private boolean enabled = true;
    private String serviceName = "xiangyun-service";
    private String secret = "dev-internal-secret";
    private long ttlSeconds = 60;
    private long clockSkewSeconds = 5;
    private List<String> allowedServices = new ArrayList<>();
    private List<String> excludePaths = new ArrayList<>(List.of(
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public long getClockSkewSeconds() {
        return clockSkewSeconds;
    }

    public void setClockSkewSeconds(long clockSkewSeconds) {
        this.clockSkewSeconds = clockSkewSeconds;
    }

    public List<String> getAllowedServices() {
        return allowedServices;
    }

    public void setAllowedServices(List<String> allowedServices) {
        this.allowedServices = allowedServices == null ? new ArrayList<>() : allowedServices;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths == null ? new ArrayList<>() : excludePaths;
    }

    public long nonceTtlSeconds() {
        return ttlSeconds + clockSkewSeconds;
    }
}
