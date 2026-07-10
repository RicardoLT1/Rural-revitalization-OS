# Backend Startup Guide

## Build

```powershell
cd backend
.\scripts\check-backend.ps1
```

Environment variables are documented in `../.env.example`. Keep real `.env` files local and do not commit them.

## Start Infrastructure

```powershell
docker compose -f ..\docker-compose.demo.yml up -d
```

Check infrastructure ports before starting services:

```powershell
.\scripts\check-infra.ps1
```

## Start Services

Open four terminals under `backend/`:

```powershell
.\scripts\start-auth.ps1
```

```powershell
.\scripts\start-operation.ps1
```

```powershell
.\scripts\start-analysis.ps1
```

```powershell
.\scripts\start-gateway.ps1
```

## Health Check

After all services have started, run:

```powershell
.\scripts\check-services-health.ps1
```

Expected service health endpoints:

- Gateway: http://127.0.0.1:8080/actuator/health
- Auth: http://127.0.0.1:8081/actuator/health
- Operation: http://127.0.0.1:8082/actuator/health
- Analysis: http://127.0.0.1:8083/actuator/health

## Ports

- Gateway: http://127.0.0.1:8080
- Auth: http://127.0.0.1:8081/swagger-ui.html
- Operation: http://127.0.0.1:8082/swagger-ui.html
- Analysis: http://127.0.0.1:8083/swagger-ui.html
- MySQL: 127.0.0.1:3307
- Redis: 127.0.0.1:6379
- Nacos: http://127.0.0.1:8848/nacos

## Important

Do not use `mvn -pl <module> -am spring-boot:run` for this project. It can try to run the parent aggregator project first and fail with a missing main class.

The service commands are long-running processes. They are working correctly when the terminal stays open and logs show the service started.

Every gateway response includes `X-Trace-Id`. Keep this value when reporting an API problem so backend logs and frontend requests can be matched quickly.
