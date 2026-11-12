package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/** 物料明细关联子实体表 t_im_purrecbillentry_lk。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryLk {

    /** 主键 fpkid */
    private Object fpkid;
    /** 明细行内码 */
    private Object fentryid;
    /** 来源明细内码 */
    private String fsourceentryid;
    /** 关联数量 */
    private BigDecimal flinkqty;
}
