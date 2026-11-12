package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 采购收货单关联追踪表 t_im_purrecbill_tc。
 * 字段按 ER 图全部 7 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillTc {

    /** 主表内码 */
    private Object fid;
    /** 目标单内码 */
    private Object ftbillid;
    /** 目标单实体编码 */
    private Object fttableid;
    /** 源单内码 */
    private Object fsbillid;
    /** 源单主实体编码 */
    private Object fstableid;
    /** 源单主实体内码 */
    private Object fsid;
    /** 目标单分录内码 */
    private Object ftid;
}
