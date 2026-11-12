package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 收货明细附件清单子单据体 tk_usp_cgshentryattach。
 * 字段按 ER 图全部 11 列定义。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CgShEntryAttach {

    /** 放行 */
    private String fk_usp_is_special;
    /** 文件/文件夹ID */
    private Object fk_usp_fileid;
    /** 下载链接 */
    private String fk_usp_download_link;
    /** 是否必传 */
    private String fk_usp_checkboxfield;
    /** 是否文件夹 */
    private String fk_usp_isfolder;
    /** 分录行号 */
    private Integer fseq;
    /** 附件明细内码（主键），关联附件表的 fdetailid */
    private Object fdetailid;
    /** 是否上传 */
    private String fk_usp_isupload;
    /** 明细行内码 */
    private Object fentryid;
    /** 附件名称 */
    private String fk_usp_attachname;
    /** 预览链接 */
    private String fk_usp_pre_link;
}
