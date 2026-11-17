package com.ruoyi.kingdee.util;

import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 查询链路的通用能力。
 *
 * <p>该类属于工具层：泛型只用于隔离“任意实体表查询”的重复代码，
 * 业务 Service 不再定义自己的泛型接口，也不再重复 try/catch、分页与空值处理。</p>
 */
public final class QuerySupport {

    private QuerySupport() {
    }

    /** 一张表的一次查询动作；只在工具层出现，业务 Service 直接传 Lambda。 */
    @FunctionalInterface
    public interface DbQuery<T> {
        List<T> execute();
    }

    /** 按一批关联键查询子表；只在工具层出现，业务 Service 直接传方法引用。 */
    @FunctionalInterface
    public interface KeyQuery<T> {
        List<T> execute(List<Object> ids, RowBounds rowBounds);
    }

    /**
     * 容错查询：单张表查询失败时记录警告并返回空列表，不影响其它表。
     *
     * @param table    表名，用于告警文案
     * @param query    实际查询
     * @param warnings 告警收集器
     * @param log      调用方日志器，便于按业务类区分日志
     */
    public static <T> List<T> safe(String table, DbQuery<T> query, List<String> warnings, Logger log) {
        try {
            List<T> rows = query.execute();
            return rows == null ? Collections.<T>emptyList() : rows;
        } catch (DataAccessException e) {
            warnings.add(rootMessage(table, e));
            log.warn("表 {} 查询失败", table, e);
            return Collections.emptyList();
        }
    }

    /** 取异常链最末端的根因，生成面向用户的简短提示。 */
    public static String rootMessage(String table, DataAccessException e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return "表 " + table + " 查询失败（可能不存在、字段名不同或无读取权限）：" + root.getMessage();
    }

    /** 构造从 0 开始、限定最大行数的 RowBounds。 */
    public static RowBounds bounds(int limit) {
        return new RowBounds(0, Math.max(1, limit));
    }

    /** 去除首尾空白；空白串统一转成 null。 */
    public static String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /** 统计值归一化为 long，非数字按 0 处理。 */
    public static long longOf(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    /** 从分组结果中安全取行；空 key 直接返回空列表。 */
    public static <T> List<T> rowsOf(java.util.Map<String, List<T>> grouped, String key) {
        return key == null ? Collections.<T>emptyList()
                : grouped.getOrDefault(key, Collections.<T>emptyList());
    }

    /**
     * 按关联键分批查询子表：空键跳过，单批失败记录告警并停止该表，超过上限时截断。
     *
     * @param table     表名，用于告警文案
     * @param ids       关联键值
     * @param query     实际查询
     * @param warnings  告警收集器
     * @param log       调用方日志器
     * @param batchSize IN 子句分批大小
     * @param maxRows   单表最大返回行数
     */
    public static <T> List<T> queryByKeys(String table, List<Object> ids, KeyQuery<T> query,
                                          List<String> warnings, Logger log, int batchSize, int maxRows) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> all = new ArrayList<>();
        for (List<Object> batch : RowUtils.partition(ids, batchSize)) {
            try {
                List<T> rows = query.execute(batch, bounds(maxRows));
                if (rows != null) {
                    all.addAll(rows);
                }
            } catch (DataAccessException e) {
                warnings.add(rootMessage(table, e));
                log.warn("表 {} 查询失败", table, e);
                break;
            }
        }
        if (all.size() >= maxRows) {
            all = new ArrayList<>(all.subList(0, maxRows));
            warnings.add("表 " + table + " 数据已截断至 " + maxRows + " 行");
        }
        return all;
    }
}
