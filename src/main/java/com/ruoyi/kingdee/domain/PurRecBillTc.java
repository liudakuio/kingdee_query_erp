package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 采购收货单关联追踪表 t_im_purrecbill_tc。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillTc {

    /** 主表内码 */
    private Object fid;
    /** 追踪号 */
    private String ftrackno;
    /** 追踪状态 */
    private String ftrackstatus;
}
