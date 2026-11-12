package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 物料明细多语言表 t_im_purrecbillentry_l。
 * 字段按 ER 图全部 4 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryL {

    /** 供货地址 */
    private String fprovideraddress;
    /** 语言 */
    private String flocaleid;
    /** 主键 fpkid */
    private String fpkid;
    /** 明细行内码 */
    private Object fentryid;
}
