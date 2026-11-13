package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 车辆明细-子表 tk_usp_vehicleentry。
 * 字段按 ER 图全部 18 列定义，通过 fid 关联采购车辆计划主表。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleEntry {

    /** 主表内码 */
    private Object fid;
    /** 修改人字段 */
    private Object fk_usp_modifierfield;
    /** 出库状态 */
    private String fk_usp_outgostate;
    /** 出库时间 */
    private String fk_usp_outgotime;
    /** 集装箱号 */
    private String fk_usp_wxnos;
    /** 文本字段 */
    private String fk_usp_textfield;
    /** 文本字段3 */
    private String fk_usp_textfield3;
    /** 文本字段1 */
    private String fk_usp_textfield1;
    /** 分录行号 */
    private Integer fseq;
    /** 入库时间 */
    private String fk_usp_incometime;
    /** 入库状态 */
    private String fk_usp_incomestate;
    /** 承运商 */
    private String fk_usp_carrier;
    /** 联系电话 */
    private String fk_usp_telephonefield;
    /** 到达日期 */
    private String fk_usp_arrivedate;
    /** 修改日期字段 */
    private String fk_usp_modifydatefield;
    /** 司机照片 */
    private String fk_usp_driverpicturer;
    /** 明细行内码 */
    private Object fentryid;
    /** 运输供应商 */
    private Object fk_usp_transportsupplier;
}
