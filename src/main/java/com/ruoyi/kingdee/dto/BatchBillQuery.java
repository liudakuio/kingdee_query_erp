package com.ruoyi.kingdee.dto;

import lombok.Data;

/**
 * 批量查询条件：一组以逗号分隔的单号以及可选的导出表范围。
 */
@Data
public class BatchBillQuery {

    /** 逗号分隔的单号列表，后端负责解析、去重和数量校验。 */
    private String billNos;

    /** 指定导出的表名，留空或 all 表示全部表。 */
    private String table;
}
