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

批量（多单号）查询时，结果展示改为**合并成一份**，而不是每个单据各开一套面板：

- 去掉「单据主表」模块（主表字段描述与关联链路展示），只保留统计卡与 Tab 区
- 顶部4 张统计卡保留，数量显示所有单据的合计（物料明细行 / 收货箱记录 / 附件清单 / 附件文件）
- Tab 区保留：物料明细、主表扩展、收货箱、附件清单与附件、全部表、关联关系
- 同一张表的数据混合在**同一个 Tab** 内展示（例如两个单号的 `tk_usp_receivingboxentry` 都在「收货箱」这一个 Tab 里）
- 每张表（含物料明细）的**第一列**为「单据编号」，标识该行来自哪个单据
- 未找到的单据仍以提示条列出，避免静默丢失

## 效果描述

批量查询后页面呈现为一块统计卡（合计数量）+ 一组 Tab：切换「物料明细」看到所有单据的明细行，每行最左侧是所属单据编号；切换「收货箱」看到所有单据的箱唛记录，同样带单据编号首列；其余 Tab 同理。单号查询的展示完全不变。

## 边界

- 不改后端接口与 Excel 导出（导出仍按表合并、来源列在末尾）
- 不改单号查询的展示与交互
- 依赖安装与前端构建由使用方自行处理


## 技术栈

沿用现有前端栈，不引入新依赖：

- Vue 2 + Element UI（`el-table` / `el-tabs` / `el-descriptions` / `el-card`）
- 现有组件：`ruoyi-ui/src/views/kingdee/receive/DossierPanel.vue`（结果面板，上一轮新建）、`DynamicTable.vue`（按动态列渲染表格）
- 后端不改：Spring Boot 2.5.15 + JDK8；`/kingdee/receive/queryBatch` 已返回批次汇总对象 `{ total, foundCount, missingCount, totals, mode, items[] }`

## 实现思路

### 核心策略：构造「合并 dossier」复用同一个结果面板

`DossierPanel` 只消费一个 dossier 对象（`tables` / `entries` / `summary` / `bill` / `meta`）。因此不需要改动它的渲染骨架，只需在**批量模式下由前端把多个单据的 dossier 合成一个同构 dossier**，再通过两个新 props 表达「无主表模块」和「来源列」：

- `hideBillCard: Boolean` —— 为 true 时不渲染「单据主表」卡片（含 `chain-line` 关联链路）
- `sourceColumn: { key, label }` ——声明来源列的字段键与中文标题

合并规则：

| 数据 | 合并方式 |
|---|---|
| `tables[<表名>]` | 按表名汇总所有单据的 `rows`，每行浅拷贝并追加 `__billNo`；`columns` 在**最前面**插入 `__billNo`；`count` 取合并后行数；`truncated` 任一单据为真即为真；`name/label/joinKey/scope` 沿用首个存在的 section |
| `entries` | 各单据 `entries` 拼接，每项浅拷贝并追加 `__billNo` |
| `summary` | `entryCount/boxCount/attachCount/fileCount` 取各单据 `summary` 之和；`nonEmptyTableCount` 取合并后有数据的表数 |
| `bill` | 置空（批量不渲染主表模块） |
| `meta` | 取首个单据的 `meta`（保留兜底，避免模板访问 `dossier.meta` 报错） |

### 来源列在两种表格里的处理差异（关键点）

- **普通表**（主表扩展 / 收货箱 / 附件 / 全部表）由 `DynamicTable` 渲染，它按 `columns` 逐列取 `scope.row[col]`；合并后的 `columns` 已含 `__billNo` 且 `rows` 已带该键，**无需改动 `DynamicTable`**，来源列自然落在 `#` 序号列之后的第一列。
- **物料明细表**在 `DossierPanel` 内是手写 `el-table`，明细列取值走 `scope.row.row[col]`（`row` 是明细行对象），而来源键挂在**明细条目对象**上（`scope.row.__billNo`），两者层级不同，不能靠 `columns` 通用渲染。因此：
  - `entryColumns` 计算属性需**过滤掉** `sourceColumn.key`，避免出现一列空白的「单据编号」
  - 明细表单独渲染一个来源列 `<el-table-column :prop="sourceColumn.key" :label="sourceColumn.label">`，取值 `scope.row[sourceColumn.key]`，置于明细列之前

### 性能与稳定性

- 合并为纯前端计算，复杂度 `O(Σ 行数)`，仅做浅拷贝（不深拷贝嵌套的 `ext`/`boxes`/`attachList`），内存开销可控；上限由后端 `max-batch-bills=20` 与 `max-rows-per-table=2000` 约束
- 合并结果放在 `computed` 中，依赖 `batchResults` / `tableDefs`，只在查询结果变化时重算一次，不在每次渲染时重复合并
- `summary` 合计直接从各单据 `summary` 累加（而非依赖 `batchSummary.totals`），使旧版数组响应与新版汇总对象两种情况都能得到正确数字

### 设计取舍

- **不再拆分面板**：用户明确要求「多个单号的数据都在同一个 Tab 里」，故不再按单据循环渲染面板
- **合并逻辑独立成模块**：新建 `mergeDossier.js` 纯函数文件，`index.vue` 只负责取数与渲染，便于后续复用与维护
- **统计卡沿用组件**：合并 dossier 的 `summary` 已是合计值，组件内 `statCards` 无需改动，天然满足「保留统计卡并显示合计」
- **单号路径零影响**：所有新逻辑都由 `hideBillCard` / `sourceColumn` 默认值控制，单号查询仍走原分支

## 架构设计

```mermaid
flowchart TD A["index.vue<br/>handleQuery 批量分支<br/>res.data.items"] --> B["mergeDossier.js<br/>mergeDossiers(items, tableDefs, sourceKey)<br/>合并 tables / entries / summary"]
    B --> C["index.vue computed<br/>mergedDossier / mergedColumnLabels"]
    C --> D["DossierPanel.vue<br/>hideBillCard=true<br/>sourceColumn=单据编号"]
    D --> E["统计卡(合计)"]
    D --> F["Tab: 物料明细(来源列单独渲染)"]
    D --> G["Tab: 主表扩展/收货箱/附件/全部表<br/>DynamicTable 按 columns 自动出首列)"]
    D --> H["Tab: 关联关系"]
    I["单号查询 singleDossier"] --> D
```

## 目录结构

```
ruoyi-ui/src/views/kingdee/receive/
├── mergeDossier.js   # [NEW] 合并工具。导出 mergeDossiers(items, tableDefs, sourceKey):
│                     #1) 按表名合并所有单据 section 的 rows，逐行浅拷贝并写入 sourceKey=单据编号；
│                     #   2) section.columns 最前面插入 sourceKey，count 取合并行数，truncated 取「任一为真」；
│                     #   3) 合并 entries，逐项浅拷贝写入 sourceKey；
│                     #   4) summary 的 entryCount/boxCount/attachCount/fileCount 累加各单据 summary，
│                     #      nonEmptyTableCount 取合并后有数据的表数；
│                     #   5) 返回与单据 dossier 同构的对象（bill 置空、meta 取首个单据）。
│                     #   要求：纯函数、不依赖 Vue，便于单测与复用。
├── DossierPanel.vue  # [MODIFY] 结果面板组件。新增 props hideBillCard(Boolean,默认false)、
│                     #   sourceColumn(Object,默认null)；「单据主表」卡片加 v-if="!hideBillCard"；
│                     #   entryColumns 计算属性过滤掉 sourceColumn.key；
│                     #   物料明细表新增来源列（:prop=sourceColumn.key，:label=sourceColumn.label，
│                     #   取值 scope.row[sourceColumn.key]），置于明细数据列之前；
│                     #   widthOf 对来源列给出约 190 的固定宽度；单号模式（默认 props）行为完全不变。
└── index.vue         # [MODIFY] 页面。批量区由「按单据循环渲染多个面板」改为「渲染单个合并面板」：
                      #   新增常量 SOURCE_KEY='__billNo'、SOURCE_LABEL='单据编号'；
                      #   新增 computed mergedDossier（调用 mergeDossier.js）与 mergedColumnLabels
                      #   （{ ...columnLabels, [SOURCE_KEY]: '单据编号' }）；
                      #   批量渲染：v-if="foundBatchItems.length" 渲染一个 <dossier-panel>
                      #     :dossier="mergedDossier" :hide-bill-card="true" :source-column="sourceColumnDef"
                      #     :table-defs="tableDefs" :column-labels="mergedColumnLabels" :relations="metaInfo.relations || []"；
                      #   missingBatchItems 的「未找到」el-alert 保留；
                      #   单号渲染分支与候选卡片、联想、导出逻辑保持不变。
```

后端文件（`PurReceiveService` / `ExcelExportService` / `PurReceiveController` / 集成测试）本次**不改动**。

## 关键接口约定

```javascript
// mergeDossier.js
export function mergeDossiers(items, tableDefs, sourceKey) {
  // items: 已过滤 found 的单据数组；tableDefs: 后端表字典；
  // 返回与单据 dossier 同构的合并对象，section.rows 每行带 sourceKey，section.columns 首列为 sourceKey
}
```

```javascript
// DossierPanel.vue 新增 props
props: {
  hideBillCard: { type: Boolean, default: false },      // 批量模式隐藏「单据主表」模块
  sourceColumn: { type: Object, default: null }        // { key: '__billNo', label: '单据编号' }
}
```

## 实施注意事项

- `index.vue` 中 `batchSummary` 仍需保留：`canExport`（`foundCount > 0`）、`isRealDb`（`mode`）、`exportJson`（导出完整批次对象）、`exportExcel`（批量走 `/exportBatch`）都依赖它，不能因 UI 调整而失效
- `handleQuery` 批量分支保留「数组 / 汇总对象」双兼容兜底，避免后端进程为旧版时整页空白
- 明细表 `row-key="fentryid"` 在跨单据合并场景下仍唯一（fentryid 为全局唯一内码），如需更稳妥可改为 `fentryid + 单据编号` 组合键
- `DynamicTable` 无需改动；`columnLabels` 传入合并后的对象即可让来源列显示「单据编号」中文表头（`DynamicTable.labelOf` 优先取 `labels[col]`）
- 展开行内的 `boxes` / `attachList` / `ext` 属于单条明细行，天然归属单一单据，**不加**来源列
- 不执行任何 npm 命令（安装依赖、构建由使用方自行处理），不重建 `dist/`
- 后端回归命令：`mvn "-Dmaven.resolver.transport=wagon" -B "-Dspring.profiles.active=demo" test`（16 个用例应仍全通过）

