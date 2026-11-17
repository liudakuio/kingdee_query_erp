session-id: 20261117-0940

## [09:51] - 重构/代码审查: 后端接口类型化、低耦合改造与回归验证完成

- **文件**: `src/main/java/com/ruoyi/kingdee/{dto,vo,service,controller,util,constant,domain,mapper,common}`、`src/test/java/com/ruoyi/kingdee`
- **决策**: 业务层消除自定义泛型接口和裸 Map 契约；泛型能力只保留在 `util/QuerySupport`；Mapper/domain 内码保留 Object 以兼容 PostgreSQL int8 绑参，VO 边界统一 String；保持原接口 URL 与 JSON 字段名不变。
- **验证**: `mvn "-Dmaven.resolver.transport=wagon" "-Dspring.profiles.active=demo" -B clean test` 成功，37 项测试全部通过；静态扫描确认业务 Service/Controller 无 `Map<String,Object>`、无私有 `SqlQuery<T>`、无 TODO。

## [13:22] - 重构/代码审查: 迁移到 MyBatis-Plus + dynamic-datasource 完成

- **文件**: `pom.xml`、`src/main/resources/application*.yml`、`config/MybatisPlusConfig.java`、`config/VehiclePlanDemoDataConfig.java`、`domain/{PurRecBill,Express,VehiclePlan}.java`、`mapper/{receive,vehicleplan}`、`service/{ExpressService,VehiclePlanService,PurReceiveService}.java`、`src/test/java/com/ruoyi/kingdee`
- **决策**: 使用 `mybatis-plus-boot-starter:3.5.5` + `dynamic-datasource-spring-boot-starter:3.6.1`；删除双 SqlSessionFactory 配置，改为 Service 层 `@DS("receive")` / `@DS("vehicleplan")`；主表 Mapper 继承 `BaseMapper`，主表查询改用 `Page + QueryWrapper`，复杂子表查询保留 XML；设置 `map-underscore-to-camel-case: false` 保持金蝶 snake_case 字段名。
- **验证**: `mvn "-Dmaven.resolver.transport=wagon" "-Dspring.profiles.active=demo" -B clean test` 成功，37 项测试全部通过；无 lint 报错；静态扫描确认旧双工厂配置、旧主表 XML statement 和原生 mybatis starter 均已移除。
