# 乡耘OS 数据库设计初稿

## 1. 数据库设计说明

本文档为后续 MySQL 与 Spring Boot 后端实现提供数据库设计初稿。当前项目仍处于 mock 演示阶段，本文档不要求立即建库，但表结构应能支撑后续正式接口、管理后台与小程序联调。

通用约定：

- 主键建议使用 `BIGINT` 自增或雪花 ID，接口层统一转为字符串。
- 所有业务表建议包含 `created_at`、`updated_at`、`deleted`。
- 状态字段建议保存稳定枚举值，中文展示由前端或字典接口处理。
- 金额类字段当前单位建议为万元，生产环境可按分存储以避免精度问题。

## 2. 主要业务实体关系概览

- `village` 是核心业务空间，资源、流程、报表快照、预测结果均归属某个乡村。
- `resource` 是资源数字孪生主表，通过 `resource_tag_rel` 关联多个标签。
- `workflow` 是流程主表，`workflow_node`、`todo_item`、`approval_record`、`archive_record` 均围绕流程展开。
- `report_snapshot` 存储经营指标快照，供首页和报表页读取。
- `forecast_result` 存储预测结果快照，供趋势预测页读取。
- `investment_match_record` 存储资源与投资方匹配结果，供招商推荐页读取。

## 3. 用户与角色表设计

### 3.1 user

表用途：保存小程序用户、乡村运营团队成员、后续后台用户基础信息。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 用户 ID |
| openid | VARCHAR(64) | 微信 openid |
| nickname | VARCHAR(64) | 昵称 |
| real_name | VARCHAR(64) | 真实姓名 |
| phone | VARCHAR(32) | 手机号 |
| avatar_url | VARCHAR(255) | 头像 |
| village_id | BIGINT | 默认所属乡村 |
| status | VARCHAR(32) | active、disabled |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

关系：`user.village_id` 关联 `village.id`；用户与角色通过 `user_role_rel` 多对多关联。

### 3.2 role

表用途：保存角色字典，如乡村CEO、运营专员、审批人员、管理员。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 角色 ID |
| code | VARCHAR(64) | 角色编码 |
| name | VARCHAR(64) | 角色名称 |
| description | VARCHAR(255) | 角色说明 |
| status | VARCHAR(32) | active、disabled |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 3.3 user_role_rel

表用途：保存用户与角色关系。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 关系 ID |
| user_id | BIGINT | 用户 ID |
| role_id | BIGINT | 角色 ID |
| village_id | BIGINT | 角色生效乡村 |
| created_at | DATETIME | 创建时间 |

关系：`user_id` 关联 `user.id`，`role_id` 关联 `role.id`。

## 4. 基础业务主体

### 4.1 village

表用途：保存乡村或示范村基础信息。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 乡村 ID |
| name | VARCHAR(128) | 乡村名称 |
| region_code | VARCHAR(32) | 行政区划编码 |
| address | VARCHAR(255) | 地址 |
| lat | DECIMAL(10,6) | 中心纬度 |
| lng | DECIMAL(10,6) | 中心经度 |
| manager_user_id | BIGINT | 负责人用户 ID |
| description | TEXT | 简介 |
| status | VARCHAR(32) | active、archived |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

关系：资源、流程、报表均通过 `village_id` 归属该表。

## 5. 资源模块表设计

### 5.1 resource

表用途：资源数字孪生主表，支撑资源地图、资源详情、招商推荐。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 资源 ID |
| village_id | BIGINT | 所属乡村 |
| name | VARCHAR(128) | 资源名称 |
| category | VARCHAR(64) | 资源类型，如闲置农房、土地、文旅空间 |
| investment_status | VARCHAR(64) | 可招商、洽谈中、已签约 |
| address | VARCHAR(255) | 资源地址 |
| lat | DECIMAL(10,6) | 纬度 |
| lng | DECIMAL(10,6) | 经度 |
| area | DECIMAL(12,2) | 面积，单位平方米 |
| annual_estimate | DECIMAL(12,2) | 预估年收益，单位万元 |
| intro | TEXT | 资源介绍 |
| owner | VARCHAR(128) | 权属或运营主体 |
| contact | VARCHAR(128) | 联系方式 |
| occupancy_rate | DECIMAL(5,2) | 当前利用率 |
| expected_roi | DECIMAL(5,2) | 预期回报率 |
| status | VARCHAR(32) | active、offline、archived |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

关系：`village_id` 关联 `village.id`；通过 `resource_tag_rel` 关联标签。

设计说明：

- 地图点位信息直接保存在 `resource.lat`、`resource.lng`。
- 资源详情页字段大部分由主表直接提供。
- 招商状态使用 `investment_status`，后续可扩展为状态流转记录表。

### 5.2 resource_tag

表用途：资源标签字典表。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 标签 ID |
| code | VARCHAR(64) | 标签编码 |
| name | VARCHAR(64) | 标签名称 |
| tag_type | VARCHAR(64) | 类型，如资源类型、招商标签、设施标签 |
| sort_order | INT | 排序 |
| status | VARCHAR(32) | active、disabled |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 5.3 resource_tag_rel

表用途：资源与标签多对多关系。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 关系 ID |
| resource_id | BIGINT | 资源 ID |
| tag_id | BIGINT | 标签 ID |
| created_at | DATETIME | 创建时间 |

关系：`resource_id` 关联 `resource.id`，`tag_id` 关联 `resource_tag.id`。

## 6. 流程协同表设计

### 6.1 workflow

表用途：流程主表，支撑流程详情、审批、协同工作台。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 流程 ID |
| village_id | BIGINT | 所属乡村 |
| title | VARCHAR(255) | 流程标题 |
| category | VARCHAR(64) | 项目申报、资产流转、活动筹备、村民议事 |
| status | VARCHAR(64) | draft、processing、completed、rejected、archived |
| current_node_id | BIGINT | 当前节点 ID |
| applicant_user_id | BIGINT | 发起人 |
| blocker | VARCHAR(255) | 当前阻塞说明 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

### 6.2 workflow_node

表用途：保存流程节点定义与执行状态。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 节点 ID |
| workflow_id | BIGINT | 流程 ID |
| node_code | VARCHAR(64) | 节点编码 |
| name | VARCHAR(128) | 节点名称 |
| owner | VARCHAR(128) | 责任角色或责任人 |
| status | VARCHAR(32) | done、doing、pending、blocked |
| sort_order | INT | 节点顺序 |
| handled_at | DATETIME | 处理时间 |
| remark | VARCHAR(500) | 节点备注 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

关系：`workflow_id` 关联 `workflow.id`。

### 6.3 todo_item

表用途：保存待办事项，支撑协同工作台待办列表。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 待办 ID |
| workflow_id | BIGINT | 关联流程 |
| node_id | BIGINT | 关联节点 |
| assignee_user_id | BIGINT | 待办处理人 |
| title | VARCHAR(255) | 待办标题 |
| category | VARCHAR(64) | 待办分类 |
| due_date | DATETIME | 截止时间 |
| status | VARCHAR(32) | pending、processing、overdue、done |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 6.4 approval_record

表用途：保存审批与处理记录，支撑流程留痕。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 记录 ID |
| workflow_id | BIGINT | 流程 ID |
| node_id | BIGINT | 节点 ID |
| operator_user_id | BIGINT | 操作人 |
| action | VARCHAR(64) | submit、pass、reject、supplement、comment |
| amount | DECIMAL(12,2) | 涉及金额，单位万元，可选 |
| remark | VARCHAR(1000) | 处理备注 |
| attachment_urls | TEXT | 附件 URL，JSON 字符串 |
| created_at | DATETIME | 操作时间 |

### 6.5 archive_record

表用途：流程归档留痕信息。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 归档 ID |
| workflow_id | BIGINT | 流程 ID |
| archive_code | VARCHAR(64) | 归档编号 |
| archived_at | DATETIME | 归档时间 |
| archived_by | BIGINT | 归档人，系统归档可为空 |
| note | VARCHAR(1000) | 归档说明 |
| file_urls | TEXT | 归档文件 URL，JSON 字符串 |
| created_at | DATETIME | 创建时间 |

## 7. 报表与分析表设计

### 7.1 report_snapshot

表用途：保存运营指标快照，支撑首页和智能报表。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 快照 ID |
| village_id | BIGINT | 所属乡村 |
| snapshot_date | DATE | 快照日期 |
| visitor_count | INT | 客流数 |
| revenue_amount | DECIMAL(12,2) | 营收，单位万元 |
| project_progress | DECIMAL(5,2) | 项目进度百分比 |
| risk_count | INT | 风险数量 |
| revenue_culture | DECIMAL(12,2) | 文旅收入 |
| revenue_product | DECIMAL(12,2) | 农产品销售收入 |
| revenue_service | DECIMAL(12,2) | 活动服务收入 |
| auto_summary | TEXT | 自动摘要 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

设计说明：

- 首页实时指标可优先读取当日快照，后续再引入实时计算。
- 智能报表的折线图、柱状图、环形图可由该表按日期聚合生成。

### 7.2 forecast_result

表用途：保存趋势预测结果快照。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 预测结果 ID |
| village_id | BIGINT | 所属乡村 |
| forecast_date | DATE | 预测日期 |
| target_date | DATE | 预测目标日期 |
| actual_value | INT | 实际值，可为空 |
| predict_value | INT | 预测值 |
| upper_value | INT | 置信上界 |
| lower_value | INT | 置信下界 |
| confidence | VARCHAR(32) | 置信度 |
| strategy_text | TEXT | 策略建议，JSON 或换行文本 |
| risk_text | TEXT | 风险说明，JSON 或换行文本 |
| created_at | DATETIME | 创建时间 |

### 7.3 investment_match_record

表用途：保存资源与招商对象匹配结果快照。

| 字段 | 类型建议 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 匹配记录 ID |
| resource_id | BIGINT | 资源 ID |
| investor_name | VARCHAR(128) | 投资方或招商对象 |
| score | DECIMAL(5,2) | 匹配分 |
| priority | VARCHAR(32) | 高优先、中优先、观察 |
| direction | VARCHAR(128) | 推荐方向 |
| reason | VARCHAR(1000) | 推荐理由 |
| status | VARCHAR(32) | pending、contacted、converted、closed |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

关系：`resource_id` 关联 `resource.id`。

## 8. 表之间关系说明

- `village 1 - n resource`
- `village 1 - n workflow`
- `village 1 - n report_snapshot`
- `resource n - n resource_tag`
- `workflow 1 - n workflow_node`
- `workflow 1 - n todo_item`
- `workflow 1 - n approval_record`
- `workflow 1 - 1 archive_record`
- `resource 1 - n investment_match_record`

## 9. 后续扩展建议

- 后续如需要完整权限体系，可补充 `permission`、`role_permission_rel`。
- 后续如需要资源图片，可新增 `resource_media`。
- 后续如需要流程配置器，可新增 `workflow_template`、`workflow_template_node`。
- 后续如需要 AI 结果追溯，可新增 `ai_task`、`ai_result_log`。
- 后续如接入真实 BI，可将 `report_snapshot` 拆分为事实表与维度表。
