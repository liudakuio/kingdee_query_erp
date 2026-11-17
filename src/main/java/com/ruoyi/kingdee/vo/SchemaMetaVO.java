package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * /meta 接口响应：表字典、关联关系与字段中文标签。
 */
@Data
@JsonPropertyOrder({"tables", "relations", "columnLabels", "tableCount"})
public class SchemaMetaVO {

    /** 可查询的表定义。 */
    private List<TableDefVO> tables;

    /** 表关联关系。 */
    private List<RelationVO> relations;

    /** 数据库列名到中文标签的映射；列名由金蝶二开决定，保留动态 key。 */
    private Map<String, String> columnLabels;

    /** 表数量。 */
    private int tableCount;
}
