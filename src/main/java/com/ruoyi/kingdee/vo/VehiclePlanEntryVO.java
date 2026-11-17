package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ruoyi.kingdee.domain.ContainerEntry;
import lombok.Data;

/**
 * 采购车辆计划的物料信息聚合行。
 */
@Data
@JsonPropertyOrder({"fentryid", "fseq", "row", "ext", "stats"})
public class VehiclePlanEntryVO {

    /** 明细行内码（字符串形式）。 */
    private String fentryid;

    /** 分录行号。 */
    private Integer fseq;

    /** 物料信息实体行。 */
    private ContainerEntry row;

    /** 箱件信息等扩展关系。 */
    private EntryExtVO ext;

    /** 明细统计。 */
    private EntryStatsVO stats;
}
