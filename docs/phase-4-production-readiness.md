# 第四阶段：生产化运维与部署准备

## 阶段目标

第四阶段不追求复杂平台化能力，先补齐真实落地时最基础、最常用的运行保障能力：

- 服务是否正常启动能被脚本检查。
- 接口问题能通过请求追踪 ID 定位。
- 运行配置有清晰边界，不暴露不必要的监控信息。
- 启动、检查、排障流程有文档可依赖。

## 已实现范围

### 1. 服务健康检查

网关、认证、运营、分析四个服务已接入 Spring Boot Actuator，并只开放轻量端点：

- `/actuator/health`
- `/actuator/info`

健康详情不对外展开，避免暴露数据库、Redis、服务内部细节。

### 2. 一键服务健康检查脚本

新增脚本：

```powershell
backend/scripts/check-services-health.ps1
```

用于检查四个应用服务：

| 服务 | 地址 |
| --- | --- |
| Gateway | `http://127.0.0.1:8080/actuator/health` |
| Auth | `http://127.0.0.1:8081/actuator/health` |
| Operation | `http://127.0.0.1:8082/actuator/health` |
| Analysis | `http://127.0.0.1:8083/actuator/health` |

基础设施仍使用：

```powershell
backend/scripts/check-infra.ps1
```

### 3. 请求追踪 ID

网关统一生成并传递 `X-Trace-Id`：

- 写入下游服务请求头。
- 写回客户端响应头。
- CORS 暴露 `X-Trace-Id`，方便前端和联调工具读取。

排查接口问题时，只需要记录：

- 请求时间。
- 接口路径。
- 登录账号角色。
- 响应中的 `X-Trace-Id`。

### 4. 看板数据可观测响应头

网关 CORS 已暴露分析看板相关响应头：

- `X-Cache-Status`
- `X-Data-Stale`
- `X-Data-Generated-At`
- `X-Data-Range-Days`

这些信息服务于内部运营和联调，不面向普通用户展示。

### 5. 启动文档更新

`backend/STARTUP.md` 已补充：

- 服务健康检查命令。
- 健康检查端点列表。
- `X-Trace-Id` 排障说明。

## 验收标准

1. 四个服务能正常编译和测试。
2. 启动服务后，`check-services-health.ps1` 能检查四个服务健康状态。
3. 通过网关访问任意业务接口时，响应头包含 `X-Trace-Id`。
4. 分析看板响应头可以被前端或联调工具读取。
5. 健康检查只暴露 `health/info`，不开放复杂运行指标。

## 当前边界

本阶段暂不引入：

- Kubernetes。
- Prometheus / Grafana。
- ELK / Loki。
- OpenTelemetry。
- 自动告警平台。

这些能力需要稳定的部署环境和明确的运维目标后再进入，不适合当前阶段提前复杂化。

## 下一阶段建议

第五阶段建议进入“配置与交付规范”：

- 整理 `.env.example`。
- 区分 demo、local、production 的配置项。
- 明确敏感配置不得提交仓库。
- 补充最小化部署清单。
- 整理演示账号、接口地址、小程序环境变量。
