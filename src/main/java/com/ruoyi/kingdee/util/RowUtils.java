package com.ruoyi.kingdee.util;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 动态列结果集工具：不同数据库返回的列名大小写、数值/时间类型不一致，这里统一成前端可直接渲染的形态。
 */
public final class RowUtils {

    private RowUtils() {
    }

    public static List<Map<String, Object>> normalize(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            if (row != null) {
                for (Map.Entry<String, Object> cell : row.entrySet()) {
                    String column = cell.getKey() == null ? "unknown" : cell.getKey().toLowerCase(Locale.ROOT);
                    normalized.put(column, normalizeValue(cell.getValue()));
                }
            }
            result.add(normalized);
        }
        return result;
    }

    public static Object normalizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
        if (value instanceof Timestamp) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Timestamp) value);
        }
        if (value instanceof java.sql.Date) {
            return value.toString();
        }
        if (value instanceof java.util.Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((java.util.Date) value);
        }
        if (value instanceof Temporal) {
            return value.toString().replace('T', ' ');
        }
        if (value instanceof byte[]) {
            return "[二进制数据 " + ((byte[]) value).length + " 字节]";
        }
        if (value instanceof Clob) {
            return readClob((Clob) value);
        }
        if (value instanceof java.sql.Blob) {
            return "[BLOB 数据]";
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character) {
            return value;
        }
        return String.valueOf(value);
    }

    private static String readClob(Clob clob) {
        try {
            long length = clob.length();
            return length <= 0 ? "" : clob.getSubString(1, (int) Math.min(length, 2000));
        } catch (Exception e) {
            return "[CLOB 数据读取失败]";
        }
    }

    /** 结果集所有行出现过的列，保持首次出现顺序。 */
    public static List<String> columnsOf(List<Map<String, Object>> rows) {
        Set<String> columns = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            columns.addAll(row.keySet());
        }
        return new ArrayList<>(columns);
    }

    /** 关联键统一转字符串，避免 BigDecimal(1001) 与 Long(1001) 分组不到一起。 */
    public static String keyOf(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).stripTrailingZeros().toPlainString();
        }
        String key = String.valueOf(value).trim();
        if (key.endsWith(".0")) {
            key = key.substring(0, key.length() - 2);
        }
        return key;
    }

    /** 内码为数字时按数值绑定（PostgreSQL int8 = varchar 会报类型错误），否则按字符串绑定。 */
    public static Object keyParam(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.matches("-?\\d{1,18}")) {
            return Long.valueOf(trimmed);
        }
        return trimmed;
    }

    public static List<Object> distinctValues(List<Map<String, Object>> rows, String column) {
        Set<Object> values = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            Object value = row.get(column);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                values.add(value);
            }
        }
        return new ArrayList<>(values);
    }

    public static <T> List<List<T>> partition(List<T> source, int size) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        int batchSize = size <= 0 ? 500 : size;
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < source.size(); i += batchSize) {
            batches.add(new ArrayList<>(source.subList(i, Math.min(i + batchSize, source.size()))));
        }
        return batches;
    }

    public static Map<String, List<Map<String, Object>>> groupBy(List<Map<String, Object>> rows, String column) {
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = keyOf(row.get(column));
            if (key == null) {
                continue;
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        return grouped;
    }
}
