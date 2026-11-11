package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 采购收货单关联子实体表 t_im_purrecbill_lk。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillLk {

    private Object fid;
    private String fsourcebillno;
    private String fsourcebilltype;

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
    }

    public String getFsourcebillno() {
        return fsourcebillno;
    }

    public void setFsourcebillno(String fsourcebillno) {
        this.fsourcebillno = fsourcebillno;
    }

    public String getFsourcebilltype() {
        return fsourcebilltype;
    }

    public void setFsourcebilltype(String fsourcebilltype) {
        this.fsourcebilltype = fsourcebilltype;
    }
}
