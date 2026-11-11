package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 收货明细附件表 tk_usp_cgentryattachment。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CgEntryAttachment {

    private Object fpkid;
    private Object fdetailid;
    private String ffilename;
    private String ffileurl;
    private Long ffilesize;
    private String fuploadtime;

    public Object getFpkid() {
        return fpkid;
    }

    public void setFpkid(Object fpkid) {
        this.fpkid = fpkid;
    }

    public Object getFdetailid() {
        return fdetailid;
    }

    public void setFdetailid(Object fdetailid) {
        this.fdetailid = fdetailid;
    }

    public String getFfilename() {
        return ffilename;
    }

    public void setFfilename(String ffilename) {
        this.ffilename = ffilename;
    }

    public String getFfileurl() {
        return ffileurl;
    }

    public void setFfileurl(String ffileurl) {
        this.ffileurl = ffileurl;
    }

    public Long getFfilesize() {
        return ffilesize;
    }

    public void setFfilesize(Long ffilesize) {
        this.ffilesize = ffilesize;
    }

    public String getFuploadtime() {
        return fuploadtime;
    }

    public void setFuploadtime(String fuploadtime) {
        this.fuploadtime = fuploadtime;
    }
}
