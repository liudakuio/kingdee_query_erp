package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ruoyi.kingdee.domain.PurRecBill;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 采购收货单完整档案响应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"found", "ambiguous", "candidates", "message", "billNo", "fid",
        "bill", "tables", "entries", "summary", "warnings", "meta", "error"})
public class PurReceiveDossierVO extends DossierResultVO {

    /** 采购收货单主表实体。 */
    private PurRecBill bill;

    /** 物料明细聚合行。 */
    private List<PurReceiveEntryVO> entries;

    /** 档案汇总。 */
    private PurReceiveSummaryVO summary;
}
