# 乡耘OS 前后端接口契约文档

## 1. 文档说明

本文档用于约定乡耘OS小程序前端与后端服务之间的数据接口。当前前端默认使用 mock 数据，后续切换真实后端时，前端 service 层应优先按本文档对接接口。

接口设计目标：

- 支撑首页驾驶舱、资源地图、资源详情、协同工作台、流程详情、智能报表、招商推荐、趋势预测页面。
- 与当前前端 `types/` 中的 `DashboardMetrics`、`ResourcePoint`、`WorkflowDetail`、`ReportDashboardView` 等类型保持一致。
- 为后续 Spring Boot 接口实现、联调验收和数据库落表提供统一字段标准。

## 2. 统一返回结构规范

### 2.1 通用成功返回

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | number | 是 | 业务状态码，成功固定为 200 |
| message | string | 是 | 响应说明 |
| data | T | 是 | 业务数据主体 |

### 2.2 列表返回

```json
{
  "code": 200,
  "message": "success",
  "data": []
}
```

适用于资源标签、风险列表、推荐列表等不需要分页的小列表。

### 2.3 分页返回

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "page": 1,
    "pageSize": 10,
    "total": 32
  }
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| list | array | 是 | 当前页数据 |
| page | number | 是 | 当前页码，从 1 开始 |
| pageSize | number | 是 | 每页数量 |
| total | number | 是 | 总记录数 |

### 2.4 错误返回

```json
{
  "code": 40001,
  "message": "参数错误",
  "errorDetail": "resourceId 不能为空"
}
```

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | number | 是 | 错误码 |
| message | string | 是 | 前端可展示的错误说明 |
| errorDetail | string | 否 | 面向开发排查的详细错误 |

## 3. 认证与通用请求约定

当前阶段小程序不做真实登录鉴权。正式联调时建议采用以下约定：

- 请求头预留 `Authorization: Bearer <token>`。
- 请求头预留 `X-Village-Id`，用于区分当前乡村或示范村。
- 时间字段统一使用 `YYYY-MM-DD HH:mm:ss`。
- 金额字段单位默认使用万元，字段名中不再重复写单位，说明文档中明确单位。
- 经纬度字段统一使用 `lat`、`lng`。
- 枚举字段使用稳定内部值，中文展示文案可由字典接口返回。

## 4. 首页驾驶舱接口

### 4.1 获取首页驾驶舱总览

- 接口名称：获取首页驾驶舱总览
- 请求方式：GET
- 接口路径：`/dashboard`
- 对应前端：`pages/dashboard/index`、`services/dashboard.getDashboardData`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| villageId | string | 否 | 乡村 ID，不传则取当前用户默认乡村 |

返回字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| villageName | string | 村庄名称 |
| roleName | string | 当前视角角色，如乡村CEO |
| stats | StatItem[] | 核心指标卡片 |
| trends | TrendSeries | 7天与30天趋势数据 |
| risks | RiskAlert[] | 风险预警列表 |
| suggestions | AiSuggestion[] | AI建议列表 |

`StatItem` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| key | string | 指标唯一键，如 flow、revenue |
| title | string | 指标标题 |
| value | number/string | 指标值 |
| unit | string | 单位 |
| delta | number | 环比变化值 |
| trend | up/down/flat | 趋势方向 |
| status | success/warning/danger/info/neutral | 状态样式 |
| icon | string | 前端展示图标文案 |

### 4.2 获取客流趋势数据

- 请求方式：GET
- 接口路径：`/dashboard/trends`
- 对应前端：`services/dashboard.getDashboardTrend`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| period | string | 是 | `7d` 或 `30d` |
| villageId | string | 否 | 乡村 ID |

返回 `TrendPoint[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| date | string | 日期或周期标签 |
| value | number | 客流数，单位人次 |

### 4.3 获取 AI 建议列表

- 请求方式：GET
- 接口路径：`/dashboard/suggestions`
- 对应前端：首页 AI 建议区

返回 `AiSuggestion[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 建议 ID |
| title | string | 建议标题 |
| content | string | 建议内容 |
| priority | P0/P1/P2/P3 | 优先级 |
| actionLabel | string | 操作按钮文案 |
| actionType | string | 跳转目标，如 forecast、match、process |
| tag | string | 建议类型标签 |

### 4.4 获取风险预警列表

- 请求方式：GET
- 接口路径：`/dashboard/risks`
- 对应前端：首页风险预警区

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| level | string | 否 | high、medium、low |
| status | string | 否 | open、processing、closed |

返回 `RiskAlert[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 风险 ID |
| title | string | 风险标题 |
| level | high/medium/low | 风险等级 |
| detail | string | 风险说明 |
| assignee | string | 责任组或责任人 |

## 5. 资源模块接口

### 5.1 获取资源地图点位列表

- 请求方式：GET
- 接口路径：`/resources/map-points`
- 对应前端：资源地图 marker 渲染

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| category | string | 否 | 资源类型 |
| investmentStatus | string | 否 | 招商状态 |
| tag | string | 否 | 资源标签 |

返回 `ResourcePoint[]`。点位字段同时可用于资源摘要卡：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 资源 ID |
| name | string | 资源名称 |
| category | string | 资源类型 |
| lat | number | 纬度 |
| lng | number | 经度 |
| address | string | 地址 |
| area | number | 面积，单位平方米 |
| annualEstimate | number | 预估年收益，单位万元 |
| investmentStatus | string | 招商状态 |
| tags | string[] | 标签列表 |

### 5.2 获取资源列表

- 请求方式：GET
- 接口路径：`/resources`
- 对应前端：`services/resource.getResources`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | number | 否 | 页码 |
| pageSize | number | 否 | 每页数量 |
| keyword | string | 否 | 搜索关键词 |
| category | string | 否 | 资源类型 |
| investmentStatus | string | 否 | 招商状态 |

建议正式接口使用分页返回。当前小程序演示可一次性返回全部。

### 5.3 获取资源详情

- 请求方式：GET
- 接口路径：`/resources/{id}`
- 对应前端：`services/resource.getResourceDetail`

返回 `ResourceDetail`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| intro | string | 资源介绍 |
| owner | string | 资源权属或运营主体 |
| contact | string | 联系方式 |
| relatedProjects | string[] | 关联项目 |
| occupancyRate | number | 当前利用率，百分比 |
| expectedROI | number | 预期回报率，百分比 |

其余基础字段继承 `ResourcePoint`。

### 5.4 新增资源

- 请求方式：POST
- 接口路径：`/resources`
- 使用场景：后台录入或后续小程序端资源录入

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | string | 是 | 资源名称 |
| category | string | 是 | 资源类型 |
| lat | number | 是 | 纬度 |
| lng | number | 是 | 经度 |
| address | string | 是 | 地址 |
| area | number | 是 | 面积 |
| investmentStatus | string | 是 | 招商状态 |
| intro | string | 否 | 资源介绍 |
| tags | string[] | 否 | 标签 |

### 5.5 编辑资源

- 请求方式：PUT
- 接口路径：`/resources/{id}`
- 请求体：同新增资源，可按后端规则支持部分更新。

### 5.6 获取资源标签字典

- 请求方式：GET
- 接口路径：`/resources/tags`
- 对应前端：`services/resource.getResourceTags`

返回：

```json
["全部", "闲置农房", "土地", "文旅空间", "可招商"]
```

后续可拆分为 `/dict/resource-categories`、`/dict/resource-statuses`、`/dict/resource-tags`。

## 6. 协同工作流接口

### 6.1 获取协同工作台总览

- 请求方式：GET
- 接口路径：`/workflows/workbench`
- 对应前端：`services/workflow.getCollabWorkbench`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| category | string | 否 | 全部、项目申报、资产流转、活动筹备、村民议事 |

返回 `CollabWorkbenchView`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| todoStats | TodoStats | 待办统计 |
| approvals | ApprovalItem[] | 审批事项 |
| workflowStrip | WorkflowNode[] | 节点概览 |
| workflowStripCurrent | string | 当前节点 ID |
| messages | WorkflowMessage[] | 消息提醒 |
| categoryOptions | OptionItem[] | 分类筛选 |
| filteredTodos | TodoViewItem[] | 当前分类待办 |

### 6.2 获取待办事项列表

- 请求方式：GET
- 接口路径：`/workflows/todos`

请求参数：`category`、`status`、`page`、`pageSize`。

返回 `TodoItem[]` 或分页结构：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 待办 ID |
| title | string | 待办标题 |
| dueDate | string | 截止时间 |
| category | string | 流程类型 |
| status | string | 待处理、进行中、已逾期、已完成 |
| processId | string | 关联流程 ID |

### 6.3 获取审批事项列表

- 请求方式：GET
- 接口路径：`/workflows/approvals`

返回 `ApprovalItem[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 审批 ID |
| title | string | 审批标题 |
| applicant | string | 申请人 |
| amount | number | 金额，单位万元 |
| status | string | 待审批、已驳回、已通过 |
| processId | string | 关联流程 ID |
| time | string | 提交或更新时间 |

### 6.4 获取流程详情

- 请求方式：GET
- 接口路径：`/workflows/processes/{id}`
- 对应前端：`services/workflow.getProcessDetail`

返回 `WorkflowDetail`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 流程 ID |
| title | string | 流程标题 |
| status | string | 流程状态 |
| currentNodeId | string | 当前节点 ID |
| blocker | string | 当前阻塞说明 |
| nodes | WorkflowNode[] | 节点列表 |
| records | ProcessRecord[] | 操作记录 |
| archive | ArchiveRecord | 归档信息 |

### 6.5 获取流程时间轴/节点记录

- 请求方式：GET
- 接口路径：`/workflows/processes/{id}/records`

请求参数：`nodeId`、`filter=all|current`。

返回 `ProcessRecord[]`。

### 6.6 提交流程处理结果

- 请求方式：POST
- 接口路径：`/workflows/processes/{id}/actions`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| action | string | 是 | pass、reject、supplement、comment |
| nodeId | string | 是 | 当前节点 ID |
| remark | string | 否 | 处理备注 |
| attachments | string[] | 否 | 附件 URL |

返回：更新后的 `WorkflowDetail` 或处理记录。

### 6.7 获取归档记录

- 请求方式：GET
- 接口路径：`/workflows/archives`
- 请求参数：`processId`、`category`、`page`、`pageSize`。

### 6.8 获取流程类型字典

- 请求方式：GET
- 接口路径：`/workflows/categories`

返回 `OptionItem[]`。

## 7. 智能报表接口

### 7.1 获取智能报表总览

- 请求方式：GET
- 接口路径：`/reports/dashboard`
- 对应前端：`services/report.getReportDashboard`

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| period | string | 否 | `7d` 或 `30d` |

返回 `ReportDashboardView`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| summary | ReportSummary[] | 顶部指标摘要 |
| periods | OptionItem[] | 时间维度 |
| period | string | 当前维度 |
| flowPoints | TrendPoint[] | 客流折线图数据 |
| revenueBar | RevenueBarData | 营收柱状图数据 |
| ratioRing | RevenueRatioData | 收入结构环形图数据 |
| autoSummary | string | 自动摘要 |
| aiTips | AiSuggestion[] | 智能建议 |

### 7.2 获取报表摘要

- 请求方式：GET
- 接口路径：`/reports/summary`

返回 `ReportSummary[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 指标 ID |
| title | string | 指标名称 |
| value | string | 展示值，含单位 |
| delta | string | 增长变化 |

### 7.3 获取客流趋势图数据

- 请求方式：GET
- 接口路径：`/reports/visitor-trends`
- 参数：`period`。
- 返回 `TrendPoint[]`。

### 7.4 获取营收对比图数据

- 请求方式：GET
- 接口路径：`/reports/revenue-bars`

返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| labels | string[] | 横轴标签 |
| series | ChartSeries[] | 柱状图系列 |

### 7.5 获取收入结构图数据

- 请求方式：GET
- 接口路径：`/reports/revenue-ratio`

返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| labels | string[] | 收入类型 |
| values | number[] | 占比值 |
| colors | string[] | 前端建议色值，可选 |

### 7.6 获取自动摘要

- 请求方式：GET
- 接口路径：`/reports/auto-summary`

返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| summary | string | 自动摘要内容 |

### 7.7 获取智能建议

- 请求方式：GET
- 接口路径：`/reports/suggestions`

返回 `AiSuggestion[]`。

## 8. 招商推荐与趋势预测接口

### 8.1 获取招商推荐列表

- 请求方式：GET
- 接口路径：`/resources/{resourceId}/investment-matches`
- 对应前端：`services/resource.getInvestmentMatches`

返回 `InvestmentMatch[]`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | string | 推荐记录 ID |
| investor | string | 推荐对象名称 |
| score | number | 匹配分 |
| reason | string | 推荐理由 |
| priority | string | 高优先、中优先、观察 |
| direction | string | 推荐方向 |

### 8.2 获取招商推荐视图

- 请求方式：GET
- 接口路径：`/reports/investment-match-view`
- 参数：`resourceId`。
- 对应前端：`services/report.getInvestmentMatchView`

返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| resource | ResourceDetail | 资源摘要 |
| matches | InvestmentMatchViewItem[] | 推荐对象列表 |
| aiSummary | AiSuggestion | 招商策略摘要 |

### 8.3 获取趋势预测结果

- 请求方式：GET
- 接口路径：`/reports/forecast`
- 对应前端：`services/report.getForecastView`

返回 `ForecastResult`：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| forecastData | ForecastPoint[] | 预测数据 |
| band | ForecastBand | 置信区间摘要 |
| risks | RiskAlert[] | 风险提示 |
| strategies | string[] | 策略建议 |

`ForecastPoint` 字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| date | string | 日期 |
| actual | number | 实际值，可选 |
| predict | number | 预测值 |
| upper | number | 置信上界 |
| lower | number | 置信下界 |

### 8.4 获取建议优先级字典

- 请求方式：GET
- 接口路径：`/dict/suggestion-priorities`

返回：`P0`、`P1`、`P2`、`P3` 与中文展示文案。

## 9. 错误码与异常处理建议

| 错误码 | 含义 | 前端处理建议 |
| --- | --- | --- |
| 200 | 成功 | 正常渲染 |
| 40001 | 参数错误 | 展示错误提示，保留当前页面 |
| 40100 | 未登录或登录失效 | 后续接入登录后跳转登录页 |
| 40300 | 权限不足 | 展示无权限提示 |
| 40400 | 数据不存在 | 进入 empty 状态 |
| 40900 | 重复提交 | toast 提示，不重复请求 |
| 40910 | 当前业务状态不允许操作 | 展示业务状态提示 |
| 50000 | 服务器异常 | 进入 error 状态，可重试 |

## 10. 前后端字段映射说明

- 前端当前字段采用小驼峰：`annualEstimate`、`investmentStatus`、`currentNodeId`。
- 后端 Java 实体可使用小驼峰，数据库字段建议使用下划线。
- 后端接口返回 JSON 应保持小驼峰，避免前端写额外 mapper。
- 图表数据建议直接返回前端可消费结构：折线为 `TrendPoint[]`，柱状图为 `{ labels, series }`，环形图为 `{ labels, values, colors }`。
- 如果后端字段与当前前端类型不一致，应优先在 service 层新增 adapter，不建议页面直接转换字段。
