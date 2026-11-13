---
name: excel-table-custom-columns
overview: 为 Excel 导出增加后端表级扩展点，支持按表追加自定义列且列值由 Java 处理器定义；保持现有列、前端与 /export 接口不变，并在 tk_usp_receivingboxentry 提供可运行示例。
design:
  architecture:
    framework: vue
  styleKeywords:
    - 企业后台
    - 卡片式布局
    - 信息密度优先
    - 扁平轻阴影
    - 蓝色主色
  fontSystem:
    fontFamily: PingFang SC
    heading:
      size: 20px
      weight: 600
    subheading:
      size: 15px
      weight: 500
    body:
      size: 13px
      weight: 400
  colorSystem:
    primary:
      - "#409EFF"
      - "#67C23A"
    background:
      - "#F5F7FA"
      - "#FFFFFF"
    text:
      - "#303133"
      - "#606266"
      - "#909399"
    functional:
      - "#67C23A"
      - "#E6A23C"
      - "#F56C6C"
todos:
  - id: verify-current-state
    content: 用 [subagent:code-explorer] 核对批量链路与前端现状
    status: pending
  - id: backend-batch-aggregate
    content: 改造 queryBatch 返回批次汇总对象
    status: pending
    dependencies:
      - verify-current-state
  - id: backend-merged-export
    content: 改造 exportBatch 按表合并并标识来源单据
    status: pending
    dependencies:
      - backend-batch-aggregate
  - id: frontend-flat-display
    content: 前端平铺展示所有单据与汇总数量
    status: pending
    dependencies:
      - backend-batch-aggregate
  - id: update-tests
    content: 更新批量查询与导出集成测试断言
    status: pending
    dependencies:
      - backend-batch-aggregate
      - backend-merged-export
  - id: run-verification
    content: 运行 Maven 测试与静态检查验证
    status: pending
    dependencies:
      - backend-batch-aggregate
      - backend-merged-export
      - frontend-flat-display
      - update-tests
---

## 用户需求
在深色侧边栏的「金蝶 ERP」菜单组下增加「采购车辆计划查询」入口，点击即可进入已完成的采购车辆计划查询页面；同时顶栏面包屑应随当前页面标题变化，不再固定显示收货单标题。

## 功能内容
- 侧边栏「金蝶 ERP」分组下并列两个子菜单项：采购收货单查询、采购车辆计划查询
- 新增项使用卡车图标（与车辆计划业务语义一致），图标与路由 `meta.icon` 保持统一
- 选中态跟随当前路由自动高亮，点击菜单项直接切换到对应页面
- 顶栏面包屑显示为「金蝶 ERP / 当前页面标题」，标题取自各路由的 `meta.title`

## 视觉效果
保持与示例图一致：深蓝灰侧边栏（#1f2d3d）、顶部蓝色圆角 K 徽标 + 白色标题「金蝶 ERP 查询」、带网格图标的分组标题「金蝶 ERP」与右侧折叠箭头、两个带图标且文字浅色的子菜单项，当前所在页面项文字高亮为白色。


## 技术栈
- Vue 2 + Element UI 2.x（`el-container` / `el-menu` / `el-submenu` / `el-menu-item`）+ `vue-router`（hash 模式）
- 独立运行脚手架，`ruoyi-ui/node_modules` 不存在，构建与启动由用户自行处理

## 实现方式
本需求是纯前端导航层改动，后端与业务页面均已就绪，只改 `App.vue` 两处：

1. **新增菜单项**：`el-menu` 已启用 `router` 模式并绑定 `:default-active="$route.path"`，因此只需在 `el-submenu index="kingdee"` 内追加一个 `el-menu-item`，其 `index` 必须与路由 path 完全一致：
   ```html
   <el-menu-item index="/kingdee/vehicleplan">
     <i class="el-icon-truck"></i><span>采购车辆计划查询</span>
   </el-menu-item>
   ```
   跳转与选中高亮由 Element UI 自行处理，不需要额外状态、事件或样式；图标与 `router/index.js` 中该路由的 `meta.icon` 统一为 `el-icon-truck`。位置放在「采购收货单查询」之后，与示例图顺序一致。

2. **面包屑动态化**：把 `App.vue:26` 硬编码的「采购收货单查询」替换为 `{{ $route.meta.title || '查询' }}`。`meta.title` 已是两条路由的唯一标题来源，直接复用可避免再维护一份标题映射表（KISS），两个页面分别显示「采购收货单查询」「采购车辆计划查询」。

## 不改动的部分
- `ruoyi-ui/src/router/index.js`：`/kingdee/vehicleplan` 路由及 `meta.title`/`meta.icon` 已就绪，仅核对不修改
- `ruoyi-ui/src/styles/index.css`：菜单外观由 Element 默认样式 + `App.vue` 的 scoped 样式实现，截图所需的深色底、logo 徽标、品牌区样式均已存在；新增菜单项沿用现有样式，无需扩展全局样式
- 后端全部代码：车辆计划的 meta / query / queryBatch / export / exportBatch 五个接口、5 个 entity、`VehiclePlanSchema`、`VehiclePlanService`、`VehiclePlanExcelService`、`VehiclePlanController` 已完成，`mvn test` 21/21 通过
- 业务页面：`views/kingdee/vehicleplan/index.vue`、`VehiclePlanPanel.vue`、`api/kingdee/vehiclePlan.js` 已就绪

## 关键实现细节
- `el-menu-item` 的 `index` 与路由 `path` 必须严格一致，否则 `router` 模式点击后不会跳转（这是最容易踩的坑）
- 面包屑取 `$route.meta.title`，不在模板里写 `v-if` 分支，避免新增业务时忘记同步
- 侧边栏宽度、品牌区、导航栏等样式沿用现有 `App.vue` scoped 定义，不做无关重构，控制改动面

## 目录结构
```
ruoyi-ui/src/
├── App.vue                # [MODIFY] ① el-submenu "kingdee" 内新增 el-menu-item（
