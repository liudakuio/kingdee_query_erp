package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ruoyi.kingdee.domain.VehiclePlan;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 采购车辆计划完整档案响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"found", "ambiguous", "candidates", "message", "billNo", "fid",
        "bill", "tables", "entries", "summary", "warnings", "meta", "error"})
public class VehiclePlanDossierVO extends DossierResultVO {

    /** 采购车辆计划主表实体。 */
    private VehiclePlan bill;

    /** 物料信息聚合行。 */
    private List<VehiclePlanEntryVO> entries;

    /** 档案汇总。 */
    private VehiclePlanSummaryVO summary;
}
