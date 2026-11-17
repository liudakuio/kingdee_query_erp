package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购车辆计划汇总：在公共字段基础上增加车辆明细数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"entryCount", "boxCount", "vehicleCount", "tableCount", "nonEmptyTableCount"})
public class VehiclePlanSummaryVO extends DossierSummaryVO {

    /** 车辆明细行数。 */
    private int vehicleCount;
}
