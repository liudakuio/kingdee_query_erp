package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 收货明细附件表 tk_usp_cgentryattachment。
 * 字段按 ER 图全部 3 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CgEntryAttachment {

    /** 基础资料ID */
    private Object fbasedataid;
    /** 附件明细内码，关联附件清单的 fdetailid */
    private Object fdetailid;
    /** 主键 fpkid */
    private Object fpkid;
}
