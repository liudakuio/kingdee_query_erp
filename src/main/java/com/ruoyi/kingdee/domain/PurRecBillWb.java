package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/** 采购收货单反写记录表 t_im_purrecbill_wb。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillWb {

    /** 主表内码 */
    private Object fid;
    /** 反写来源单号 */
    private String fsrcbillno;
    /** 反写数量 */
    private BigDecimal fwritebackqty;
    /** 反写时间 */
    private String fwritebacktime;
}
