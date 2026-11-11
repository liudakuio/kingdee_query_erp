package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 采购收货单关联追踪表 t_im_purrecbill_tc。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillTc {

    private Object fid;
    private String ftrackno;
    private String ftrackstatus;

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
    }

    public String getFtrackno() {
        return ftrackno;
    }

    public void setFtrackno(String ftrackno) {
        this.ftrackno = ftrackno;
    }

    public String getFtrackstatus() {
        return ftrackstatus;
    }

    public void setFtrackstatus(String ftrackstatus) {
        this.ftrackstatus = ftrackstatus;
    }
}
