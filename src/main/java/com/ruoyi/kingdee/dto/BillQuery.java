package com.ruoyi.kingdee.dto;

import lombok.Data;

/**
 * 单档案查询条件。
 *
 * <p>用于替代 Controller 中散落的 billNo、fid、table 参数；相同查询条件在多个接口复用，
 * 后续增加过滤项时只需改这里一处。fid 在 VO 边界统一以字符串表示，避免前端精度丢失。</p>
 */
@Data
public class BillQuery {

    /** 单据编号，与 fid 二选一；同时传入时优先按 fid 精确查询。 */
    private String billNo;

    /** 主表内码，单据编号重复时用于精确选择。 */
    private String fid;

    /** 指定导出的表名，留空或 all 表示全部表。 */
    private String table;
}
