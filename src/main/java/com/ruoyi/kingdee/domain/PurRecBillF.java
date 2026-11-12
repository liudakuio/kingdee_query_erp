package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购收货单分表 t_im_purrecbill_f。
 * 字段按 ER 图全部 11 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillF {

    /** 主表内码 */
    private Object fid;
    /** 汇率 */
    private BigDecimal fexchangerate;
    /** 含税 */
    private String fistax;
    /** 付款方式 */
    private String fpaymode;
    /** 汇率表 */
    private Object fexratetable;
    /** 发票类别 */
    private Object finvoicebiztypeid;
    /** 付款条件 */
    private Object fpayconditionid;
    /** 汇率日期 */
    private String fexratedate;
    /** 本位币 */
    private Object fcurrencyid;
    /** 结算币 */
    private Object fsettlecurrencyid;
    /** 结算方式 */
    private Object fsettletypeid;
}
