package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/** 收货箱明细子单据体 tk_usp_receivingboxentry。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceivingBoxEntry {

    /** 箱明细内码 */
    private Object fdetailid;
    /** 明细行内码 */
    private Object fentryid;
    /** 箱号 */
    private String fboxno;
    /** 箱内数量 */
    private BigDecimal fboxqty;
    /** 批次号 */
    private String fbatchno;
    /** 箱状态 */
    private String fboxstatus;
}
