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

Formal Web admin (Phase 3 accepted, V1.2 productization complete):

```bash
cd web-admin
npm install
npm run dev
```

Open `http://127.0.0.1:5173`. The Vite development server proxies `/api` to the Gateway at port `8080`. The legacy static `web/` demo remains only as a rollback fallback until an explicit retirement decision is recorded.

The current Web Admin supports approval detail decisions, resource archives and activity timelines, a real-coordinate resource map, dashboard drilldown, notifications, server-side global search, CSV export, and role-aware read-only states. The verified productization checkpoint is `v1.2-web-admin-productized` (`74c8c85`). V1.3 Admin Pro on `feature/v1.3-admin-pro` now includes hard permissions, real village-scoped metrics, unified pagination, security and change auditing, authenticated resource-material management, village-scoped federated search, audited resource batch actions with partial-result reporting, persistent account notifications, village-level system settings, and a security-aware personal center. The map remains an intentionally lightweight village distribution view; full GIS, production object storage, external notification channels, and automated E2E remain follow-up capabilities.

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
10. [docs/v1.3-admin-pro.md](docs/v1.3-admin-pro.md) - Admin Pro release split, permission matrix, RC1 status, and next implementation order.
11. [docs/internal-auth.md](docs/internal-auth.md) - internal service signing, Feign signing, nonce replay protection, and rollout rules.
12. [docs/outbox-rabbitmq.md](docs/outbox-rabbitmq.md) - workflow events, Outbox/Inbox reliability, RabbitMQ topology, and operating notes.
13. [docs/delivery-checklist.md](docs/delivery-checklist.md) - final checklist for configuration, startup, tests, demo flow, and troubleshooting.
14. [backend/STARTUP.md](backend/STARTUP.md) - backend build and startup commands.
