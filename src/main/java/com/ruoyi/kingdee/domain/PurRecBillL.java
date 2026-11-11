package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 采购收货单多语言表 t_im_purrecbill_l。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillL {

    private Object fid;
    private String flocale;
    private String fname;

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
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
