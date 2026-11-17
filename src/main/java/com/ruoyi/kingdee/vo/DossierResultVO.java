package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 单档案查询结果的公共字段。
 *
 * <p>found、ambiguous、candidates 始终输出；message 与业务明细在对应场景才输出，
 * 因此类级使用 NON_NULL 保持原 Map 响应中“字段缺失”的行为。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"found", "ambiguous", "candidates", "message", "billNo", "fid",
        "bill", "tables", "entries", "summary", "warnings", "meta", "error"})
public class DossierResultVO {

    /** 是否命中完整档案。 */
    private boolean found;

    /** 同一单号是否存在多条记录。 */
    private boolean ambiguous;

    /** 候选单据列表。 */
    private List<BillCandidateVO> candidates = Collections.emptyList();

    /** 未命中、重复或联想时的提示信息。 */
    private String message;

    /** 单据编号。 */
    private String billNo;

    /** 主表内码（字符串形式）。 */
    private String fid;

    /** 按物理表名分组的数据块；导出层也读取该字段，不再依赖 Map key。 */
    private Map<String, TableSectionVO> tables;

    /** 查询降级告警。 */
    private List<String> warnings;

    /** 查询元信息。 */
    private QueryMetaVO meta;

    /** 批量查询中单项失败标记；正常项缺失该字段。 */
    private Boolean error;
}
