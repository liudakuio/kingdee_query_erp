package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 一张表在档案响应中的数据块。
 *
 * <p>rows 的元素是对应表的 domain 实体；列名由表结构决定，因此这里用对象列表承载，
 * 不把实体提前转成 Map。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"name", "label", "joinKey", "scope", "count", "columns", "rows", "truncated"})
public class TableSectionVO extends TableDefVO {

    /** 实际返回行数。 */
    private int count;

    /** 本次导出的列名顺序。 */
    private List<String> columns;

    /** 数据库行实体列表。 */
    private List<Object> rows;

    /** 是否达到行数上限。 */
    private boolean truncated;

    public TableSectionVO(String name, String label, String joinKey, String scope) {
        super(name, label, joinKey, scope);
    }
}
