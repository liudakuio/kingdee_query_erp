package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * 单号重复或模糊匹配时的候选单据。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"fid", "billNo", "label", "status", "bizDate"})
public class BillCandidateVO {

    /** 主表内码，统一为字符串以避免前端精度丢失。 */
    private String fid;

    /** 单据编号。 */
    private String billNo;

    /** 下拉框展示文案。 */
    private String label;

    /** 单据状态。 */
    private String status;

    /** 业务日期；仅采购收货单联想结果提供。 */
    private String bizDate;
}
