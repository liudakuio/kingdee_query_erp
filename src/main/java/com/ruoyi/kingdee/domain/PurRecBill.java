package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 采购收货单主表 t_im_purrecbill。
 * 字段名与数据库列名保持一致，供 MyBatis 自动映射及前端按列字典渲染。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBill {

    private Object fid;
    private String fbillno;
    private String fbizdate;
    private String fstatus;
    private String fsuppliername;
    private String fcreatorname;
    private String fcreatetime;
    private String fremark;

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
    }

    public String getFbillno() {
        return fbillno;
    }

    public void setFbillno(String fbillno) {
        this.fbillno = fbillno;
    }

    public String getFbizdate() {
        return fbizdate;
    }

    public void setFbizdate(String fbizdate) {
        this.fbizdate = fbizdate;
    }

    public String getFstatus() {
        return fstatus;
    }

    public void setFstatus(String fstatus) {
        this.fstatus = fstatus;
    }

    public String getFsuppliername() {
        return fsuppliername;
    }

    public void setFsuppliername(String fsuppliername) {
        this.fsuppliername = fsuppliername;
    }

    public String getFcreatorname() {
        return fcreatorname;
    }

    public void setFcreatorname(String fcreatorname) {
        this.fcreatorname = fcreatorname;
    }

    public String getFcreatetime() {
        return fcreatetime;
    }

    public void setFcreatetime(String fcreatetime) {
        this.fcreatetime = fcreatetime;
    }

    public String getFremark() {
        return fremark;
    }

    public void setFremark(String fremark) {
        this.fremark = fremark;
    }
}
