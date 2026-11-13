package com.ruoyi.kingdee.service.export;

import lombok.Data;

import java.util.Map;

/**
 * 自定义列按行取值时的上下文：该行字段（已按 JSON 规则归一化，内码为字符串）、单据信息与表名。
 * 只承载数据，不依赖 POI / Servlet / Spring，便于处理器单独测试。
 */
@Data
public class RowExportContext {

    private Map<String, Object> row;
    private String billNo;
    private Object fid;
    private String tableName;
}
