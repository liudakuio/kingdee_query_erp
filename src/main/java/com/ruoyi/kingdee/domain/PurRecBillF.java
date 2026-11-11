package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/** 采购收货单分表 t_im_purrecbill_f。 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBillF {

    private Object fid;
    private String fcustomfield1;
    private String fcustomfield2;

    public Object getFid() {
        return fid;
    }

    public void setFid(Object fid) {
        this.fid = fid;
    }

    public String getFcustomfield1() {
        return fcustomfield1;
    }

    public void setFcustomfield1(String fcustomfield1) {
        this.fcustomfield1 = fcustomfield1;
    }

    public String getFcustomfield2() {
        return fcustomfield2;
    }

    public void setFcustomfield2(String fcustomfield2) {
        this.fcustomfield2 = fcustomfield2;
    }
}
