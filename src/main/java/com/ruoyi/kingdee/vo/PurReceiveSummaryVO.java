package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购收货单汇总：在公共字段基础上增加附件统计。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"entryCount", "boxCount", "attachCount", "fileCount", "tableCount", "nonEmptyTableCount"})
public class PurReceiveSummaryVO extends DossierSummaryVO {

    /** 附件清单数量。 */
    private int attachCount;

    /** 附件文件数量。 */
    private int fileCount;
}
