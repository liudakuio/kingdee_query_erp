package com.ruoyi.kingdee.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购物流单-主表 tk_usp_express。
 * ER 图共 32 列，此处按业务需要保留承运、运输、地址与审计等 25 列白名单。
 */
@Data
@TableName("tk_usp_express")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Express {

    /** 主表内码 */
    @TableId(value = "fid", type = IdType.INPUT)
    private Object fid;
    /** 单据编号 */
    private String fbillno;
    /** 单据状态 */
    private String fbillstatus;
    /** 承运商名称 */
    private String fk_usp_carrier_name;
    /** 承运商联系人 */
    private String fk_usp_carrierperson;
    /** 司机电话 */
    private String fk_usp_driverphone;
    /** 联系电话 */
    private String fk_usp_telephonefield;
    /** 运输方式 */
    private String fk_usp_radiooptgroupfield;
    /** 出发地 */
    private String fk_usp_starting;
    /** 目的地 */
    private String fk_usp_destination;
    /** 交货地点 */
    private String fk_usp_combofield2;
    /** 交货港口 */
    private String fk_usp_textfield;
    /** 提货单号 */
    private String fk_usp_ladingno;
    /** 运输工具编号 */
    private String fk_usp_transportnumber;
    /** 公里数 */
    private BigDecimal fk_usp_kilometer;
    /** 供应商 */
    private Object fk_usp_basedatafield;
    /** 采购组织 */
    private Object forgid;
    /** 交货日期 */
    private String fk_usp_delivery_date;
    /** 单据类型 */
    private Object fk_usp_billtypefield;
    /** 创建人 */
    private Object fcreatorid;
    /** 创建时间 */
    private String fcreatetime;
    /** 修改人 */
    private Object fmodifierid;
    /** 修改时间 */
    private String fmodifytime;
    /** 审核人 */
    private Object fauditorid;
    /** 审核日期 */
    private String fauditdate;
}
