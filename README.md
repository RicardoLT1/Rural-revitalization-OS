# Rural-revitalization-OS

Xiangyun OS is a lightweight distributed rural resource operation and collaboration platform demo.

The current implementation uses WeChat Mini Program + Spring Cloud Gateway + independent Auth, Operation, and Analysis services. Historical single-service demo material has been removed so the repository follows the current distributed architecture only.

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

Formal Web admin (Phase 3 in progress):

```bash
cd web-admin
npm install
npm run dev
```

Open `http://127.0.0.1:5173`. The Vite development server proxies `/api` to the Gateway at port `8080`. The legacy static `web/` demo remains available until Phase 3 acceptance is complete.

## Demo Notes

Recommended reading order:

1. [docs/architecture-technical-overview.md](docs/architecture-technical-overview.md) - overall architecture, business logic, technical points, risks, and evolution path.
2. [docs/distributed-architecture.md](docs/distributed-architecture.md) - concrete service topology, ports, startup flow, cache flow, and demo accounts.
3. [docs/phase-1-acceptance-checklist.md](docs/phase-1-acceptance-checklist.md) - first-stage startup, demo flow, and acceptance checklist.
4. [docs/phase-2-business-closure-plan.md](docs/phase-2-business-closure-plan.md) - business closure plan for resources, applications, approvals, and audit logs.
5. [docs/phase-2-e2e-demo-script.md](docs/phase-2-e2e-demo-script.md) - end-to-end demo script for the second-stage business flow.
6. [docs/phase-3-reliability-baseline.md](docs/phase-3-reliability-baseline.md) - reliability and observability baseline for dashboard data freshness, cache status, and graceful degradation.
7. [docs/phase-4-production-readiness.md](docs/phase-4-production-readiness.md) - health checks, trace id propagation, and production-readiness operating notes.
8. [docs/phase-5-configuration-delivery.md](docs/phase-5-configuration-delivery.md) - environment examples, delivery boundaries, and handoff rules.
9. [docs/v1.2-roadmap.md](docs/v1.2-roadmap.md) - V1.2 phased roadmap, acceptance gates, and delivery boundaries.
10. [docs/internal-auth.md](docs/internal-auth.md) - internal service signing, Feign signing, nonce replay protection, and rollout rules.
11. [docs/delivery-checklist.md](docs/delivery-checklist.md) - final checklist for configuration, startup, tests, demo flow, and troubleshooting.
12. [backend/STARTUP.md](backend/STARTUP.md) - backend build and startup commands.
