package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/** 采购收货单物料明细表 t_im_purrecbillentry。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillEntry {

    private Object fentryid;
    private Object fid;
    private Integer fseq;
    private String fmaterialid;
    private String fmaterialname;
    private BigDecimal fqty;
    private String fwarehouseid;
    private String fk_usp_deliverybill;
    private String fk_usp_inspect_doc_no;

    public Object getFentryid() {
        return fentryid;
    }

    public void setFentryid(Object fentryid) {
        this.fentryid = fentryid;
    }

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
    }

    public Integer getFseq() {
        return fseq;
    }

    public void setFseq(Integer fseq) {
        this.fseq = fseq;
    }

    public String getFmaterialid() {
        return fmaterialid;
    }

    public void setFmaterialid(String fmaterialid) {
        this.fmaterialid = fmaterialid;
    }

    public String getFmaterialname() {
        return fmaterialname;
    }

    public void setFmaterialname(String fmaterialname) {
        this.fmaterialname = fmaterialname;
    }

    public BigDecimal getFqty() {
        return fqty;
    }

    public void setFqty(BigDecimal fqty) {
        this.fqty = fqty;
    }

    public String getFwarehouseid() {
        return fwarehouseid;
    }

    public void setFwarehouseid(String fwarehouseid) {
        this.fwarehouseid = fwarehouseid;
    }

    public String getFk_usp_deliverybill() {
        return fk_usp_deliverybill;
    }

    public void setFk_usp_deliverybill(String fk_usp_deliverybill) {
        this.fk_usp_deliverybill = fk_usp_deliverybill;
    }

    public String getFk_usp_inspect_doc_no() {
        return fk_usp_inspect_doc_no;
    }

    public void setFk_usp_inspect_doc_no(String fk_usp_inspect_doc_no) {
        this.fk_usp_inspect_doc_no = fk_usp_inspect_doc_no;
    }
}
