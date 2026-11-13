package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 箱件信息-子表 tk_usp_subentryentity。
 * 字段按 ER 图全部 18 列定义，通过 fentryid 关联物料信息子表。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Subentryentity {

    /** 体积 */
    private String fk_usp_volume;
    /** 发货箱号 */
    private String fk_usp_sendboxno;
    /** 毛重 */
    private String fk_usp_gross_weight;
    /** 高度 */
    private String fk_usp_height;
    /** 来源箱件内码 */
    private Object fk_usp_srcsubentryid;
    /** 分录行号 */
    private Integer fseq;
    /** 是否到达 */
    private String fk_usp_arrived;
    /** 箱内数量 */
    private BigDecimal fk_usp_detailnum;
    /** 长度 */
    private String fk_usp_length;
    /** 是否共享 */
    private String fk_usp_issh;
    /** 集装箱号 */
    private String fk_usp_wxno;
    /** 关联数量 */
    private BigDecimal fk_usp_associatedqty;
    /** 箱件内码 */
    private Object fdetailid;
    /** 应收数量 */
    private BigDecimal fk_usp_detailqty;
    /** 净重 */
    private String fk_usp_net_weight;
    /** 明细行内码 */
    private Object fentryid;
    /** 供应商批号 */
    private String fk_usp_suplot;
    /** 宽度 */
    private String fk_usp_width;
}
