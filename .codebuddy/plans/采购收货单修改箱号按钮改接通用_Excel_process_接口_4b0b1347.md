---
name: 采购收货单修改箱号按钮改接通用 Excel process 接口
overview: 保持页面按钮位置不变，把 receive/index.vue 的“修改箱号信息”从 /kingdee/receive/fixBoxNo 改接新增的 /kingdee/tool/excel/process；后端完整移植外部 EasyExcel process 流程，并按项目现有 MyBatis-Plus + dynamic-datasource 架构落地。
todos:
  - id: baseline-audit
    content: 使用 [subagent:code-explorer] 复核 process 逻辑与调用点
    status: completed
  - id: backend-foundation
    content: 新增 EasyExcel、行模型、转换器和雪花工具
    status: completed
    dependencies:
      - baseline-audit
  - id: backend-mapper-service
    content: 新增 selectByCode 并移植 Excel 处理服务
    status: completed
    dependencies:
      - backend-foundation
  - id: backend-controller
    content: 新增通用 process 接口和下载响应
    status: completed
    dependencies:
      - backend-mapper-service
  - id: frontend-switch
    content: 修改收货单页按钮调用新接口
    status: completed
    dependencies:
      - backend-controller
  - id: tests-regression
    content: 新增测试并运行全量回归验证
    status: completed
    dependencies:
      - frontend-switch
  - id: expert-review
    content: 使用 [skill:编程专家.Skill] 评审迁移风险
    status: completed
    dependencies:
      - tests-regression
---

## 用户要求

用户原始请求：修改前端采购物流单查询界面的“修改箱号信息”按钮调用的后端接口，接口逻辑参考外部项目 `ExcelProcessController.java` 中的 `process` 方法。

已确认范围：

1. 只改采购收货单页面已有的“修改箱号信息”按钮，不在物流单页面新增按钮。
2. 在本项目新增通用后端接口 `/kingdee/tool/excel/process`。
3. 前端暂不传 `insertFentryIds` 和 `updateFentryIds`，后端保留这两个参数兼容。
4. 完整移植外部 `process` 业务逻辑：按固定 11 列读取 Excel，按编码查询明细内码，生成新的明细主键，行号按分组递增，输出处理结果 Excel。

## 产品概述

保持现有页面按钮位置和上传弹窗基本不变，点击“上传并处理”后，只上传 Excel 文件，不再提交当前查询行数据。后端处理完成后下载结果 Excel；失败时显示明确错误提示。

## 核心功能

- 收货单页“修改箱号信息”改调通用 Excel 处理接口。
- 后端接收 Excel 文件并返回处理结果文件。
- 上传文件为空或格式不符时返回明确提示。
- 按 Excel 第 K 列编码匹配箱号信息并回填明细内码。
- 生成 19 位明细主键，按明细内码分组生成递增行号。
- 保留旧箱号修正链路，不删除、不影响已有兼容能力。

## Tech Stack Selection

沿用项目现有技术栈：

- 后端：Java 8、Spring Boot 2.5.15、Spring MVC、MyBatis-Plus 3.5.5、dynamic-datasource 3.6.1。
- Excel：新增 EasyExcel 3.x，兼容 Java 8；保留现有 Apache POI 依赖不删除。
- 前端：现有 Vue 2 + Element UI，`axios` 封装请求，响应类型使用 blob。
- 测试：JUnit 5、Mockito、Spring Boot Test、MockMvc。
- Maven 验证命令：`mvn "-Dmaven.resolver.transport=wagon" "-Dspring.profiles.active=demo" -B clean test`。

## Implementation Approach

采用“后端新增通用接口 → 完整移植外部处理逻辑 → 前端切换接口 → 回归验证”的渐进式方案。

关键决策：

1. **通用接口独立暴露**  
新增 `/kingdee/tool/excel/process`，不复用 `/kingdee/receive/fixBoxNo`，避免旧接口语义混杂。旧接口和旧箱号修正链路保留兼容。
2. **业务规则完整移植**  
按外部 `ExcelVo` 的 11 列固定索引读取；第 K 列在 Java 字段中命名为 `code`，Excel 表头仍显示 `updateSql`。这是外部 `selectByCode` 的实际输入，必须保持列位一致。
3. **数据源边界清晰**  
`selectByCode` 查询收货单主库，因此新 Service 标注 `@DS("receive")`，不依赖 Mapper 包路径。
4. **降低重复查库**  
外部实现对每行都调用一次 `selectByCode`。本方案保留相同业务结果，但增加按 `code` 的本地缓存，同一编码只查一次，避免大文件场景出现 N+1 查询。
5. **错误协议一致**  
成功返回二进制 xlsx；文件为空或扩展名非法返回 400 JSON；处理失败返回 500 JSON，并记录服务端异常日志。
6. **前端最小改造**  
只改收货单页面按钮的提交逻辑和弹窗说明，保持现有页面布局和交互风格不变。

## Implementation Notes

- 新增 EasyExcel 时只用于新通用接口，不替换现有 POI 导出链路，避免影响已有 Excel 导出测试。
- `NullableIntegerConverter` 需移植：空单元格、空字符串返回 null，数字字符串按整数读取，输出时空值写空字符串。
- `SnowflakeIdUtils` 需移植到当前项目，沿用主机进程推导 workerId，并支持 JVM 参数覆盖。
- `insertFentryIds` 和 `updateFentryIds` 只接收并兼容，不参与现有输出计算；可保留解析方法供后续扩展。
- 输出文件名使用 `处理结果.xlsx`，sheet 名为 `处理结果`。
- 不要泄露数据库连接信息；日志只记录文件处理失败和表名/编码类业务上下文，不记录敏感配置。
- 旧 `FixBoxNoRequest`、`FixBoxNoService`、`BoxNoFixHandler`、`UploadedBoxNoRow` 等链路暂不删除，避免扩大改动范围。

## Architecture Design

调用链路：
前端按钮 → `/kingdee/tool/excel/process` → `ExcelProcessController` → `ExcelProcessService` → `PurReceiveMapper.selectByCode` → 收货单数据源 → EasyExcel 输出结果流 → 前端下载。

组件关系：

- Controller：只做参数校验、调用服务、设置下载响应头。
- Service：负责 Excel 读取、业务转换、ID 生成、行号分组、Excel 写出。
- Mapper：只负责按箱号编码查询 `ReceivingBoxEntry`。
- 前端 API：只负责封装 multipart 请求和 blob 响应。
- 前端页面：负责选择文件、触发请求、下载结果、展示错误。

## Directory Structure

```text
d:/project/kinde_erp/
├── pom.xml                                                      # [MODIFY] 新增 EasyExcel 3.x 依赖，保留 POI。
├── src/main/java/com/ruoyi/kingdee/
│   ├── controller/
│   │   └── ExcelProcessController.java                          # [NEW] 新增 /kingdee/tool/excel/process，校验文件，返回 xlsx 或 JSON 错误。
│   ├── service/
│   │   ├── ExcelProcessService.java                             # [NEW] 定义 Excel 处理服务接口。
│   │   └── impl/
│   │       └── ExcelProcessServiceImpl.java                     # [NEW] 移植外部 process 与 doBusiness 逻辑，标注 @DS("receive")。
│   ├── vo/
│   │   └── ExcelProcessRowVO.java                               # [NEW] 11 列 Excel 行模型，按 index 0 到 10 映射，第 K 列字段为 code。
│   ├── converter/
│   │   └── NullableIntegerConverter.java                        # [NEW] 可空整数转换器，处理 fseq 空单元格。
│   ├── util/
│   │   └── SnowflakeIdUtils.java                                # [NEW] 雪花 ID 生成器，用于生成 fdetailid。
│   └── mapper/receive/
│       └── PurReceiveMapper.java                                # [MODIFY] 新增 selectByCode 方法，返回 ReceivingBoxEntry。
├── src/main/resources/mapper/receive/
│   └── PurReceiveMapper.xml                                     # [MODIFY] 新增按 fk_usp_sendboxno 查询的 selectByCode SQL。
├── src/test/java/com/ruoyi/kingdee/
│   ├── ExcelProcessServiceTest.java                             # [NEW] 覆盖空文件、正常处理、code 未匹配、fseq 分组递增。
│   └── ExcelProcessControllerTest.java                          # [NEW] MockMvc 验证接口校验、blob 下载响应和错误 JSON。
└── ruoyi-ui/src/
├── api/kingdee/receive.js                                   # [MODIFY] 新增或修改上传处理 API，指向 /kingdee/tool/excel/process，responseType 为 blob。
└── views/kingdee/receive/index.vue                          # [MODIFY] 改弹窗文案；submitFixBoxNo 只提交 file，不再提交 rows。

## Agent Extensions

### SubAgent

- **code-explorer**
- Purpose: 复核外部 `process` 逻辑、当前前端按钮调用链、Mapper/XML 影响点。
- Expected outcome: 确认列位映射、接口参数、调用点和旧链路保留边界，避免漏改。

### Skill

- **编程专家.Skill**
- Purpose: 对新增接口、Excel 处理服务、前端切换和回归结果做代码审查。
- Expected outcome: 输出迁移风险清单，确认无功能偏差、无旧接口误删、测试和编译通过。