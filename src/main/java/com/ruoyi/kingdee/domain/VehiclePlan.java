package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购车辆计划-主表 tk_usp_vehicleplan。
 * ER 图共 26 列，此处按业务需要只保留 16 列白名单，其余字段不展示。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehiclePlan {

    /** 主表内码 */
    private Object fid;
    /** 单据编号 */
    private String fbillno;
    /** 单据状态 */
    private String fbillstatus;
    /** 业务类型 */
    private String fk_usp_businesstype;
    /** 来源 */
    private String fk_usp_source;
    /** 目的地 */
    private String fk_usp_destination;
    /** 始发地 */
    private String fk_usp_origin;
    /** 启运地 */
    private String fk_usp_starting;
    /** 运输单号 */
    private String fk_usp_transportnumber;
    /** 发货通知单号 */
    private String fk_usp_delivernoticeno;
    /** 收货部门 */
    private Object fk_usp_receivedept;
    /** 收货组织 */
    private Object forgid;
    /** 公里数 */
    private BigDecimal fk_usp_kilometer;
    /** 组合字段 */
    private String fk_usp_combofield;
    /** 创建人 */
    private Object fcreatorid;
    /** 创建时间 */
    private String fcreatetime;
}
