package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收货箱明细子单据体 tk_usp_receivingboxentry。
 * 字段按《金蝶采购收货单数据库 ER 图》全部 9 列定义，顺序与 ER 图一致。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceivingBoxEntry {

    /** 发货箱件/袋号 */
    private String fk_usp_sendboxno;
    /** 集装箱号 */
    private String fk_usp_wxno;
    /** 源单ID */
    private Object fk_usp_srcsubentryid;
    /** 分录行号 */
    private Integer fseq;
    /** 是否到达 */
    private String fk_usp_arrived;
    /** 实收数量 */
    private BigDecimal fk_usp_realqty;
    /** 箱明细内码（主键） */
    private Object fdetailid;
    /** 应收数量 */
    private BigDecimal fk_usp_detailqty;
    /** 明细行内码，关联 t_im_purrecbillentry.fentryid */
    private Object fentryid;
}
