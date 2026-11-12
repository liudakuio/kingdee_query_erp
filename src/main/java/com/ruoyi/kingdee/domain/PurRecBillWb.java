package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购收货单反写记录表 t_im_purrecbill_wb。
 * 字段按 ER 图全部 10 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillWb {

    /** 主表内码 */
    private Object fid;
    /** 反写操作 */
    private String foperate;
    /** 反写规则版本 */
    private Object fruleverid;
    /** 源单内码 */
    private Object fsbillid;
    /** 源单主实体编码 */
    private Object fstableid;
    /** 源单主实体内码 */
    private Object fsid;
    /** 反写值 */
    private BigDecimal fwritevalue;
    /** 分录行号 */
    private Integer fseq;
    /** 明细行内码 */
    private Object fentryid;
    /** 反写规则项 */
    private Object fruleitemid;
}
