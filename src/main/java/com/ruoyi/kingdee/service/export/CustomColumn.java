package com.ruoyi.kingdee.service.export;

import lombok.Data;

/**
 * 追加到导出表末尾的自定义列：表头文本 + 按行取值逻辑。
 * 取值异常由 AbstractTableExportService 统一降级为空并记录告警，处理器本身不必吞异常。
 */
@Data
public class CustomColumn {

    private final String header;
    private final ColumnValueResolver valueResolver;

    private CustomColumn(String header, ColumnValueResolver valueResolver) {
        this.header = header;
        this.valueResolver = valueResolver;
    }

    public static CustomColumn of(String header, ColumnValueResolver valueResolver) {
        if (header == null || header.trim().isEmpty()) {
            throw new IllegalArgumentException("自定义列表头不能为空");
        }
        if (valueResolver == null) {
            throw new IllegalArgumentException("自定义列取值逻辑不能为空");
        }
        return new CustomColumn(header, valueResolver);
    }

    /** null 一律转成空单元格，保证每行都有该列。 */
    public String resolve(RowExportContext context) {
        String value = valueResolver.resolve(context);
        return value == null ? "" : value;
    }
}
