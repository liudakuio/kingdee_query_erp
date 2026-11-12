package com.ruoyi.kingdee.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 采购收货单主表 t_im_purrecbill。
 * 字段名与数据库列名保持一致，供 MyBatis 自动映射及前端按列字典渲染。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurRecBill {

    /** 主表内码，数字或字符串均由数据库列类型决定 */
    private Object fid;
    /** 单据编号 */
    private String fbillno;
    /** 业务日期 */
    private String fbizdate;
    /** 单据状态 */
    private String fstatus;
    /** 供应商 */
    private String fsuppliername;
    /** 制单人 */
    private String fcreatorname;
    /** 创建时间 */
    private String fcreatetime;
    /** 备注 */
    private String fremark;
}
