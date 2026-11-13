---
name: excel-table-custom-columns
overview: 为 Excel 导出增加后端表级扩展点，支持按表追加自定义列且列值由 Java 处理器定义；保持现有列、前端与 /export 接口不变，并在 tk_usp_receivingboxentry 提供可运行示例。
todos:
  - id: verify-export-chain
    content: 使用 [subagent:code-explorer] 核对导出服务与测试断言
    status: completed
  - id: add-customizer-api
    content: 新增表级扩展接口、列模型与箱唛示例列
    status: completed
    dependencies:
      - verify-export-chain
  - id: wire-excel-service
    content: 改造 ExcelExportService 注入自定义列
    status: completed
    dependencies:
      - add-customizer-api
  - id: update-export-tests
    content: 更新集成测试覆盖单表、全部表与空表
    status: completed
    dependencies:
      - wire-excel-service
  - id: run-maven-verification
    content: 运行 Maven 编译与集成测试验证导出
    status: completed
    dependencies:
      - wire-excel-service
      - update-export-tests
---

## User Requirements

- 修改 `ExcelExportService.java:47-73` 的 `build` 及其导出流程，使**不同的表可以单独做特殊处理**。
- 以 `tk_usp_receivingboxentry` 为例，导出时新增一个列，列的内容可以自定义。
- 澄清确认：内容由**后端 Java 扩展点**提供（每张表一个处理器，列值可取该行其它字段或固定文本）；**仅追加自定义列**，保持现有列及顺序不变；**前端与 `/export` 接口不变**；需在该表提供可运行的示例列。

## Product Overview

在现有 Excel 导出能力（全部表 13 个工作表 / 指定单表）之上，增加按表注册的表级定制扩展点。原有列、表头样式、冻结首行、列宽自适应与「文本写入」策略保持不变；导出结果中，仅配置了处理器的表在**末尾多出自定义列**，其余表与当前完全一致。

## Core Features

- 表级处理器自动注册：新增一个实现类即可为某张表定制，无需改动导出主流程。
- 自定义列按行生成值：处理器可读该行字段、单据编号、fid、表名等上下文，也可返回固定文本。
- 内置 `tk_usp_receivingboxentry` 示例列（表头与内容由示例处理器定义），后续改示例类即可自定义内容。
- 单表导出与全部表导出一致生效；空表只多一列空表头；无处理器的表行为零变化。
- 前端导出范围下拉、下载交互与 `/export` 参数保持原样。

## Tech Stack Selection

沿用现有栈：Java 8、Spring Boot 2.5.15、Apache POI 5.2.3（`poi-ooxml` 已在 `pom.xml`）、Lombok `@Data`、Jackson `ObjectMapper`、JUnit 5 + MockMvc。**不新增依赖**；`PurReceiveController`、`ruoyi-ui` 前端、`pom.xml` 均不改动。

## Implementation Approach

采用策略模式 + Spring 自动发现，保持开闭原则：定义 `TableExportCustomizer` 接口（声明作用表名与追加列定义），`ExcelExportService` 通过构造器注入 `List&lt;TableExportCustomizer&gt;` 并构建 `表名 → 处理器` 的 Map；`build` 与 `writeSheet` 只在写完原有列后，把该表的追加列写到**表头末尾与每行末尾**。

关键决策与取舍：

- **扩展点粒度按表**：与用户「不同的表单独做特殊处理」一一对应；同表注册多个处理器时启动期抛 `IllegalStateException`，避免静默覆盖。
- **值由 `Function&lt;RowExportContext, String&gt;` 计算**：既有按行计算能力（用户明确选择「列值可取该行其它字段」），又避免把业务规则写死在导出服务里；上下文携带 `row`（`objectMapper.convertValue` 后的 Map，Long 已字符串化）、`billNo`、`fid`、`tableName`。
- **仅追加、不重排/隐藏/改表头/改样式**（用户澄清只选此项）：`writeSheet` 改动局限在宽度数组扩容与两处追加写入，`headerOf`、`displayLength`、`headerStyle`、`resolveTables` 保持原样，改动面最小。
- **可靠性**：单个处理器异常时该单元格写空字符串并按表记录一次 `warn`（复用 slf4j，不打印整行数据），不使整份导出失败，与 `PurReceiveService.safe(...)` 的降级风格一致；`build` 仍先生成完整 `byte[]` 再返回，异常仍由 `GlobalExceptionHandler` 输出 JSON。
- **精度保护**：自定义列值统一按文本写入，不引入数值类型转换，延续「19 位内码不丢精度」的既有约定。

性能：每表一次 O(1) Map 查找，每行仅对追加列做一次计算，复用已有 `convertValue` 结果，不重复序列化；总复杂度 O(行数 × (原列数 + 追加列数))，与原实现同阶；无处理器的表零额外开销。

## Implementation Notes

- 复用现有模式与工具：表名用 `PurReceiveSchema.BOX` 等常量（禁止硬编码字符串）、`PurReceiveSchema.columnLabels()`、`WorkbookUtil.createSafeSheetName`、`objectMapper.convertValue`、`displayLength` 与 `MIN/MAX_COLUMN_WIDTH`；新模型类使用 Lombok `@Data`，与实体风格一致。
- `build` 从 dossier 取 `billNo`/`fid` 组装上下文基础信息（找不到时回退到入参），传给 `writeSheet`；控制器注入参数数量不变（`ExcelExportService` 构造器新增一个参数，由 Spring 自动装配）。
- 空表分支：仍只创建第 0 行表头，追加列同样只写表头；`createFreezePane(0, 1)` 不变。
- 日志：降级时每表最多一条 `warn`，不含行数据；避免重复日志刷屏。
- 向后兼容：`exportAllTablesShouldCreateOneSheetPerTable`（13 sheet）、`exportUnknownTableShouldReturnBadRequest`（code 400）不变；`exportSingleTableShouldCreateOnlyThatSheet` 的表头列数断言由 9 调整为 10，并补充末列表头、首行自定义值、`getLastRowNum()` 仍为 4、首列仍为 `BOX-0001`。
- 上一轮导出功能的 Maven 编译/测试被用户取消、**尚未验证过**，本次必须一并执行（POI 依赖可能首次下载）。
- 构建命令：`$env:JAVA_HOME='D:\app\jdk8'; $env:PATH="$env:JAVA_HOME\bin;D:\app\Maven\apache-maven-3.9.16\bin;$env:PATH"; mvn "-Dmaven.resolver.transport=wagon" -B "-Dspring.profiles.active=demo" test`（PowerShell 中含点号的 `-D` 必须整体加引号）。

## Architecture Design

`PurReceiveController.export` → `ExcelExportService.build`（查询复用 `PurReceiveService.query`，解析导出范围）→ `writeSheet`（原列 + 该表 `TableExportCustomizer` 的追加列）→ `XSSFWorkbook` 字节流。定制逻辑隔离在 `service/export` 子包的处理器中，`ExcelExportService` 只依赖接口，新增表定制无需改主流程；组件关系简单，不引入新架构层。

## Directory Structure

```
project-root/
├── src/main/java/com/ruoyi/kingdee/service/
│   ├── ExcelExportService.java                 # [MODIFY] 导出主流程。构造器注入 List&lt;TableExportCustomizer&gt; 并建表名索引；build 组装 RowExportContext 基础信息；writeSheet 在原 columns 末尾追加自定义列（表头、数据行、列宽数组），空表仅表头；处理器异常降级为空 + 每表一次 warn；其余方法保持不变。
│   └── export/
│       ├── TableExportCustomizer.java         # [NEW] 表级定制扩展点接口。声明 tableName() 与 extraColumns()；由 Spring 扫描 @Component 实现自动注册，同表冲突在启动期报错。
│       ├── CustomColumn.java                  # [NEW] 追加列模型。Lombok @Data，字段 header（表头文本）与 valueResolver（Function&lt;RowExportContext,String&gt;），提供 of(...) 静态工厂与 resolve(context)；resolve 内做 null/异常兜底返回空字符串。
│       ├── RowExportContext.java              # [NEW] 按行计算上下文。Lombok @Data，字段 row（该行字段 Map）、billNo、fid、tableName；仅传值，不含 Excel/POI 依赖。
│       └── BoxEntryExportCustomizer.java      # [NEW] 可运行示例。@Component，tableName() 返回 PurReceiveSchema.BOX；追加一列自定义表头，值按行由 fk_usp_sendboxno 与 fk_usp_arrived 等字段拼装，用户改此类即可自定义列内容。
└── src/test/java/com/ruoyi/kingdee/
    └── PurReceiveIntegrationTest.java         # [MODIFY] 调整单表导出列数断言（9 → 10）；新增断言：末列表头文本、首行自定义列值、原有首列 BOX-0001 与 4 行数据不变；补充全部表导出时其它 12 张表列数不变、以及单据 1003 空箱唛表只含表头（含自定义列）的用例。
```

不改动：`PurReceiveController.java`、`ruoyi-ui/src/api/kingdee/receive.js`、`ruoyi-ui/src/views/kingdee/receive/index.vue`、`pom.xml`、`db/*.sql`。

## Key Code Structures

扩展点契约为本次核心架构，仅给出接口级签名（不含实现）：

```java
public interface TableExportCustomizer {
    String tableName();
    List&lt;CustomColumn&gt; extraColumns();
}

@Data
public final class CustomColumn {
    private String header;
    private Function&lt;RowExportContext, String&gt; valueResolver;
    public static CustomColumn of(String header, Function&lt;RowExportContext, String&gt; valueResolver);
    public String resolve(RowExportContext context);
}

@Data
public final class RowExportContext {
    private Map&lt;String, Object&gt; row;
    private String billNo;
    private Object fid;
    private String tableName;
}
```

## Agent Extensions

### SubAgent

- **code-explorer**
- Purpose: 实施前快速核对 `ExcelExportService.java`、`PurReceiveSchema` 表名常量、`PurReceiveIntegrationTest` 导出用例的当前状态与断言。
- Expected outcome: 产出精确的待修改文件清单与需调整的断言位置（特别是单表导出表头列数 9 需改为 10），确保改动基于最新代码而非压缩上下文。