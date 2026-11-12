package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 采购收货单关联子实体表 t_im_purrecbill_lk。
 * 字段按 ER 图全部 6 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillLk {

    /** 主表内码 */
    private Object fid;
    /** 源单内码 */
    private Object fsbillid;
    /** 源单主实体编码 */
    private Object fstableid;
    /** 源单主实体内码 */
    private Object fsid;
    /** 分录行号 */
    private Integer fseq;
    /** 主键 fpkid */
    private Object fpkid;
}
