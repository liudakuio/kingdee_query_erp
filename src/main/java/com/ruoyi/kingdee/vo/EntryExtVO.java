package com.ruoyi.kingdee.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 明细行的扩展关系集合。
 *
 * <p>扩展表的表名由业务 Schema 决定，JSON 中仍以表名为动态 key；
 * Java 侧用专用方法写入，避免业务代码到处拼 Map。</p>
 */
public class EntryExtVO {

    private final Map<String, List<Object>> sections = new LinkedHashMap<>();

    /**
     * 写入一张扩展表的行数据。
     *
     * @param tableName 扩展表物理表名
     * @param rows      该表的实体行
     */
    public void put(String tableName, List<?> rows) {
        sections.put(tableName, rows == null ? new ArrayList<>() : new ArrayList<>(rows));
    }

    /** 按原 JSON 契约把 tableName 展开为动态字段。 */
    @JsonAnyGetter
    public Map<String, List<Object>> sections() {
        return sections;
    }
}
