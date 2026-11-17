package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * 批量查询的合计字段；业务没有的统计项不输出。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"entryCount", "boxCount", "vehicleCount", "attachCount", "fileCount"})
public class BatchTotalsVO {

    /** 明细行合计。 */
    private long entryCount;

    /** 箱件行合计。 */
    private long boxCount;

    /** 车辆明细合计；采购车辆计划专用。 */
    private Long vehicleCount;

    /** 附件清单合计；采购收货单专用。 */
    private Long attachCount;

    /** 附件文件合计；采购收货单专用。 */
    private Long fileCount;
}
