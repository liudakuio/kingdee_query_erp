package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 收货明细附件清单子单据体 tk_usp_cgshentryattach。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CgShEntryAttach {

    /** 附件明细内码，关联附件表的 fdetailid */
    private Object fdetailid;
    /** 明细行内码 */
    private Object fentryid;
    /** 附件名称 */
    private String fattachname;
    /** 附件类型 */
    private String fattachtype;
    /** 是否必传 */
    private String frequired;
    /** 检查结果 */
    private String fcheckresult;
}
