package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料明细关联子实体表 t_im_purrecbillentry_lk。
 * 字段按 ER 图全部 12 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryLk {

    /** 基本数量_原始携带值 */
    private BigDecimal fbaseqty_old;
    /** 不合格数（加） */
    private BigDecimal fk_usp_qtyfield2;
    /** 车辆/基本数量扩展字段2 */
    private BigDecimal fk_usp_baseunitqtyfield2;
    /** 源单内码 */
    private Object fsbillid;
    /** 基本数量扩展字段2_原始值 */
    private BigDecimal fk_usp_baseunitqtyfield2_old;
    /** 源单主实体编码 */
    private Object fstableid;
    /** 源单主实体内码 */
    private Object fsid;
    /** 分录行号 */
    private Integer fseq;
    /** 不合格数（加）_原始值 */
    private BigDecimal fk_usp_qtyfield2_old;
    /** 基本数量_确认携带值 */
    private BigDecimal fbaseqty;
    /** 明细行内码 */
    private Object fentryid;
    /** 主键 fpkid */
    private Object fpkid;
}
