package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/** 收货箱明细子单据体 tk_usp_receivingboxentry。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceivingBoxEntry {

    private Object fdetailid;
    private Object fentryid;
    private String fboxno;
    private BigDecimal fboxqty;
    private String fbatchno;
    private String fboxstatus;

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

    public String getFboxno() {
        return fboxno;
    }

    public void setFboxno(String fboxno) {
        this.fboxno = fboxno;
    }

    public BigDecimal getFboxqty() {
        return fboxqty;
    }

    public void setFboxqty(BigDecimal fboxqty) {
        this.fboxqty = fboxqty;
    }

    public String getFbatchno() {
        return fbatchno;
    }

    public void setFbatchno(String fbatchno) {
        this.fbatchno = fbatchno;
    }

    public String getFboxstatus() {
        return fboxstatus;
    }

    public void setFboxstatus(String fboxstatus) {
        this.fboxstatus = fboxstatus;
    }
}
