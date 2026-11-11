# 金蝶 ERP 采购收货单查询工具（若依前端）

根据 `金蝶采购收货单数据结构与关联关系.md` 实现的只读查询工具：输入**采购收货单号**，一次性查询并返回主表、物料明细、明细扩展、收货箱、附件清单、附件表等 **13 张表 / 12 条关联关系** 的全部关联数据。

前端按**若依 RuoYi-Vue** 规范编写（`@/utils/request`、`@/api/...`、`src/views/...`、Element UI、`{code,msg,data}` 响应），既可直接拷入已有若依项目，也可用仓库内附带的独立脚手架运行。

## 目录结构

```
├─ pom.xml                             后端（Spring Boot 2.5.15 + MyBatis，JDK 8）
├─ src/main/java/com/ruoyi/kingdee/
│  ├─ controller/PurReceiveController.java   REST 接口
│  ├─ service/PurReceiveService.java         三层关联取数与结果组装
│  ├─ mapper/PurReceiveMapper.java           13 张表分步查询
│  ├─ constant/PurReceiveSchema.java         表字典 / 12 条关系 / 字段字典
│  ├─ util/RowUtils.java                     动态列归一化、关联键分组
│  └─ common/AjaxResult.java                 若依风格响应体
├─ src/main/resources/
│  ├─ application.yml                       通用配置（默认 demo 档案）
│  ├─ application-demo.yml                  内置 H2 演示库
│  ├─ application-kingdee.yml               真实金蝶库配置模板
│  ├─ mapper/PurReceiveMapper.xml            固定 SQL，全部参数用 #{} 绑定
│  └─ db/schema.sql、db/data.sql            演示库 13 张表 + 3 张示例单据
├─ src/test/java/.../PurReceiveIntegrationTest.java   端到端集成测试
├─ ruoyi-ui/                            若依风格前端（可独立运行）
│  ├─ src/api/kingdee/receive.js
│  ├─ src/views/kingdee/receive/index.vue         查询主页面
│  ├─ src/views/kingdee/receive/DynamicTable.vue  动态列表格组件
│  └─ src/utils/request.js etc.                   独立运行脚手架
└─ sql/kingdee_menu.sql                 若依菜单 / 权限脚本
```

## 快速开始（本机 JDK 8 + Maven 3.9 + Node 16）

### 1. 启动后端（默认 H2 演示库，无需安装数据库）

```powershell
mvn "-Dmaven.resolver.transport=wagon" -B test          # 编译 + 集成测试
mvn "-Dmaven.resolver.transport=wagon" spring-boot:run  # 启动，端口 8080
```

> 本机 Maven 默认 HTTP resolver 下载 jar 会失败，必须显式使用 wagon 传输；PowerShell 中 `-D` 参数含点号需整体加引号。

验证接口：

```powershell
curl "http://localhost:8080/kingdee/receive/query?billNo=CGSH-20261111-0001"
curl "http://localhost:8080/kingdee/receive/meta"
```

演示单号：`CGSH-20261111-0001`（2 行明细 / 4 条箱唛 / 3 条附件清单 / 4 个文件）、`CGSH-20261111-0002`、`CGSH-20261110-0003`（无箱唛与附件，用于验证空子表）。

### 2. 启动前端

```powershell
cd ruoyi-ui
npm install
npm run serve        # http://localhost:8081 ，已代理 /kingdee → http://localhost:8080
```

生产构建：`npm run build`，产物在 `ruoyi-ui/dist/`。

### 3. 接入已有若依项目

1. 复制 `ruoyi-ui/src/api/kingdee/receive.js`、`ruoyi-ui/src/views/kingdee/receive/`（含 `DynamicTable.vue`）到现有 `ruoyi-ui/src/` 对应目录。
2. **不要覆盖**已有若依的 `src/utils/request.js`、`src/router/index.js`、`src/main.js`、`src/App.vue`（这些文件仅用于本仓库独立运行）；页面使用的 `@/utils/request` 会自动指向若依原版封装。
3. 执行 `sql/kingdee_menu.sql` 添加菜单与 `kingdee:receive:query` 权限，刷新若依前端即可在菜单中打开。
4. 若依自带 `VUE_APP_BASE_API = '/prod-api'`，请把后端 `/kingdee/**` 加入反向代理，或把 `request.js` 的 `baseURL` 指向后端地址。

## 接口

| 方法 | 地址 | 说明 |
| --- | --- | --- |
| GET | `/kingdee/receive/query?billNo=xxx` | 精确查询单号，返回整张单据 13 张表关联数据 |
| GET | `/kingdee/receive/query?fid=1001` | 按主表内码精确查询（同号多单时前端让用户选择内码） |
| GET | `/kingdee/receive/query?billNo=xxx&fuzzy=true` | 只返回模糊匹配的单据候选列表 |
| GET | `/kingdee/receive/list?billNo=xxx` | 单号联想（输入时自动补全） |
| GET | `/kingdee/receive/meta` | 13 张表定义、12 条关联关系、字段中文字典 |

响应体：`{ "code": 200, "msg": "查询成功", "data": { found, billNo, fid, bill, tables, entries, summary, warnings, meta } }`。
`tables` 为 13 个表分区，每个分区含 `{label, joinKey, count, columns, rows}`；`entries` 中每行含 `{row, ext, boxes, attachList, stats}`。

## 切换到真实金蝶数据库

1. 编辑 `src/main/resources/application-kingdee.yml`：填写 `driver-class-name`、`url`、`username`、`password`。
2. 以 `kingdee` 档案启动：`mvn "-Dmaven.resolver.transport=wagon" spring-boot:run -Dspring-boot.run.profiles=kingdee`（或修改 `application.yml` 的 `spring.profiles.active`）。
3. 该档案 `spring.sql.init.mode: never`，**不会**对真实库执行任何建表/写操作。
4. 数据库驱动：已内置 PostgreSQL 42.3.8；Oracle 需添加 `ojdbc8`、SQL Server 需添加 `mssql-jdbc` 依赖（在 `pom.xml` 中取消对应注释或自行添加）。
5. 建议使用**只读账号**。工具只执行 `SELECT`，不包含任何 INSERT/UPDATE/DDL。

## 关联链路与设计说明

```
t_im_purrecbill.fid
  └─> t_im_purrecbillentry.fid                （主表 → 明细，含 5 张主表扩展表）
        └─> fentryid                          （明细 → 扩展表 / 收货箱 / 附件清单）
              ├─ t_im_purrecbillentry_l / _r / _lk
              ├─ tk_usp_receivingboxentry
              └─ tk_usp_cgshentryattach.fdetailid
                    └─ tk_usp_cgentryattachment
```

- **不做大 JOIN**：文档第 7 节指出一个明细可能对应多条箱唛/附件，直接 JOIN 会造成行数膨胀。实现按三个内码分表查询，再在 Java 层按内码分组组装。
- **不推测未知字段**：文档只确认了部分字段，因此统一使用 `SELECT *` 返回动态 `Map`，前端 `DynamicTable` 按实际列名渲染；已知字段通过 `/meta` 提供中文名，其余原样展示列名。
- **跨库兼容**：列名统一小写、数值/时间统一转字符串、内码数字按数值绑定（PostgreSQL `int8 = varchar` 会报错）；`IN` 条件按 500 个分批以兼容 Oracle 1000 上限。
- **降级提示**：某张表不存在、字段名不同或无权限时，该表返回空并在 `warnings` 中提示，不影响其他表查询。
- **安全**：表名/列名均为固定字面量，用户输入只经 `#{}` 绑定；单号做字符白名单校验（拒绝引号、空格、`%` 等）；单表行数默认上限 2000，可在 `application.yml` 调整。

## 已知限制

- 文档不是完整字段字典，演示库字段仅为验证用；真实环境的统计口径、基础资料（物料/仓库/供应商）关联表未纳入本工具。
- 模糊匹配使用 `UPPER(fbillno) LIKE UPPER(?)`，无法命中索引；数据量很大时建议改用精确单号查询或补充索引方案。
- 行数上限由 MyBatis `RowBounds` 在结果集层截断，数据库端仍可能扫描较多行，生产环境可按数据库方言改写成分页 SQL。
- 附件表只返回文件路径等元数据，不提供文件下载/预览（需接金蝶文件服务）。
