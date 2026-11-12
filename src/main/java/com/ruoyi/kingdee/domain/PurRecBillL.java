package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 采购收货单多语言表 t_im_purrecbill_l。
 * 字段按 ER 图全部 4 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillL {

    /** 主表内码 */
    private Object fid;
    /** 备注 */
    private String fcomment;
    /** 语言 */
    private String flocaleid;
    /** 主键 fpkid */
    private String fpkid;
}
