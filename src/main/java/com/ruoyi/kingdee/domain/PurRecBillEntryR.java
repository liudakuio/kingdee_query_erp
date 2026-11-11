package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/** 物料明细分表 t_im_purrecbillentry_r。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryR {

    private Object fentryid;
    private BigDecimal fbaseqty;
    private BigDecimal fprice;
    private BigDecimal famount;
    private BigDecimal ftaxrate;

    public Object getFentryid() {
        return fentryid;
    }

    public void setFentryid(Object fentryid) {
        this.fentryid = fentryid;
    }

    public BigDecimal getFbaseqty() {
        return fbaseqty;
    }

    public void setFbaseqty(BigDecimal fbaseqty) {
        this.fbaseqty = fbaseqty;
    }

    public BigDecimal getFprice() {
        return fprice;
    }

    public void setFprice(BigDecimal fprice) {
        this.fprice = fprice;
    }

    public BigDecimal getFamount() {
        return famount;
    }

    public void setFamount(BigDecimal famount) {
        this.famount = famount;
    }

    public BigDecimal getFtaxrate() {
        return ftaxrate;
    }

    public void setFtaxrate(BigDecimal ftaxrate) {
        this.ftaxrate = ftaxrate;
    }
}
