package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

/**
 * 批量查询响应：批次统计 + 每个单号的完整结果。
 */
@Data
@JsonPropertyOrder({"total", "foundCount", "missingCount", "totals", "mode", "items"})
public class BatchResultVO {

    /** 去重后的单号数量。 */
    private int total;

    /** 命中完整档案的单号数量。 */
    private int foundCount;

    /** 未命中的单号数量。 */
    private int missingCount;

    /** 各业务统计项的合计。 */
    private BatchTotalsVO totals;

    /** 数据源模式，取第一个档案的元信息。 */
    private String mode;

    /** 每个单号对应的档案结果。 */
    private List<DossierResultVO> items;
}
