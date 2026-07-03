# Xiangyun OS Lightweight Distributed Architecture

## Overview

The final demo uses a real lightweight distributed backend:

- WeChat Mini Program is the only frontend.
- Spring Cloud Gateway is the only public backend entry.
- `xiangyun-auth-service`, `xiangyun-operation-service`, and `xiangyun-analysis-service` are independently runnable services.
- Gateway and all business services register with Nacos.
- Analysis calls Operation through OpenFeign; Operation calls Auth through OpenFeign.
- Redis stores login sessions, resource detail cache, and dashboard cache.
- MySQL uses one `xiangyun_os` database for course-demo simplicity.

## Ports

| Component | Port | Purpose |
| --- | ---: | --- |
| Gateway | 8080 | Mini program API entry |
| Auth Service | 8081 | Login, users, roles |
| Operation Service | 8082 | Villages, resources, workflows |
| Analysis Service | 8083 | Dashboard, reports, forecasts |
| Nacos | 8848 | Registry and config center |
| MySQL | 3307 | Business database host port, mapped to container port 3306 |
| Redis | 6379 | Session and business cache |

## Startup

```bash
docker compose -f docker-compose.demo.yml up -d
cd backend
mvn compile
cd xiangyun-auth-service && mvn exec:java -Dexec.mainClass=com.xiangyun.auth.AuthServiceApplication
cd xiangyun-operation-service && mvn exec:java -Dexec.mainClass=com.xiangyun.operation.OperationServiceApplication
cd xiangyun-analysis-service && mvn exec:java -Dexec.mainClass=com.xiangyun.analysis.AnalysisServiceApplication
cd xiangyun-gateway && mvn exec:java -Dexec.mainClass=com.xiangyun.gateway.GatewayApplication
```

Run each service command in a separate terminal. A service command does not exit after startup; it keeps the process alive to listen on its port.

Recommended service start order:

1. Nacos, MySQL, Redis
2. Auth Service
3. Operation Service
4. Analysis Service
5. Gateway

## Demo Accounts

All demo account passwords are `123456`.

| Username | Role | Terminal |
| --- | --- | --- |
| user_demo | USER | WeChat Mini Program |
| staff_demo | STAFF | Web Workbench |
| admin | ADMIN | Web Workbench |

Legacy accounts `operator`, `approver`, and `viewer` remain compatible and map to `STAFF`, but they are not used in the final recording.

## Core Demo Flow

1. Mini Program logs in through `POST /api/auth/login`.
2. Auth generates JWT and writes `login:token:{jti}` to Redis.
3. Mini Program calls `GET /api/dashboard` through Gateway.
4. Gateway validates JWT and Redis session, then writes trusted `X-User-*` headers.
5. Gateway routes to Analysis by `lb://xiangyun-analysis-service`.
6. Analysis checks `dashboard:{villageId}:{days}` in Redis.
7. On cache miss, Analysis calls Operation through OpenFeign.
8. Operation reads MySQL resource/workflow data and calls Auth for user summary where needed.
9. Analysis aggregates report data, writes dashboard cache, and returns a unified response.
10. Second dashboard request hits Redis cache.

## Verification

```bash
cd backend
mvn test
```

Current automated test coverage:

- 24 tests
- Auth login/session/user summary
- Operation resources/workflows/cache eviction/Feign user summary
- Analysis dashboard/report/forecast/Feign/cache behavior
- Gateway public path, missing token, valid token, missing Redis session

Endpoint count in active multi-service modules:

```text
74 controller mappings
```

## Notes

- `/infra/*` explanation-only endpoints are not part of the final distributed demo.
- Gateway Swagger aggregation is intentionally not required; each service exposes its own Swagger UI.
- MySQL is shared physically, but services only use their own logical area and exchange cross-domain data through Feign.
