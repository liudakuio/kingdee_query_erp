package com.ruoyi.kingdee.service.fix;

import com.ruoyi.kingdee.constant.BoxNoColumnAliases;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 上传箱号 Excel 的一行数据。
 *
 * <p>Excel 表头由用户提供，原始列保留在 values 中；匹配键与正确箱号在解析阶段提取，
 * 业务处理器不再直接遍历动态 Map。</p>
 */
@Data
public class UploadedBoxNoRow {

    /** 用于与当前行匹配的 fentryid，找不到时为 null。 */
    private String matchKey;

    /** 上传表中的正确箱号，找不到时为 null。 */
    private String boxNo;

    /** 原始列值，key = Excel 表头（已去空格）。 */
    private Map<String, String> values = new LinkedHashMap<>();

    /**
     * 从 Excel 原始行构造实体。
     *
     * @param values 列名到单元格文本的映射
     */
    public static UploadedBoxNoRow of(Map<String, String> values) {
        UploadedBoxNoRow row = new UploadedBoxNoRow();
        row.setValues(values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values));
        row.setMatchKey(valueOfColumn(values, BoxNoColumnAliases.MATCH_KEYS));
        row.setBoxNo(valueOfColumn(values, BoxNoColumnAliases.BOX_NO_KEYS));
        return row;
    }

    /** 按候选列名依次取第一个非空值；列名大小写不敏感。 */
    public static String valueOfColumn(Map<String, ?> row, List<String> candidates) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        for (String candidate : candidates) {
            for (Map.Entry<String, ?> entry : row.entrySet()) {
                String name = entry.getKey();
                if (name == null || !name.trim().equalsIgnoreCase(candidate)) {
                    continue;
                }
                Object value = entry.getValue();
                if (value != null && !String.valueOf(value).trim().isEmpty()) {
                    return String.valueOf(value).trim();
                }
            }
        }
        return null;
    }
}
