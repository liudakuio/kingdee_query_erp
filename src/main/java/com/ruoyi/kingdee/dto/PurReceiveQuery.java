package com.ruoyi.kingdee.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购收货单查询条件。
 *
 * <p>fuzzy=true 表示只做单号联想，不加载完整档案；把它放进查询对象，
 * 避免 Controller 用多个布尔参数表达两种不同业务语义。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurReceiveQuery extends BillQuery {

    /** 是否只返回候选单据列表。 */
    private boolean fuzzy;
}
