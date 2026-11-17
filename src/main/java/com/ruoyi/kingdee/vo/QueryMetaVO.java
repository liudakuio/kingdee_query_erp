package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

/**
 * 查询元信息：展示本次查询的时间、耗时和数据源模式。
 */
@Data
@JsonPropertyOrder({"queriedAt", "elapsedMs", "mode", "tableCount"})
public class QueryMetaVO {

    /** 查询完成时间，格式 yyyy-MM-dd HH:mm:ss。 */
    private String queriedAt;

    /** 查询耗时，单位毫秒。 */
    private long elapsedMs;

    /** 数据源模式：kingdee 或 demo。 */
    private String mode;

    /** 当前业务涉及的表数量。 */
    private int tableCount;
}
