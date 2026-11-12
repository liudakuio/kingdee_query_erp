package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/** 物料明细分表 t_im_purrecbillentry_r。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryR {

    /** 明细行内码 */
    private Object fentryid;
    /** 基本数量 */
    private BigDecimal fbaseqty;
    /** 单价 */
    private BigDecimal fprice;
    /** 金额 */
    private BigDecimal famount;
    /** 税率 */
    private BigDecimal ftaxrate;
}
