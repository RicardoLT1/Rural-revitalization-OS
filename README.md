# 乡耘OS（微信小程序原型）

## 项目说明
- 框架：微信小程序原生框架
- 语言：TypeScript
- 样式：SCSS（全局 Design Token）
- 数据：本地 mock（无后端）

## 页面结构
- 主 Tab：`dashboard`、`resource-map`、`collab`、`report`
- 详情页：`resource-detail`、`process-detail`、`investment-match`、`forecast`

## 目录
- 小程序根目录：`miniprogram/`
- 组件目录：`miniprogram/components/`
- 页面目录：`miniprogram/pages/`
- Mock 数据：`miniprogram/mock/`
- 工具与导航：`miniprogram/utils/`

## 运行方式
1. 微信开发者工具打开项目根目录。
2. 使用 `project.config.json` 中的 `miniprogramRoot = miniprogram/`。
3. 直接编译并预览 8 个页面演示链路。

## 说明
- 图表能力统一通过 `utils/chart.ts` 输出配置，并由 `chart-card` 组件统一渲染。
- 地图能力使用原生 `map` 组件，资源筛选与 marker 联动在 `utils/map.ts` 中处理。
