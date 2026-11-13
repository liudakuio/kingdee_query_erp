package com.ruoyi.kingdee.service.export;

import java.util.List;

/**
 * Excel 导出的表级定制扩展点。
 * 实现类注册为 Spring Bean 即自动生效；一张表只能有一个处理器，重复注册在启动期报错。
 * 现有列与顺序保持不变，处理器返回的列一律追加到末尾。
 */
public interface TableExportCustomizer {

    /** 作用的表名，建议使用 PurReceiveSchema 中的表名常量。 */
    String tableName();

    /** 该表需要追加的列，返回空列表表示不追加。 */
    List<CustomColumn> extraColumns();
}
