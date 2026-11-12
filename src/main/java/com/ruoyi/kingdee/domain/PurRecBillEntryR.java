package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料明细分表 t_im_purrecbillentry_r。
 * 字段按 ER 图全部 62 列定义，顺序与 ER 图一致；未列出的业务字段不推测。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryR {

    /** 主表内码 */
    private Object fid;
    /** 让步接收数量 */
    private BigDecimal fconcessionqty;
    /** 来源系统 */
    private String fsrcsystem;
    /** 物流单据 */
    private String flogisticsbill;
    /** 已入库基本数量 */
    private BigDecimal finvbaseqty;
    /** 核心单据实体 */
    private String fmainbillentity;
    /** 不合格数量(第三单位) */
    private BigDecimal funqualifiedunit3rd;
    /** 未核销基本数量 */
    private BigDecimal funverifybaseqty;
    /** 合格已退货数量 */
    private BigDecimal ftotalreturnqty;
    /** 合格数量(主辅单位) */
    private BigDecimal fqualifiedunit2nd;
    /** 未入库数量 */
    private BigDecimal fremaininvqty;
    /** 来源单据分录序号 */
    private Object fsrcbillentryseq;
    /** 合同行号 */
    private String fconbillrownum;
    /** 应付数量 */
    private BigDecimal fjoinpriceqty;
    /** 未转固数量 */
    private BigDecimal fremainpurqty;
    /** 合同编号 */
    private String fconbillnumber;
    /** 核心单据ID */
    private Object fmainbillid;
    /** 验收状态 */
    private String facceptanceentrystatus;
    /** 来源系统单据编号 */
    private String fsrcsysbillno;
    /** 来源单据编号 */
    private String fsrcbillnumber;
    /** 不合格数量 */
    private BigDecimal funqualifiedqty;
    /** 来源单据ID */
    private Object fsrcbillid;
    /** 已入库数量 */
    private BigDecimal finvqty;
    /** 核心单据编号 */
    private String fmainbillnumber;
    /** 物料版本 */
    private Object fmversion;
    /** 已核销基本数量 */
    private BigDecimal fverifybaseqty;
    /** 来源系统单据分录ID */
    private String fsrcsysbillentryid;
    /** 剩余应付基本数量 */
    private BigDecimal fremainjoinpricebaseqty;
    /** 剩余应付数量 */
    private BigDecimal fremainjoinpriceqty;
    /** 未核销数量 */
    private BigDecimal funverifyqty;
    /** 明细行内码 */
    private Object fentryid;
    /** 采购订单行号 */
    private Object fpurbillentryid;
    /** 合同ID */
    private Object fconbillid;
    /** 核心单据分录序号 */
    private Object fmainbillentryseq;
    /** 不合格数量(主辅单位) */
    private BigDecimal funqualifiedunit2nd;
    /** 合同实体 */
    private String fconbillentity;
    /** 合同行ID */
    private Object fconbillentryid;
    /** 应付基本数量 */
    private BigDecimal fjoinpricebaseqty;
    /** 委外工单ID */
    private Object fmftorderid;
    /** 让步接收基本数量 */
    private BigDecimal fconcessionbaseqty;
    /** 合格已退货基本数量 */
    private BigDecimal ftotalreturnbaseqty;
    /** 合格数量(第三单位) */
    private BigDecimal fqualifiedunit3rd;
    /** 合格未退货基本数量 */
    private BigDecimal fremainreturnbaseqty;
    /** 采购订单号 */
    private Object fpurbillnumber;
    /** 已核销数量 */
    private BigDecimal fverifyqty;
    /** 合格数量 */
    private BigDecimal fqualifiedqty;
    /** 委外工单分录序号 */
    private Object fmftorderentryseq;
    /** 来源单据实体 */
    private String fsrcbillentity;
    /** 已转固数量 */
    private BigDecimal fpurchasedqty;
    /** 合格基本数量 */
    private BigDecimal fqualifiedbaseqty;
    /** 委外工单行ID */
    private Object fmftorderentryid;
    /** 不合格基本数量 */
    private BigDecimal funqualifiedbaseqty;
    /** 未入库基本数量 */
    private BigDecimal fremaininvbaseqty;
    /** 来源单据行ID */
    private Object fsrcbillentryid;
    /** 采购订单号 */
    private String fpurorderbillnumber;
    /** 核心单据行ID */
    private Object fmainbillentryid;
    /** 验收日期 */
    private String facceptancedate;
    /** 已转固价税合计(本位币) */
    private BigDecimal fpurchasedamount;
    /** 未转固价税合计(本位币) */
    private BigDecimal fremainpuramount;
    /** 合格未退货数量 */
    private BigDecimal fremainreturnqty;
    /** 来源系统单据ID */
    private String fsrcsysbillid;
    /** 委外工单编号 */
    private String fmftordernumber;
}
