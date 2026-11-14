package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.constant.ExpressSchema;
import com.ruoyi.kingdee.constant.ExpressSchema.TableDef;
import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Express;
import com.ruoyi.kingdee.domain.ExpressTc;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.mapper.vehicleplan.ExpressMapper;
import com.ruoyi.kingdee.mapper.vehicleplan.VehiclePlanMapper;
import com.ruoyi.kingdee.util.BillNos;
import com.ruoyi.kingdee.util.RowUtils;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购物流单查询：按 fid（主表→物料明细→箱件信息）两层关联组装档案。
 * 返回结构与采购车辆计划完全同构，便于前端复用同一套展示组件。
 */
@Service
public class ExpressService {

    private static final Logger log = LoggerFactory.getLogger(ExpressService.class);

    private final ExpressMapper expressMapper;
    private final VehiclePlanMapper sharedMapper;
    private final KingdeeProperties properties;
    private final Environment environment;

    public ExpressService(ExpressMapper expressMapper, VehiclePlanMapper sharedMapper,
                          KingdeeProperties properties, Environment environment) {
        this.expressMapper = expressMapper;
        this.sharedMapper = sharedMapper;
        this.properties = properties;
        this.environment = environment;
    }

    /** 表字典、关联关系与字段标签，前端用于生成动态列标题。 */
    public Map<String, Object> meta() {
        List<Map<String, Object>> tables = new ArrayList<>();
        for (TableDef def : ExpressSchema.tableDefs()) {
            tables.add(def.toMap());
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("tables", tables);
        meta.put("relations", ExpressSchema.relations());
        meta.put("columnLabels", ExpressSchema.columnLabels());
        meta.put("tableCount", tables.size());
        return meta;
    }

    /**
     * 按单据编号（或主表内码）查询整张采购物流单档案。
     *
     * @param billNo 单据编号
     * @param fid    主表内码，单据编号重复时用于精确选择
     */
    public Map<String, Object> query(String billNo, String fid) {
        long start = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("found", false);
        result.put("ambiguous", false);
        result.put("candidates", Collections.emptyList());

        Object fidParam = RowUtils.keyParam(fid);
        String no = trimToNull(billNo);
        if (no != null) {
            BillNos.validate(no);
        }
        if (fidParam == null && no == null) {
            throw new IllegalArgumentException("请输入采购物流单号");
        }

        List<Express> plans = fidParam != null
                ? safe(ExpressSchema.EXPRESS, () -> expressMapper.selectExpressByFid(fidParam, bounds(1)), warnings)
                : safe(ExpressSchema.EXPRESS,
                () -> expressMapper.selectExpressByBillNo(no, bounds(properties.getMaxCandidates())), warnings);

        if (plans.isEmpty()) {
            result.put("message", fidParam != null
                    ? "未找到内码为 " + fid + " 的采购物流单"
                    : "未找到单据编号为 " + no + " 的采购物流单");
        } else if (fidParam == null && plans.size() > 1) {
            result.put("ambiguous", true);
            result.put("candidates", candidatesFromPlans(plans));
            result.put("message", "同一单据编号存在 " + plans.size() + " 条记录，请按内码 fid 精确选择");
        } else {
            buildDossier(plans.get(0), result, warnings);
        }
        return finish(result, start, warnings);
    }

    /**
     * 批量查询：单号以中英文逗号分隔，返回批次汇总对象 + 每个单据的完整档案。
     * 空输入或超过数量上限整体拒绝；单个单号非法 / 查询失败只标记该项。
     */
    public Map<String, Object> queryBatch(String billNos) {
        List<String> numbers = BillNos.parse(billNos);
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("请输入采购物流单号，多个单号用逗号分隔");
        }
        if (numbers.size() > properties.getMaxBatchBills()) {
            throw new IllegalArgumentException("单次最多查询 " + properties.getMaxBatchBills() + " 个单号");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (String no : numbers) {
            Map<String, Object> item;
            try {
                item = query(no, null);
                item.putIfAbsent("billNo", no);
            } catch (RuntimeException e) {
                item = new LinkedHashMap<>();
                item.put("billNo", no);
                item.put("found", false);
                item.put("ambiguous", false);
                item.put("candidates", Collections.emptyList());
                item.put("message", e.getMessage());
                item.put("error", true);
            }
            items.add(item);
        }

        int foundCount = 0;
        long entryCount = 0;
        long boxCount = 0;
        String mode = null;
        for (Map<String, Object> item : items) {
            if (Boolean.TRUE.equals(item.get("found"))) {
                foundCount++;
                Map<String, Object> summary = (Map<String, Object>) item.get("summary");
                if (summary != null) {
                    entryCount += longOf(summary.get("entryCount"));
                    boxCount += longOf(summary.get("boxCount"));
                }
            }
            if (mode == null) {
                Map<String, Object> meta = (Map<String, Object>) item.get("meta");
                if (meta != null && meta.get("mode") != null) {
                    mode = String.valueOf(meta.get("mode"));
                }
            }
        }

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("entryCount", entryCount);
        totals.put("boxCount", boxCount);

        Map<String, Object> batch = new LinkedHashMap<>();
        batch.put("total", items.size());
        batch.put("foundCount", foundCount);
        batch.put("missingCount", items.size() - foundCount);
        batch.put("totals", totals);
        batch.put("mode", mode == null ? "demo" : mode);
        batch.put("items", items);
        return batch;
    }

    private void buildDossier(Express plan, Map<String, Object> result, List<String> warnings) {
        final Object fid = plan.getFid();

        List<ExpressTc> planTc = safe(ExpressSchema.EXPRESS_TC,
                () -> expressMapper.selectExpressTc(fid, bounds()), warnings);
        List<ContainerEntry> containerEntries = safe(ExpressSchema.CONTAINER_ENTRY,
                () -> sharedMapper.selectContainerEntries(fid, bounds()), warnings);
        List<Object> entryIds = RowUtils.distinctValues(containerEntries, ContainerEntry::getFentryid);
        // 没有物料明细时跳过箱件查询，避免生成非法的 IN () 条件
        List<Subentryentity> subEntries = entryIds.isEmpty()
                ? Collections.emptyList()
                : safe(ExpressSchema.SUB_ENTRY, () -> sharedMapper.selectSubEntries(entryIds, bounds()), warnings);

        Map<String, List<Subentryentity>> subGroup = RowUtils.groupBy(subEntries, Subentryentity::getFentryid);

        Map<String, Object> tables = new LinkedHashMap<>();
        putSection(tables, ExpressSchema.EXPRESS, Collections.singletonList(plan), Express.class);
        putSection(tables, ExpressSchema.EXPRESS_TC, planTc, ExpressTc.class);
        putSection(tables, ExpressSchema.CONTAINER_ENTRY, containerEntries, ContainerEntry.class);
        putSection(tables, ExpressSchema.SUB_ENTRY, subEntries, Subentryentity.class);

        List<Map<String, Object>> entries = new ArrayList<>();
        int boxCount = 0;
        for (ContainerEntry row : containerEntries) {
            String key = RowUtils.keyOf(row.getFentryid());
            List<Subentryentity> boxes = subGroup.getOrDefault(key, Collections.emptyList());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fentryid", key);
            item.put("fseq", row.getFseq());
            item.put("row", row);

            Map<String, Object> ext = new LinkedHashMap<>();
            ext.put(ExpressSchema.SUB_ENTRY, boxes);
            item.put("ext", ext);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("boxes", boxes.size());
            item.put("stats", stats);

            entries.add(item);
            boxCount += boxes.size();
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("entryCount", entries.size());
        summary.put("boxCount", boxCount);
        summary.put("tableCount", tables.size());
        summary.put("nonEmptyTableCount", countNonEmpty(tables));

        result.put("found", true);
        result.put("billNo", plan.getFbillno());
        result.put("fid", fid);
        result.put("bill", plan);
        result.put("tables", tables);
        result.put("entries", entries);
        result.put("summary", summary);
    }

    private void putSection(Map<String, Object> tables, String tableName, List<?> rows, Class<?> entityType) {
        TableDef def = ExpressSchema.tableDef(tableName);
        Map<String, Object> section = def.toMap();
        section.put("count", rows.size());
        section.put("columns", RowUtils.columnsOf(entityType));
        section.put("rows", rows);
        section.put("truncated", rows.size() >= properties.getMaxRowsPerTable());
        tables.put(def.getName(), section);
    }

    private int countNonEmpty(Map<String, Object> tables) {
        int count = 0;
        for (Object value : tables.values()) {
            Map<?, ?> section = (Map<?, ?>) value;
            if (((Number) section.get("count")).intValue() > 0) {
                count++;
            }
        }
        return count;
    }

    private List<Map<String, Object>> candidatesFromPlans(List<Express> plans) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (Express plan : plans) {
            Map<String, Object> candidate = new LinkedHashMap<>();
            candidate.put("fid", plan.getFid());
            candidate.put("billNo", plan.getFbillno());
            candidate.put("label", plan.getFbillno() + "（内码 " + plan.getFid() + "）");
            candidate.put("status", plan.getFbillstatus());
            candidates.add(candidate);
        }
        return candidates;
    }

    private <T> List<T> safe(String table, SqlQuery<T> query, List<String> warnings) {
        try {
            List<T> rows = query.execute();
            return rows == null ? Collections.<T>emptyList() : rows;
        } catch (DataAccessException e) {
            warnings.add(rootMessage(table, e));
            log.warn("表 {} 查询失败", table, e);
            return Collections.emptyList();
        }
    }

    private String rootMessage(String table, DataAccessException e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return "表 " + table + " 查询失败（可能不存在、字段名不同或无读取权限）：" + root.getMessage();
    }

    private long longOf(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private Map<String, Object> finish(Map<String, Object> result, long start, List<String> warnings) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("queriedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        meta.put("elapsedMs", System.currentTimeMillis() - start);
        meta.put("mode", Arrays.asList(environment.getActiveProfiles()).contains("kingdee") ? "kingdee" : "demo");
        meta.put("tableCount", ExpressSchema.tableDefs().size());
        result.put("warnings", warnings);
        result.put("meta", meta);
        return result;
    }

    private RowBounds bounds() {
        return bounds(properties.getMaxRowsPerTable());
    }

    private RowBounds bounds(int limit) {
        return new RowBounds(0, Math.max(1, limit));
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    @FunctionalInterface
    private interface SqlQuery<T> {
        List<T> execute();
    }
}
