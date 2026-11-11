package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 收货明细附件清单子单据体 tk_usp_cgshentryattach。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CgShEntryAttach {

    private Object fdetailid;
    private Object fentryid;
    private String fattachname;
    private String fattachtype;
    private String frequired;
    private String fcheckresult;

    public Object getFdetailid() {
        return fdetailid;
    }

    public void setFdetailid(Object fdetailid) {
        this.fdetailid = fdetailid;
    }

    public Object getFentryid() {
        return fentryid;
    }

    public void setFentryid(Object fentryid) {
        this.fentryid = fentryid;
    }

    public String getFattachname() {
        return fattachname;
    }

    public void setFattachname(String fattachname) {
        this.fattachname = fattachname;
    }

    public String getFattachtype() {
        return fattachtype;
    }

    public void setFattachtype(String fattachtype) {
        this.fattachtype = fattachtype;
    }

    public String getFrequired() {
        return frequired;
    }

    public void setFrequired(String frequired) {
        this.frequired = frequired;
    }

    public String getFcheckresult() {
        return fcheckresult;
    }

    public void setFcheckresult(String fcheckresult) {
        this.fcheckresult = fcheckresult;
    }
}
