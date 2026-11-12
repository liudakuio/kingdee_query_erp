package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 物料明细多语言表 t_im_purrecbillentry_l。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryL {

    /** 明细行内码 */
    private Object fentryid;
    /** 语言 */
    private String flocale;
    /** 名称 */
    private String fname;
}
