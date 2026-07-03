# Rural-revitalization-OS

Xiangyun OS is a lightweight distributed rural revitalization platform demo for the final course project.

## Quick Start

Infrastructure:

```bash
docker compose -f docker-compose.demo.yml up -d
```

Backend services:

```bash
cd backend
.\scripts\check-backend.ps1
.\scripts\start-auth.ps1
.\scripts\start-operation.ps1
.\scripts\start-analysis.ps1
.\scripts\start-gateway.ps1
```

Run the four service commands in four separate terminals. These commands are backend services and will keep running until stopped.

Gateway:

```text
http://127.0.0.1:8080/api
```

Tests:

```bash
cd backend
mvn test
```

## Demo Notes

See [docs/distributed-architecture.md](docs/distributed-architecture.md) for the final distributed architecture, endpoint coverage, Redis cache flow, Nacos/Gateway/Feign demo flow, and video recording script.
