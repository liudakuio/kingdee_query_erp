package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/** 物料明细关联子实体表 t_im_purrecbillentry_lk。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryLk {

    private Object fpkid;
    private Object fentryid;
    private String fsourceentryid;
    private BigDecimal flinkqty;

    public Object getFpkid() {
        return fpkid;
    }

    public void setFpkid(Object fpkid) {
        this.fpkid = fpkid;
    }

    public Object getFentryid() {
        return fentryid;
    }

    public void setFentryid(Object fentryid) {
        this.fentryid = fentryid;
    }

    public String getFsourceentryid() {
        return fsourceentryid;
    }

    public void setFsourceentryid(String fsourceentryid) {
        this.fsourceentryid = fsourceentryid;
    }

    public BigDecimal getFlinkqty() {
        return flinkqty;
    }

    public void setFlinkqty(BigDecimal flinkqty) {
        this.flinkqty = flinkqty;
    }
}
