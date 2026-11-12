package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 采购收货单主表 t_im_purrecbill。
 * ER 图共 48 列，此处按需求只保留业务上最常用的 15 列白名单，避免主表卡片过长。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBill {

    /** 主表内码，数字或字符串均由数据库列类型决定 */
    private Object fid;
    /** 单据编号 */
    private String fbillno;
    /** 单据状态 */
    private String fbillstatus;
    /** 业务日期 */
    private String fbiztime;
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
    /** 审核时间 */
    private String fauditdate;
    /** 单据类型 */
    private Object fbilltypeid;
    /** 供货供应商 */
    private Object fsupplierid;
    /** 收货组织 */
    private Object forgid;
    /** 验收状态 */
    private String facceptancestatus;
    /** 备注 */
    private String fcomment;
}
