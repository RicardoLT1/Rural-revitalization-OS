# Environment profiles and seed-data policy

V1.3 uses explicit Spring profiles to keep development, demonstration, test, and
production data from sharing the same initialization path. The base configuration and
the `dev`/`prod` profiles resolve only structural Flyway migrations. Rich sample data is
available only when the `demo` profile is active.

## Profile matrix

| Profile | Intended use | Infrastructure/data source | Flyway locations | Seed policy |
| --- | --- | --- | --- | --- |
| `dev` | Local backend development against disposable or developer-owned data | Local defaults may point at `docker-compose.demo.yml`, but the database is not populated automatically | `classpath:db/migration` | Schema only; no demo accounts or business records |
| `demo` | Contest/demo UI and acceptance walkthrough | Local demo MySQL, Redis, RabbitMQ, and Nacos | `classpath:db/migration`, then `classpath:db/demo` | Idempotent demo accounts and rich business data are applied |
| `test` | Maven/unit/integration tests | Test-owned in-memory stores, mocks, and per-test fixtures | Demo Flyway seeds are disabled | Every test creates the data it needs; no dependency on a running demo database |
| `prod` | Formal deployment | External endpoints and credentials supplied through environment variables or a secret manager | `classpath:db/migration` | Schema only; demo data is never resolved or injected |

Only one of `dev`, `demo`, or `prod` should be active for a running service. Never use a
comma-separated profile such as `prod,demo`. As a defensive guard, demo seed configuration
uses the activation expression `demo & !prod`, so the demo location is not enabled when
the production profile is also present.

## Migration layout

Each database-owning service follows this layout:

```text
src/main/resources/db/migration/   # versioned structural migrations
src/main/resources/db/demo/        # versioned, idempotent demo seeds
```

The existing demo seed version numbers were preserved when they were moved. Therefore,
an existing demo database whose Flyway history already contains those versions remains
valid when restarted with the `demo` profile. A new `prod` database cannot discover the
`db/demo` location and receives no demo records.

Do not reuse a database initialized with `demo` as a `dev` or `prod` database. Flyway will
correctly report the previously applied demo versions as unresolved when the demo location
is absent. Provision a separate schema/database for the target environment; do not delete
history rows or relabel a contaminated demo database as production.

Auth, Operation, and Analysis currently share one physical MySQL schema but use separate
Flyway history tables. All three set `baseline-version: 0`. This is required for a clean
deployment where another service may create the first table: the next service baselines
at version 0 and still executes its own V1 migration. Existing databases that already
have the corresponding history table are unaffected by this setting.

Do not put sample users, fixed contest records, visual dashboard history, or test fixtures
under `db/migration`. New demo-only data must use `db/demo`; automated tests should build
fixtures in test code or test resources.

## Starting each profile

The PowerShell scripts default to `demo`, matching the repository's demo Compose file:

```powershell
cd backend
.\scripts\start-auth.ps1 -Profile demo
.\scripts\start-operation.ps1 -Profile demo
.\scripts\start-analysis.ps1 -Profile demo
.\scripts\start-gateway.ps1 -Profile demo
```

For a schema-only developer database, pass `-Profile dev` to all four scripts. Maven
verification uses the isolated test profile:

```powershell
.\scripts\check-backend.ps1
```

The verification script first runs `check-profile-separation.ps1`. It rejects seed files
under the structural migration path, a non-zero shared-schema baseline, demo locations in
dev/test/prod, repository fallback values for required production secrets, and startup
commands that can retain stale migration resources.

Run this clean build before starting the services. `mvn clean` removes module `target`
directories used by the local `exec:java` launchers and should not be invoked as an
in-place health check against services that are meant to remain running.

For production, first export every required connection credential and security secret,
then pass `-Profile prod` to all four scripts. The prod profile intentionally has no
repository fallback for MySQL credentials, Redis credentials, RabbitMQ credentials, JWT
signing secrets, or internal-service signing secrets. Operation file storage also requires
an absolute deployment path through `XIANGYUN_UPLOAD_ROOT`.

The committed `.env.example` is a variable inventory, not a secret file and not an
automatic loader. Real production values must remain outside Git.

### First production administrator

Production never loads the demo account directory. On the first Auth startup only, set:

```text
AUTH_BOOTSTRAP_ENABLED=true
AUTH_BOOTSTRAP_USERNAME=admin
AUTH_BOOTSTRAP_PASSWORD=<at least 12 characters, supplied by a secret manager>
AUTH_BOOTSTRAP_DISPLAY_NAME=<administrator display name>
AUTH_BOOTSTRAP_VILLAGE_ID=1
```

The bootstrap inserts the administrator only when that username does not already exist;
it never resets or overwrites an existing account. After the first successful login,
restart Auth with `AUTH_BOOTSTRAP_ENABLED=false` and remove
`AUTH_BOOTSTRAP_PASSWORD` from the runtime environment. Future administrators should be
managed through the authenticated user-management flow, not by re-enabling bootstrap.

## Acceptance checks

Before a formal release:

1. Run `mvn clean test "-Dspring.profiles.active=test"` under `backend/`. The `clean`
   step is mandatory after migration files move between locations so stale resources under
   `target/classes` cannot bypass the profile boundary.
2. Start all services with `demo` and verify the documented demo accounts and dashboards.
3. Validate a clean database with `prod` configuration and confirm the demo usernames and
   fixed visual records are absent.
4. Confirm all production secrets are supplied externally and that no service started by
   falling back to repository demo credentials.
