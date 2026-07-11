# Internal Service Authentication

Stage 2 adds a zero-trust boundary for internal service calls.

## Request Flow

```text
External client
-> Gateway cleans trusted headers
-> Gateway validates JWT / Redis session
-> Gateway writes trusted X-User-* headers
-> Gateway signs X-Internal-* headers
-> Operation / Analysis verifies the internal signature
```

Feign calls are also signed:

```text
Analysis -> Operation: signed by InternalFeignRequestInterceptor
Operation -> Auth: signed by InternalFeignRequestInterceptor
```

## Trusted Headers

Gateway removes these client-supplied headers before writing trusted values:

```text
X-User-Id
X-Username
X-User-Role
X-Village-Id
X-Internal-Service
X-Internal-Timestamp
X-Internal-Nonce
X-Internal-Signature
```

Internal authentication uses:

```text
X-Internal-Service
X-Internal-Timestamp
X-Internal-Nonce
X-Internal-Signature
X-Trace-Id
```

## Signature Canonical Form

The HMAC-SHA256 input is a fixed seven-line string:

```text
METHOD
PATH_WITH_QUERY
TIMESTAMP
NONCE
TRACE_ID
SERVICE_NAME
BODY_HASH
```

Stage 2 signs method, path, query string, timestamp, nonce, trace id, and service name.
`BODY_HASH` is reserved as an empty string for now. POST / PUT / PATCH request-body integrity validation is a Stage 2.1 enhancement because reading bodies in Gateway can affect reactive request forwarding.

## Nonce Replay Protection

Operation and Analysis reject repeated nonces.

Preferred storage:

```text
Redis key: internal:nonce:{service}:{nonce}
TTL: ttl-seconds + clock-skew-seconds
```

If Redis is not available in a narrow unit-test context, the implementation falls back to an in-memory store. Production and demo environments should use Redis.

## Service Allowlist

Operation allows:

```text
xiangyun-gateway
xiangyun-analysis-service
```

Analysis allows:

```text
xiangyun-gateway
```

## Excluded Paths

Default development/demo exclusions:

```text
/actuator/health
/actuator/info
/v3/api-docs/**
/swagger-ui/**
/swagger-ui.html
```

Production should keep the allowlist minimal. At most, keep `/actuator/health` open for health checks.

## Environment Controls

```text
XIANGYUN_INTERNAL_AUTH_ENABLED=true
XIANGYUN_INTERNAL_SECRET=change-me-strong-internal-secret
XIANGYUN_INTERNAL_TTL_SECONDS=60
XIANGYUN_INTERNAL_CLOCK_SKEW_SECONDS=5
```

Environment policy:

```text
dev: may temporarily disable for troubleshooting
demo: must enable
prod: must enable
test: controlled by test cases
```

Stage 2 acceptance must run with internal auth enabled.
