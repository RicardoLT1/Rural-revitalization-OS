# 交付检查清单

## 1. 代码与配置

- [ ] `.env` 只在本地保存，不提交仓库。
- [ ] `.env.example` 中没有真实生产密钥。
- [ ] `miniprogram/config/env.ts` 的 `baseURL` 指向当前 Gateway。
- [ ] `JWT_SECRET` 在非演示环境中已替换为强密钥。
- [ ] MySQL、Redis、Nacos 地址与当前环境一致。

## 2. 后端启动

- [ ] 已启动 MySQL、Redis、Nacos。
- [ ] `backend/scripts/check-infra.ps1` 检查通过。
- [ ] Auth、Operation、Analysis、Gateway 四个服务均已启动。
- [ ] `backend/scripts/check-services-health.ps1` 检查通过。

## 3. 自动化验证

```powershell
cd backend
mvn test
```

验收要求：

- [ ] 全后端测试 `BUILD SUCCESS`。
- [ ] Gateway 测试覆盖鉴权与 `X-Trace-Id`。
- [ ] Operation 测试覆盖申请、审批、补材料、操作日志。
- [ ] Analysis 测试覆盖缓存命中、缓存未命中、降级返回。

## 4. 演示账号

| 账号 | 密码 | 角色 | 用途 |
| --- | --- | --- | --- |
| `user_demo` | `123456` | USER | 小程序用户提交合作申请 |
| `staff_demo` | `123456` | STAFF | 工作人员处理审批和补材料 |
| `admin` | `123456` | ADMIN | 管理员刷新看板缓存和管理功能 |

## 5. 演示主流程

- [ ] 用户登录小程序。
- [ ] 浏览资源详情。
- [ ] 提交合作申请。
- [ ] STAFF/ADMIN 在协同工作台看到待处理申请。
- [ ] 执行通过、驳回或要求补材料。
- [ ] 用户在流程详情页看到审批记录和操作日志。
- [ ] Analysis 看板能展示业务指标。

## 6. 排障信息

出现接口问题时至少记录：

- 请求时间。
- 当前账号。
- 请求接口路径。
- HTTP 状态码。
- 响应头 `X-Trace-Id`。
- 后端服务控制台错误日志。
