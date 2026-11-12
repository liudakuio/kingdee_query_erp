package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 采购收货单多语言表 t_im_purrecbill_l。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillL {

    /** 主表内码 */
    private Object fid;
    /** 语言 */
    private String flocale;
    /** 名称 */
    private String fname;
}
