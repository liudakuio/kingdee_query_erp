package com.ruoyi.kingdee.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 实体结果集组装工具：关联键统一成字符串，并按 entity 类型提供稳定的列顺序。
 * 数据库查询结果已由 MyBatis 映射为 entity，这里不再做 Map 行的键值归一化。
 */
public final class RowUtils {

    private static final Map<Class<?>, List<String>> COLUMN_CACHE = new ConcurrentHashMap<>();

    private RowUtils() {
    }

    /** entity 声明的字段即该表的列集合，保持声明顺序，供前端生成表头。 */
    public static List<String> columnsOf(Class<?> entityType) {
        List<String> cached = COLUMN_CACHE.get(entityType);
        if (cached != null) {
            return cached;
        }
        List<String> columns = new ArrayList<>();
        for (Field field : entityType.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                columns.add(field.getName());
            }
        }
        List<String> result = Collections.unmodifiableList(columns);
        COLUMN_CACHE.putIfAbsent(entityType, result);
        return result;
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

    /** 取某列（getter）去重后的值，保持首次出现顺序。 */
    public static <T> List<Object> distinctValues(List<T> rows, Function<T, ?> getter) {
        Set<Object> values = new LinkedHashSet<>();
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>(values);
        }
        for (T row : rows) {
            Object value = getter.apply(row);
            if (value != null && !String.valueOf(value).trim().isEmpty()) {
                values.add(value);
            }
        }
        return new ArrayList<>(values);
    }

    /** 按关联键（getter）把 entity 列表分组。 */
    public static <T> Map<String, List<T>> groupBy(List<T> rows, Function<T, ?> getter) {
        Map<String, List<T>> grouped = new LinkedHashMap<>();
        if (rows == null || rows.isEmpty()) {
            return grouped;
        }
        for (T row : rows) {
            String key = keyOf(getter.apply(row));
            if (key == null) {
                continue;
            }
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        return grouped;
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
}
