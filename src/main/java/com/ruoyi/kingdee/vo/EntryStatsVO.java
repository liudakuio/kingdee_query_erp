package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * 单个明细行的统计信息；不同业务只输出自己实际统计的字段。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"boxes", "attachments", "files"})
public class EntryStatsVO {

    /** 箱件数量。 */
    private int boxes;

    /** 附件清单数量；采购收货单专用。 */
    private Integer attachments;

    /** 附件文件数量；采购收货单专用。 */
    private Integer files;
}
