---
name: vehicleplan-box-tab
overview: 在采购车辆计划查询的 Tab 顺序中，于「主表扩展」与「车辆明细」之间新增独立的「箱号信息」Tab（箱件信息表 tk_usp_subentryentity），并修正一处标签换行格式问题。
todos:
  - id: add-box-tab
    content: 在 VehiclePlanPanel.vue 的主表扩展与车辆明细之间插入「箱号信息」Tab，复用 sectionOf(SUB_ENTRY) 与 DynamicTable
    status: completed
  - id: box-columns-order
    content: 新增 computed boxColumns：来源列保持首位，箱号等箱件关键列前置，其余列保持原顺序
    status: completed
    dependencies:
      - add-box-tab
  - id: fix-format-and-label
    content: 修正第 84 行标签挤行问题，并把物料信息展开标题统一为「箱号信息」
    status: completed
    dependencies:
      - add-box-tab
  - id: verify
    content: 执行 read_lints 静态检查并跑 Maven 回归，确认后端 24 个用例通过
    status: completed
    dependencies:
      - box-columns-order
      - fix-format-and-label
---

## 用户要求

在「采购车辆计划查询」页面的结果面板中，于「主表扩展」与「车辆明细」两个 Tab 之间新增一个独立的「箱号信息」Tab，展示箱件数据（表 `tk_usp_subentryentity`）。

## 功能内容

- **新增 Tab**：标签为「箱号信息 (N)」，N 为当前单据（或批量合并后）的箱件记录数，位于「主表扩展」之后、「车辆明细」之前。
- **Tab 内容**：顶部提示关联键说明「tk_usp_container_entry.fentryid = tk_usp_subentryentity.fentryid（一对多）」，下方以表格展示全部箱件记录，列为「发货箱号、集装箱号、箱内数量、应收数量、毛重、净重、体积、高/宽/长、是否到达、供应商批号」等箱件字段，表头使用中文标签并保留数据库列名。
- **列顺序优化**：箱号等关键列前置，便于一眼识别；批量查询时「单据编号」来源列仍保持在第一列。
- **物料信息 Tab 内展开行**的箱件明细保留不变（两处并存：展开看单条物料的箱件，Tab 看全部箱件），并把其标题文案统一为「箱号信息」以保持术语一致。
- 视觉效果沿用现有 Tab 与表格样式，不新增独立样式规则；单号查询与批量合并查询两种模式均可用。

## 边界

- 不改后端接口与数据组装逻辑，不改采购收货单页面，不改「全部表」Tab。

## 技术栈

沿用现有前端栈：Vue 2 + Element UI（`el-tabs` / `el-alert` / `el-tag`）+ 自研 `DynamicTable` 展示组件。后端零改动。

## 实施策略

纯前端单文件增量修改，落在 `ruoyi-ui/src/views/kingdee/vehicleplan/VehiclePlanPanel.vue`：

1. **新增 Tab pane**：在「主表扩展」pane（当前第 84-95 行）之后、「车辆明细」pane（第 97-105 行）之前插入 `name="boxes"` 的 pane。数据源全部复用组件已有能力：

- 行数据：`sectionOf(SUB_ENTRY).rows`（`SUB_ENTRY` 常量 = `tk_usp_subentryentity`，组件 `data()` 已暴露给模板）
- 列定义：`sectionOf(SUB_ENTRY).columns`
- 计数：`sectionOf(SUB_ENTRY).count`
- 渲染：`<dynamic-table :rows :columns :labels="columnLabels" :max-height="420" />`（与「车辆明细」Tab 写法一致，高度上限沿用同一量级）
- 提示：`<el-alert class="inner-tip" :closable="false" type="info" title="关联键：tk_usp_container_entry.fentryid = tk_usp_subentryentity.fentryid（一对多）" />`，与其它 Tab 的提示样式统一。

2. **新增 computed `boxColumns`（列前置）**：实体字段声明顺序导致「体积」在「发货箱号」之前，直接使用会让用户先看到无关列。故在组件内新增计算属性，把箱件关键列 `fk_usp_sendboxno`（发货箱号）、`fk_usp_wxno`（集装箱号）、`fk_usp_detailnum`（箱内数量）、`fk_usp_detailqty`（应收数量）、`fk_usp_gross_weight`（毛重）、`fk_usp_net_weight`（净重）、`fk_usp_volume`（体积）依次前置，其余列保持后端返回的原顺序（不丢字段）。

- **批量模式兼容**：批量合并时 `mergeDossier.js` 已把来源单据列 `__billNo` 放在 `columns` 首位；重排时必须把 `sourceColumn.key` 判定为首列并保持在前，否则会破坏「首列标识来源单据」的既有约定。因此 `boxColumns` 计算顺序为：来源列（若存在）→ 箱件关键列 → 其余列。

3. **顺手修一处格式瑕疵**：当前第 84 行 `</el-tab-pane>` 与下一个 `<el-tab-pane ...>` 挤在同一行（此前分块写入时丢失换行），一并拆行恢复可读性；该行同时是本次新 Tab 的插入锚点。

4. **术语统一**：物料信息 Tab 展开行标题由「箱件信息」改为「箱号信息」，与新 Tab 名称及用户口径一致（表名 `tk_usp_subentryentity` 在提示与「全部表」中仍原样展示，不臆测金蝶业务含义）。

## 性能与影响面

- 新 Tab 仅渲染已有分区数据，未引入额外请求；`DynamicTable` 对空分区回退 `EMPTY_SECTION`，无数据时展示空表格，不报错。
- 仅改动一个 .vue 文件，不触碰后端、收货单页面与公共组件（`DynamicTable.vue`、`mergeDossier.js` 均不动），回归风险极低。

## 验证

- 前端依赖由用户本地安装，无法在本次执行 `npm run build`；以 `read_lints` 静态检查 + 源码核对为准，页面效果由用户 `Ctrl+F5` 强刷确认。
- 后端未改动，可选执行 `mvn "-Dmaven.resolver.transport=wagon" -B "-Dspring.profiles.active=demo" test` 确认 24 个用例仍全通过。