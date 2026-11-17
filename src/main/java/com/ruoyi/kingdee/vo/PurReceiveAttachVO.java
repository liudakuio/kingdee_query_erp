package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ruoyi.kingdee.domain.CgEntryAttachment;
import com.ruoyi.kingdee.domain.CgShEntryAttach;
import lombok.Data;

import java.util.List;

/**
 * 采购收货单附件清单聚合行。
 */
@Data
@JsonPropertyOrder({"fdetailid", "row", "files"})
public class PurReceiveAttachVO {

    /** 附件清单内码（字符串形式）。 */
    private String fdetailid;

    /** 附件清单实体行。 */
    private CgShEntryAttach row;

    /** 该清单下的附件实体行。 */
    private List<CgEntryAttachment> files;
}
