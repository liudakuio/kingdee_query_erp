package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购收货单物料明细表 t_im_purrecbillentry。
 * ER 图共 151 列，此处按需求只保留业务上最常用的 20 列白名单。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntry {

    /** 明细行内码 */
    private Object fentryid;
    /** 主表内码 */
    private Object fid;
    /** 分录行号 */
    private Object fseq;
    /** 物料编码 */
    private Object fmaterialid;
    /** 物料名称(封存) */
    private String fmaterialname;
    /** 数量 */
    private BigDecimal fqty;
    /** 基本数量 */
    private BigDecimal fbaseqty;
    /** 单价 */
    private BigDecimal fprice;
    /** 含税单价 */
    private BigDecimal fpriceandtax;
    /** 金额 */
    private BigDecimal famount;
    /** 价税合计 */
    private BigDecimal famountandtax;
    /** 金额(本位币) */
    private BigDecimal fcuramount;
    /** 税额 */
    private BigDecimal ftaxamount;
    /** 税率% */
    private BigDecimal ftaxrate;
    /** 仓库 */
    private Object fwarehouseid;
    /** 批号 */
    private String flotnumber;
    /** 供应商发货箱号 */
    private String fk_usp_textfield1;
    /** 收货通知单号 */
    private String fk_usp_deliverybill;
    /** 检验单据编号 */
    private String fk_usp_inspect_doc_no;
    /** 备注 */
    private String fentrycomment;
}
