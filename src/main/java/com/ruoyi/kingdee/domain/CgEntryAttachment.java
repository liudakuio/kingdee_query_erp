package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/** 收货明细附件表 tk_usp_cgentryattachment。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CgEntryAttachment {

    /** 主键 fpkid */
    private Object fpkid;
    /** 附件明细内码 */
    private Object fdetailid;
    /** 文件名 */
    private String ffilename;
    /** 文件路径 */
    private String ffileurl;
    /** 文件大小(字节) */
    private Long ffilesize;
    /** 上传时间 */
    private String fuploadtime;
}
