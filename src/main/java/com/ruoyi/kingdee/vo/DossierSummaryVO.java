package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * 档案汇总的公共字段。
 */
@Data
@JsonPropertyOrder({"entryCount", "boxCount", "tableCount", "nonEmptyTableCount"})
public class DossierSummaryVO {

    /** 物料明细行数。 */
    private int entryCount;

    /** 箱件行数。 */
    private int boxCount;

    /** 关联表数量。 */
    private int tableCount;

    /** 实际有数据的表数量。 */
    private int nonEmptyTableCount;
}
