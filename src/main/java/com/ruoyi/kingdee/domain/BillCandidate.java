package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 单号联想 / 模糊查询返回的候选单据。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillCandidate {

    /** 主表内码，回传给 /query 做精确查询 */
    private Object fid;
    /** 单据编号 */
    private String billNo;
    /** 下拉框展示文本 */
    private String label;
    /** 单据状态 */
    private String status;
    /** 业务日期 */
    private String bizDate;
}
