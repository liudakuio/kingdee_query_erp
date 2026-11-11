package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 物料明细多语言表 t_im_purrecbillentry_l。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntryL {

    private Object fentryid;
    private String flocale;
    private String fname;

    public Object getFentryid() {
        return fentryid;
    }

    public void setFentryid(Object fentryid) {
        this.fentryid = fentryid;
    }

    public String getFlocale() {
        return flocale;
    }

    public void setFlocale(String flocale) {
        this.flocale = flocale;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }
}
