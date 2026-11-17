package com.ruoyi.kingdee.service.export;

import java.util.Collections;
import java.util.Map;

/**
 * 自定义列按行取值时的上下文：当前实体行、单据信息、表名。
 *
 * <p>外部只通过 {@link #value(String)} / {@link #text(String)} 按列名取值，
 * 不再暴露内部 Map，避免导出扩展点依赖泛化数据结构。</p>
 */
public class RowExportContext {

    private final Object rowEntity;
    private final Map<String, Object> values;
    private final String billNo;
    private final String fid;
    private final String tableName;

    public RowExportContext(Object rowEntity, Map<String, Object> values,
                            String billNo, String fid, String tableName) {
        this.rowEntity = rowEntity;
        this.values = values == null ? Collections.<String, Object>emptyMap() : values;
        this.billNo = billNo;
        this.fid = fid;
        this.tableName = tableName;
    }

    /** 当前行的 domain 实体。 */
    public Object getRowEntity() {
        return rowEntity;
    }

    /** 所属单据编号。 */
    public String getBillNo() {
        return billNo;
    }

    /** 主表内码，供自定义列生成 SQL 等场景使用。 */
    public String getFid() {
        return fid;
    }

    /** 当前表名。 */
    public String getTableName() {
        return tableName;
    }

    /** 按数据库列名取原始值。 */
    public Object value(String column) {
        return values.get(column);
    }

    /** 按数据库列名取文本值，null 统一转为空串。 */
    public String text(String column) {
        Object value = values.get(column);
        return value == null ? "" : String.valueOf(value);
    }
}
