package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 单号联想 / 模糊查询返回的候选单据。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BillCandidate {

    private Object fid;
    private String billNo;
    private String label;
    private String status;
    private String bizDate;

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBizDate() {
        return bizDate;
    }

    public void setBizDate(String bizDate) {
        this.bizDate = bizDate;
    }
}
