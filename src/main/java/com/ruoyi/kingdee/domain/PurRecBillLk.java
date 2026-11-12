package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 采购收货单关联子实体表 t_im_purrecbill_lk。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillLk {

    /** 主表内码 */
    private Object fid;
    /** 来源单据编号 */
    private String fsourcebillno;
    /** 来源单据类型 */
    private String fsourcebilltype;
}
