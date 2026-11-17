package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表字典中的单张表定义，供前端生成动态列标题。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"name", "label", "joinKey", "scope"})
public class TableDefVO {

    /** 物理表名。 */
    private String name;

    /** 中文表名。 */
    private String label;

    /** 与其他表关联的字段名。 */
    private String joinKey;

    /** 归属分组：bill、entry 或 custom。 */
    private String scope;
}
