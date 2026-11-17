package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ruoyi.kingdee.domain.PurRecBillEntry;
import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import lombok.Data;

import java.util.List;

/**
 * 采购收货单物料明细聚合行：明细本身、扩展子实体、箱号、附件与统计。
 */
@Data
@JsonPropertyOrder({"fentryid", "fseq", "row", "ext", "boxes", "attachList", "stats"})
public class PurReceiveEntryVO {

    /** 明细行内码（字符串形式）。 */
    private String fentryid;

    /** 分录行号。 */
    private Integer fseq;

    /** 物料明细实体行。 */
    private PurRecBillEntry row;

    /** 明细多语言表、分表与关联子实体。 */
    private EntryExtVO ext;

    /** 当前明细对应的收货箱实体行。 */
    private List<ReceivingBoxEntry> boxes;

    /** 当前明细对应的附件清单。 */
    private List<PurReceiveAttachVO> attachList;

    /** 明细统计。 */
    private EntryStatsVO stats;
}
