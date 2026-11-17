package com.ruoyi.kingdee.service.export;

/**
 * 导出自定义列的取值逻辑。
 *
 * <p>刻意定义为非泛型接口，避免把 JDK 泛型函数式接口暴露到业务扩展点。</p>
 */
@FunctionalInterface
public interface ColumnValueResolver {

    /**
     * @param context 当前行的强类型上下文
     * @return 单元格文本；null 由 CustomColumn 统一转为空串
     */
    String resolve(RowExportContext context);
}
