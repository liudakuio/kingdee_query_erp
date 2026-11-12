package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/** 采购收货单物料明细表 t_im_purrecbillentry。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntry {

    /** 明细行内码 */
    private Object fentryid;
    /** 主表内码 */
    private Object fid;
    /** 行序号 */
    private Integer fseq;
    /** 物料编码/内码 */
    private String fmaterialid;
    /** 物料名称 */
    private String fmaterialname;
    /** 数量 */
    private BigDecimal fqty;
    /** 仓库内码 */
    private String fwarehouseid;
    /** 收货通知单号 */
    private String fk_usp_deliverybill;
    /** 检验单据编号 */
    private String fk_usp_inspect_doc_no;
}
