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
采购车辆计划的数据存放在**另一个数据库**中，需要在后端为它新增一个独立的数据库连接；采购收货单继续使用现有主库，两套业务互不影响。

## 澄清结论（已确认）
- 新库类型：**PostgreSQL**（与现有主库一致，驱动已在 `pom.xml` 中，无需新增依赖）
- 连接信息放置：`application.yml` 中放 `kingdee.vehicleplan.datasource` 占位（默认指向 H2 空库），真实地址与账号写在 `application-kingdee.yml`（本地真实库配置文件）
- 演示档案：车辆计划数据源默认指向 H2，并在演示库中**建表并造示例数据**，使 demo 档案下可直接看到完整页面

## 核心功能
- 采购车辆计划查询走新的第二个数据源，主库（收货单）链路完全不受影响
- 两套 Mapper各自绑定自己的 SqlSessionFactory，车辆计划的5 张表查询全部落到新库
- 主库与车辆计划库的连接参数分离配置；真实凭据不进版本库
- 演示档案（demo）自动建出车辆计划的 5 张表与示例数据，可离线验证查询、批量合并与 Excel 导出
- 现有收货单的 16 个集成用例与车辆计划的 5 个单元测试继续全部通过


## 技术栈
沿用现有工程栈，不引入新依赖：Spring Boot 2.5.15、mybatis-spring-boot-starter 2.2.2、Hikari、PostgreSQL 驱动、H2（demo）、lombok、poi-ooxml 5.2.3、JDK8。

## 实现方案

### 1. 核心决策：Mapper 按业务分包 + 双 SqlSessionFactory
`@MapperScan` 无法按接口区分数据源，因此把 Mapper 按业务拆成两个子包，各自绑定独立的 `SqlSessionFactory`：

- 主库：`com.ruoyi.kingdee.mapper.receive.PurReceiveMapper` → `sqlSessionFactory`
- 车辆计划库：`com.ruoyi.kingdee.mapper.vehicleplan.VehiclePlanMapper` → `vehiclePlanSqlSessionFactory`

XML 同步迁移到 `resources/mapper/receive/` 与 `resources/mapper/vehicleplan/`，主库 `mybatis.mapper-locations` 收窄为 `classpath*:mapper/receive/*.xml`，避免主 factory 误加载车辆计划语句；两个 XML 的 namespace 同步更新。

### 2. 关键技术坑（必须处理）：主库 factory 需显式化
mybatis starter 的 `MybatisAutoConfiguration.sqlSessionFactory` 带 `@ConditionalOnMissingBean(SqlSessionFactory.class)`。一旦注册车辆计划的 `vehiclePlanSqlSessionFactory`，主库 factory 将**不再自动创建**，导致收货单 Mapper 启动报错。

处理方式：新增 `PrimaryMybatisConfig`，用 `org.mybatis.spring.SqlSessionFactoryBean` 显式构建主库 factory，并复用 starter 已绑定的 `MybatisProperties`（`getMapperLocations()` / `getConfiguration()`），使 `mybatis.mapper-locations` 与 `mybatis.configuration.call-setters-on-nulls` 行为不变；starter 自动配置的 `SqlSessionTemplate` 会按类型复用该 factory。

### 3. 车辆计划数据源与 factory
新增 `VehiclePlanDataSourceConfig`：
- `@Bean("vehiclePlanDataSource")` + `@ConfigurationProperties(prefix = "kingdee.vehicleplan.datasource")`，`DataSourceBuilder.create().build()`（Hikari），`hikari.read-only: true`、`maximum-pool-size: 5` 随配置绑定
- `@Bean("vehiclePlanSqlSessionFactory")`：`setDataSource`、`setMapperLocations("classpath*:mapper/vehicleplan/*.xml")`，并**手动 new `org.apache.ibatis.session.Configuration` 设 `setCallSettersOnNulls(true)`**（第二个 factory 不继承 `mybatis.configuration.*`）
- 配置类上挂 `@MapperScan(basePackages = "com.ruoyi.kingdee.mapper.vehicleplan", sqlSessionFactoryRef = "vehiclePlanSqlSessionFactory")`
- 车辆计划是只读查询，不涉及跨库事务，无需事务管理器

### 4. demo 演示数据初始化（只作用于车辆计划数据源）
`spring.sql.init` 只作用于主 DataSource，不能复用。新增 `@Profile("demo")` 的 `VehiclePlanDemoDataConfig`，用 `DataSourceInitializer` + `ResourceDatabasePopulator` 对 **vehiclePlanDataSource** 执行 `db/vehicleplan-schema.sql`、`db/vehicleplan-data.sql`；真实库（kingdee profile）下该配置类不生效，**绝不会建表**。

H2 内存库 URL 必须带 `DB_CLOSE_DELAY=-1`，否则连接池回收连接后内存库与表消失。

### 5. 配置分层
- `application.yml`：`kingdee.vehicleplan.datasource` 默认 H2（`jdbc:h2:mem:vehicleplan;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`）+ 注释说明真实连接写在 `application-kingdee.yml`
- `application-kingdee.yml`（本地真实库文件）：新增同前缀的真实 PostgreSQL 连接占位
- 安全加固：`.gitignore` 补充 `src/main/resources/application-kingdee.yml`，并提供 `application-kingdee.example.yml` 模板入库；提示用户执行 `git rm --cached src/main/resources/application-kingdee.yml` 使历史中的凭据不再被跟踪

### 6. 顺带修正
`VehiclePlanService` 在物料信息为空时会执行空 `IN ()`（SQL 语法错误被当成 warning），改为 `entryIds` 为空时直接跳过箱件查询。

## 性能与可靠性
- 两套连接池独立，主库 `maximum-pool-size: 5` 不被车辆计划占用；只读事务由 Hikari `read-only: true` 保证
- 批量 IN 按500 分批（Oracle 上限 1000 兼容），单表行数上限 2000、单次批量单号上限 20 沿用 `kingdee.query.*`
- 单表查询失败降级为 warning，不影响其它表与整体响应

## 架构设计

```mermaid
flowchart LR A[VehiclePlanController<br/>/kingdee/vehicleplan/*] --> S[VehiclePlanService]
  S --> M2[VehiclePlanMapper<br/>mapper.vehicleplan]
  M2 --> F2[vehiclePlanSqlSessionFactory]
  F2 --> DS2[(车辆计划库<br/>PostgreSQL / demo:H2)]

  B[PurReceiveController<br/>/kingdee/receive/*] --> S1[PurReceiveService]
  S1 --> M1[PurReceiveMapper<br/>mapper.receive]
  M1 --> F1[sqlSessionFactory<br/>PrimaryMybatisConfig]
  F1 --> DS1[(主库<br/>PostgreSQL / demo:H2)]

  C[VehiclePlanDemoDataConfig<br/>@Profile demo] -.仅演示档案建表造数.-> DS2
```

## 目录结构

```
kinde_erp/
├── src/main/java/com/ruoyi/kingdee/
│   ├── KingdeeApplication.java                 # [MODIFY] @MapperScan 收窄为 com.ruoyi.kingdee.mapper.receive，主库 factory 交由 PrimaryMybatisConfig 提供
│   ├── config/
│   │   ├── PrimaryMybatisConfig.java           # [NEW] 显式构建主库 SqlSessionFactory（复用 MybatisProperties 的 mapperLocations/configuration）+ @MapperScan(mapper.receive, sqlSessionFactoryRef="sqlSessionFactory")，避免被车辆计划 factory 触发 @ConditionalOnMissingBean 跳过
│   │   ├── VehiclePlanDataSourceConfig.java    # [NEW] vehiclePlanDataSource（@ConfigurationProperties kingdee.vehicleplan.datasource）+ vehiclePlanSqlSessionFactory（手动 Configuration 设 callSettersOnNulls）+ @MapperScan(mapper.vehicleplan, sqlSessionFactoryRef=...)
│   │   └── VehiclePlanDemoDataConfig.java      # [NEW] @Profile("demo")，DataSourceInitializer + ResourceDatabasePopulator 对车辆计划数据源执行 db/vehicleplan-schema.sql、db/vehicleplan-data.sql
│   ├── mapper/
│   │   ├── receive/PurReceiveMapper.java       # [MOVE] 从 mapper/ 移到 mapper/receive/，改 package，SQL 保持不变
│   │   └── vehicleplan/VehiclePlanMapper.java  # [MOVE] 从 mapper/ 移到 mapper/vehicleplan/，改 package，6 个查询保持固定表名 + #{} 绑定
│   └── service/VehiclePlanService.java         # [MODIFY] 更新 VehiclePlanMapper 的 import；物料信息为空时跳过箱件 IN 查询，避免空 IN 语法错误
├── src/main/resources/
│   ├── application.yml                         # [MODIFY] mybatis.mapper-locations 改为 classpath*:mapper/receive/*.xml；新增 kingdee.vehicleplan.datasource 占位（H2 默认值）与注释
│   ├── application-kingdee.yml                 # [MODIFY] 新增 kingdee.vehicleplan.datasource 的真实 PostgreSQL 连接占位（地址/账号/密码由用户填写，本文件不入库）
│   ├── application-kingdee.example.yml         # [NEW] 真实库配置模板（入库用），含主库与车辆计划库两段示例与字段说明
│   ├── mapper/receive/PurReceiveMapper.xml     # [MOVE] namespace 改为 com.ruoyi.kingdee.mapper.receive.PurReceiveMapper
│   ├── mapper/vehicleplan/VehiclePlanMapper.xml # [MOVE] namespace 改为 com.ruoyi.kingdee.mapper.vehicleplan.VehiclePlanMapper
│   └── db/
│       ├── vehicleplan-schema.sql              # [NEW] 5 张表 H2 建表脚本（沿用 db/schema.sql 风格：DROP TABLE IF EXISTS + CREATE TABLE；主表建 26 列，entity仍映射 16 列白名单）
│       └── vehicleplan-data.sql                # [NEW] 示例数据：2 张车辆计划主表 + 关联追踪 + 物料信息（含箱件） + 车辆明细，字段与 ER 图一致，其余列留 NULL
├── src/test/java/com/ruoyi/kingdee/
│   └── VehiclePlanIntegrationTest.java         # [NEW] @SpringBootTest + @AutoConfigureMockMvc + @ActiveProfiles("demo")：meta 表数/关系数、query 的 found/tables/entries/箱件数、queryBatch 合计与缺失标记、export 与 exportBatch 的 sheet 数与列数（含表级自定义列）
└── .gitignore                                  # [MODIFY] 补充 src/main/resources/application-kingdee.yml，避免真实库凭据继续入库
```

## 关键代码结构

```java
/** 车辆计划第二数据源：与主库隔离的连接池、SqlSessionFactory 与 Mapper扫描 */
@Configuration
@MapperScan(basePackages = "com.ruoyi.kingdee.mapper.vehicleplan",
            sqlSessionFactoryRef = "vehiclePlanSqlSessionFactory")
public class VehiclePlanDataSourceConfig {

    @Bean("vehiclePlanDataSource")
    @ConfigurationProperties(prefix = "kingdee.vehicleplan.datasource")
    public DataSource vehiclePlanDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("vehiclePlanSqlSessionFactory")
    public SqlSessionFactory vehiclePlanSqlSessionFactory(
            @Qualifier("vehiclePlanDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/vehicleplan/*.xml"));
        org.apache.ibatis.session.Configuration configuration =
                new org.apache.ibatis.session.Configuration();
        configuration.setCallSettersOnNulls(true);
        factory.setConfiguration(configuration);
        return factory.getObject();
    }
}
```

## 验证
- `mvn "-Dmaven.resolver.transport=wagon" -B "-Dspring.profiles.active=demo" test` → 21 个存量用例 + 新增车辆计划集成测试全部通过
- 前端本次无改动，`read_lints` 静态检查即可；页面效果由用户安装依赖后启动确认

