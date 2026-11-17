session-id: 20261117-0940

## [09:51] - 重构/代码审查: 后端接口类型化、低耦合改造与回归验证完成

- **文件**: `src/main/java/com/ruoyi/kingdee/{dto,vo,service,controller,util,constant,domain,mapper,common}`、`src/test/java/com/ruoyi/kingdee`
- **决策**: 业务层消除自定义泛型接口和裸 Map 契约；泛型能力只保留在 `util/QuerySupport`；Mapper/domain 内码保留 Object 以兼容 PostgreSQL int8 绑参，VO 边界统一 String；保持原接口 URL 与 JSON 字段名不变。
- **验证**: `mvn "-Dmaven.resolver.transport=wagon" "-Dspring.profiles.active=demo" -B clean test` 成功，37 项测试全部通过；静态扫描确认业务 Service/Controller 无 `Map<String,Object>`、无私有 `SqlQuery<T>`、无 TODO。
