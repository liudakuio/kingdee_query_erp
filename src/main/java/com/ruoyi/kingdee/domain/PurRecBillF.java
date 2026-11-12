package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 采购收货单分表 t_im_purrecbill_f。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillF {

    /** 主表内码 */
    private Object fid;
    /** 自定义字段 1（演示库补充字段） */
    private String fcustomfield1;
    /** 自定义字段 2（演示库补充字段） */
    private String fcustomfield2;
}
