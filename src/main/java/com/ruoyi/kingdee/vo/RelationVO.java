package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表与表之间的一条关联关系。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"from", "fromKey", "to", "toKey", "cardinality", "note"})
public class RelationVO {

    /** 起始表。 */
    private String from;

    /** 起始表关联字段。 */
    private String fromKey;

    /** 目标表。 */
    private String to;

    /** 目标表关联字段。 */
    private String toKey;

    /** 基数，例如 OneN、ZeroOne。 */
    private String cardinality;

    /** 中文说明。 */
    private String note;
}
