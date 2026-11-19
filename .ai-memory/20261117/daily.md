session-id: 20261117-0940

## [09:51] - 重构/代码审查: 后端接口类型化、低耦合改造与回归验证完成

- **文件**: `src/main/java/com/ruoyi/kingdee/{dto,vo,service,controller,util,constant,domain,mapper,common}`、`src/test/java/com/ruoyi/kingdee`
- **决策**: 业务层消除自定义泛型接口和裸 Map 契约；泛型能力只保留在 `util/QuerySupport`；Mapper/domain 内码保留 Object 以兼容 PostgreSQL int8 绑参，VO 边界统一 String；保持原接口 URL 与 JSON 字段名不变。
- **验证**: `mvn "-Dmaven.resolver.transport=wagon" "-Dspring.profiles.active=demo" -B clean test` 成功，37 项测试全部通过；静态扫描确认业务 Service/Controller 无 `Map<String,Object>`、无私有 `SqlQuery<T>`、无 TODO。

## [13:22] - 重构/代码审查: 迁移到 MyBatis-Plus + dynamic-datasource 完成

- **文件**: `pom.xml`、`src/main/resources/application*.yml`、`config/MybatisPlusConfig.java`、`config/VehiclePlanDemoDataConfig.java`、`domain/{PurRecBill,Express,VehiclePlan}.java`、`mapper/{receive,vehicleplan}`、`service/{ExpressService,VehiclePlanService,PurReceiveService}.java`、`src/test/java/com/ruoyi/kingdee`
- **决策**: 使用 `mybatis-plus-boot-starter:3.5.5` + `dynamic-datasource-spring-boot-starter:3.6.1`；删除双 SqlSessionFactory 配置，改为 Service 层 `@DS("receive")` / `@DS("vehicleplan")`；主表 Mapper 继承 `BaseMapper`，主表查询改用 `Page + QueryWrapper`，复杂子表查询保留 XML；设置 `map-underscore-to-camel-case: false` 保持金蝶 snake_case 字段名。
- **验证**: `mvn "-Dmaven.resolver.transport=wagon" "-Dspring.profiles.active=demo" -B clean test` 成功，37 项测试全部通过；无 lint 报错；静态扫描确认旧双工厂配置、旧主表 XML statement 和原生 mybatis starter 均已移除。

## [14:36] - 功能实现/代码审查: 修改箱号按钮改接通用 Excel process 接口完成

- **文件**: `pom.xml`、`controller/ExcelProcessController.java`、`service/{ExcelProcessService.java,impl/ExcelProcessServiceImpl.java}`、`vo/ExcelProcessRowVO.java`、`converter/NullableIntegerConverter.java`、`util/SnowflakeIdUtils.java`、`mapper/receive/PurReceiveMapper.java`、`mapper/receive/PurReceiveMapper.xml`、`ruoyi-ui/src/api/kingdee/receive.js`、`ruoyi-ui/src/views/kingdee/receive/index.vue`、`src/test/java/com/ruoyi/kingdee/ExcelProcess*Test.java`
- **决策**: 新增 `/kingdee/tool/excel/process`，按外部实现完整移植固定 11 列读取、K 列 code 查询、雪花 fdetailid、fseq 分组递增与 xlsx 下载；前端只提交 file，不再提交 rows；旧 `/kingdee/receive/fixBoxNo` 链路保留兼容。`selectByCode` 按外部实际 SQL 语义用 fbillno 过滤并加 LIMIT 1 避免多行异常，同时增加同 code 查询缓存降低 N+1。
- **验证**: `mvn "-Dmaven.resolver.transport=wagon" "-Dspring.profiles.active=demo" -B clean test` 成功，42 项测试全部通过；无 lint 报错；前端 API URL 已切换到 `/kingdee/tool/excel/process`，不再提交 rows 参数。
