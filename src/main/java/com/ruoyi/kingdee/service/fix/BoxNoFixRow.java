package com.ruoyi.kingdee.service.fix;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单行箱号信息的处理结果：当前行原始数据 + 命中的修正值 + 处理说明。
 */
@Data
public class BoxNoFixRow {

    /** 当前查询到的箱号信息行（key = 数据库列名） */
    private Map<String, Object> current = new LinkedHashMap<>();
    /** 上传表中命中的修正值（key = Excel 列名） */
    private Map<String, String> applied = new LinkedHashMap<>();
    /** 是否在上传表中找到对应箱号 */
    private boolean matched;
    /** 处理说明，例如「已按上传表修正」「上传表中未找到该箱号」 */
    private String message;

    public static BoxNoFixRow of(Map<String, Object> current) {
        BoxNoFixRow row = new BoxNoFixRow();
        row.setCurrent(current == null ? new LinkedHashMap<>() : current);
        return row;
    }
}