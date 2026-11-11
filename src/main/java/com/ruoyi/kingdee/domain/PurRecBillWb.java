package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/** 采购收货单反写记录表 t_im_purrecbill_wb。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillWb {

    private Object fid;
    private String fsrcbillno;
    private BigDecimal fwritebackqty;
    private String fwritebacktime;

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
    }

    public String getFsrcbillno() {
        return fsrcbillno;
    }

    public void setFsrcbillno(String fsrcbillno) {
        this.fsrcbillno = fsrcbillno;
    }

    public BigDecimal getFwritebackqty() {
        return fwritebackqty;
    }

    public void setFwritebackqty(BigDecimal fwritebackqty) {
        this.fwritebackqty = fwritebackqty;
    }

    public String getFwritebacktime() {
        return fwritebacktime;
    }

    public void setFwritebacktime(String fwritebacktime) {
        this.fwritebacktime = fwritebacktime;
    }
}
