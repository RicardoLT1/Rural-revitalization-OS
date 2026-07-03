# 乡耘OS 前端 Service 与未来 API 映射说明

## 1. 文档说明

本文档说明当前小程序 `miniprogram/services/` 与未来后端 API 的映射关系。后续从 mock 模式切换到 api 模式时，优先在 service 层完成接口替换和字段适配，页面层不应直接处理后端字段差异。

当前模式由 `miniprogram/config/env.ts` 控制：

| 配置 | 说明 |
| --- | --- |
| dataSource | `mock` 或 `api`，默认 `mock` |
| baseURL | 后端接口基础地址 |
| timeout | 请求超时时间 |
| enableDebugLog | 调试日志开关 |

## 2. dashboard service

文件：`miniprogram/services/dashboard.ts`

| 当前方法 | 返回类型 | 未来 API | 当前字段一致性 | 说明 |
| --- | --- | --- | --- | --- |
| getDashboardData | DashboardMetrics | GET `/dashboard` | 基本一致 | 首页总览一次性接口 |
| getDashboardTrend | TrendPoint[] | GET `/dashboard/trends?period=7d|30d` | 一致 | 当前从总览数据中取 trends，未来可独立请求 |
| getDashboardPeriods | OptionItem[] | 前端常量或 GET `/dict/report-periods` | 一致 | 当前无需后端接口 |

适配建议：

- 如果后端将指标拆成多个接口，可在 `getDashboardData` 中聚合，页面不变。
- 如果后端返回数据库下划线字段，应在 service 或 adapter 中转换为小驼峰。

## 3. resource service

文件：`miniprogram/services/resource.ts`

| 当前方法 | 返回类型 | 未来 API | 当前字段一致性 | 说明 |
| --- | --- | --- | --- | --- |
| getResourceTags | string[] | GET `/resources/tags` | 一致 | 资源筛选标签 |
| getResources | ResourcePoint[] | GET `/resources` | 基本一致 | 资源列表，可扩展分页 |
| getResourceDetail | ResourceDetail | GET `/resources/{id}` | 一致 | 资源详情页 |
| getInvestmentMatches | InvestmentMatch[] | GET `/resources/{resourceId}/investment-matches` | 一致 | 招商推荐基础数据 |
| getInvestmentStatusType | StatusType | 前端常量 | 一致 | 展示样式映射，不建议后端处理 |
| getResourceMapView | ResourceMapView | 聚合 `tags + resources` | 前端聚合 | 给资源地图页使用的 view model |

适配建议：

- 后端可以先实现 `/resources` 与 `/resources/{id}`，地图点位可复用资源列表字段。
- 若正式列表分页，`getResourceMapView` 需要按地图页需求决定是否请求全量点位接口 `/resources/map-points`。
- `ResourceMapView` 是前端 view model，不建议后端直接返回。

## 4. workflow service

文件：`miniprogram/services/workflow.ts`

| 当前方法 | 返回类型 | 未来 API | 当前字段一致性 | 说明 |
| --- | --- | --- | --- | --- |
| getCollabWorkbench | CollabWorkbenchView | GET `/workflows/workbench` | 基本一致 | 协同工作台聚合接口 |
| getProcessDetail | ProcessDetail | GET `/workflows/processes/{id}` | 一致 | 流程详情 |
| getProcessStatusType | StatusType | 前端常量 | 一致 | 根据 blocker 转样式 |
| getProcessRecords | ProcessRecord[] | GET `/workflows/processes/{id}/records` | 一致 | 当前本地过滤，未来可服务端过滤 |

适配建议：

- 工作台可以先做聚合接口，减少小程序首屏请求数。
- 流程处理动作建议新增 POST `/workflows/processes/{id}/actions`，当前页面仍是演示态 toast。
- `TodoViewItem.statusClass` 属于前端样式字段，可继续在 service 中补充。

## 5. report service

文件：`miniprogram/services/report.ts`

| 当前方法 | 返回类型 | 未来 API | 当前字段一致性 | 说明 |
| --- | --- | --- | --- | --- |
| getReportPeriods | OptionItem[] | 前端常量或 GET `/dict/report-periods` | 一致 | 时间维度 |
| getReportDashboard | ReportDashboardView | GET `/reports/dashboard?period=7d|30d` | 基本一致 | 智能报表聚合接口 |
| getForecastView | ForecastResult | GET `/reports/forecast` | 一致 | 趋势预测页 |
| getInvestmentMatchView | InvestmentMatchView | GET `/reports/investment-match-view?resourceId=` | 前端聚合 | 当前由资源详情和推荐列表组合 |

适配建议：

- `ReportDashboardView` 可由后端一次性返回，适合报表页首屏。
- 若后端拆分摘要、折线、柱状、环形、建议接口，service 层可并发请求后组装。
- `getInvestmentMatchView` 目前跨 resource/report 两个领域，正式后端可提供聚合接口，也可保持前端组合。

## 6. mock 字段与未来接口字段一致性

当前 mock 字段整体采用前端小驼峰结构，建议后端接口也返回小驼峰 JSON：

| 前端字段 | 数据库建议字段 | 说明 |
| --- | --- | --- |
| annualEstimate | annual_estimate | 预估年收益 |
| investmentStatus | investment_status | 招商状态 |
| currentNodeId | current_node_id | 当前流程节点 |
| occupancyRate | occupancy_rate | 资源利用率 |
| expectedROI | expected_roi | 预期回报率 |
| forecastData | forecast_data | 预测数据 |

## 7. adapter / mapper 建议

当前阶段不需要新增独立 adapter 目录，原因：

- 页面已经只调用 service。
- service 中的数据整理较轻量。
- mock 与未来接口字段建议保持一致。

建议在以下场景再新增 `miniprogram/adapters/`：

- 后端字段无法保持小驼峰。
- 一个页面需要组合 3 个以上接口。
- 图表字段需要在多个 service 中重复转换。
- 后端分页结构与前端列表结构差异较大。

## 8. 第四阶段联调建议顺序

建议优先实现以下接口：

1. GET `/dashboard`
2. GET `/resources`
3. GET `/resources/{id}`
4. GET `/workflows/workbench`
5. GET `/reports/dashboard`

完成以上 5 个接口后，四个主 Tab 和资源详情页即可进入真实数据联调。
