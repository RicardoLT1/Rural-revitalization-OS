# 第一阶段收口验收清单

## 1. 阶段目标

第一阶段目标是让当前分布式版本稳定、清晰、可演示。重点不是继续增加新架构，而是把现有主线收紧：

- 文档只描述当前分布式架构。
- 后端只保留当前 Maven 多模块主线。
- 小程序默认走 Gateway API。
- 后端返回文本不出现乱码。
- 核心演示链路可以按固定顺序跑通。
- 自动化测试保持通过。

## 2. 当前架构主线

当前正式架构为：

```text
微信小程序
-> Spring Cloud Gateway
-> Auth Service / Operation Service / Analysis Service
-> Redis / MySQL / Nacos
```

当前后端模块：

- `xiangyun-common`
- `xiangyun-gateway`
- `xiangyun-auth-service`
- `xiangyun-operation-service`
- `xiangyun-analysis-service`

不再维护旧单体 `backend/src` 代码。

## 3. 启动顺序

### 3.1 启动基础设施

```powershell
docker compose -f docker-compose.demo.yml up -d
```

### 3.2 检查基础设施

```powershell
cd backend
.\scripts\check-infra.ps1
```

### 3.3 启动后端服务

分别打开四个终端，在 `backend/` 下执行：

```powershell
.\scripts\start-auth.ps1
.\scripts\start-operation.ps1
.\scripts\start-analysis.ps1
.\scripts\start-gateway.ps1
```

推荐顺序：

1. Auth Service
2. Operation Service
3. Analysis Service
4. Gateway

## 4. 演示主线

建议按以下顺序演示：

1. 登录小程序。
2. 进入首页看板，展示资源数、可合作资源、待审批数和风险流程。
3. 进入资源地图，查看资源点位。
4. 打开资源详情，查看招商状态、资源介绍和推荐信息。
5. 提交合作申请。
6. 进入“我的申请”，查看申请状态。
7. 使用 STAFF 或 ADMIN 账号进入协同工作台，处理审批。
8. 打开智能报表，展示客流、营收、趋势和 AI 建议。
9. 打开趋势预测，展示预测曲线和运营策略。

## 5. 验收项

### 5.1 文档

- `README.md` 指向当前架构文档。
- `docs/architecture-technical-overview.md` 描述整体架构和演进路线。
- `docs/distributed-architecture.md` 描述服务拓扑、端口和核心调用链路。
- `backend/STARTUP.md` 描述后端启动方式。

### 5.2 后端

- Gateway 端口为 `8080`。
- Auth Service 端口为 `8081`。
- Operation Service 端口为 `8082`。
- Analysis Service 端口为 `8083`。
- `/api/internal/**` 不允许外部直接访问。
- 登录后请求会携带 `Authorization: Bearer <token>`。
- Gateway 会校验 JWT 和 Redis session。
- Gateway 会注入 `X-User-*` 用户上下文头。

### 5.3 小程序

- `miniprogram/config/env.ts` 中 `dataSource` 为 `api`。
- `baseURL` 指向 `http://127.0.0.1:8080/api`。
- 401 会清理本地登录态并回到登录页。
- 网络不可达时给出明确提示。

### 5.4 数据展示

- 首页看板中文显示正常。
- 报表页中文显示正常。
- 趋势预测策略中文显示正常。
- 后端响应中不应出现历史乱码文本。
- Analysis 返回 `X-Cache-Status`。
- 返回兜底缓存时应带 `X-Data-Stale: true`。

### 5.5 自动化测试

每次收口后运行：

```powershell
cd backend
mvn test
```

验收标准：

```text
BUILD SUCCESS
Failures: 0
Errors: 0
```

## 6. 第一阶段完成标准

满足以下条件即可认为第一阶段完成：

- 旧单体代码和旧口径文档已清理。
- 当前架构文档和 README 入口一致。
- 后端全量测试通过。
- 业务响应中没有已知乱码输出。
- 小程序主演示链路具备稳定演示条件。

第一阶段完成后，再进入第二阶段：资源档案、合作申请、审批状态机和审计日志的真实业务闭环建设。
