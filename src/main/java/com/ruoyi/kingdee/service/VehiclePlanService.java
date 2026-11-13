package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.constant.VehiclePlanSchema;
import com.ruoyi.kingdee.constant.VehiclePlanSchema.TableDef;
import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.domain.VehicleEntry;
import com.ruoyi.kingdee.domain.VehiclePlan;
import com.ruoyi.kingdee.domain.VehiclePlanTc;
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
 * 采购车辆计划查询：按 fid（主表→物料信息→箱件信息）与 fid（主表→车辆明细）两层关联组装档案。
 * 返回结构与采购收货单一致，便于前端复用同一套展示组件。
 */
@Service
public class VehiclePlanService {

    private static final Logger log = LoggerFactory.getLogger(VehiclePlanService.class);

    private final VehiclePlanMapper mapper;
    private final KingdeeProperties properties;
    private final Environment environment;

    public VehiclePlanService(VehiclePlanMapper mapper, KingdeeProperties properties, Environment environment) {
        this.mapper = mapper;
        this.properties = properties;
        this.environment = environment;
    }

    /** 表字典、关联关系与字段标签，前端用于生成动态列标题。 */
    public Map<String, Object> meta() {
        List<Map<String, Object>> tables = new ArrayList<>();
        for (TableDef def : VehiclePlanSchema.tableDefs()) {
            tables.add(def.toMap());
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("tables", tables);
        meta.put("relations", VehiclePlanSchema.relations());
        meta.put("columnLabels", VehiclePlanSchema.columnLabels());
        meta.put("tableCount", tables.size());
        return meta;
    }

    /**
     * 按单据编号（或主表内码）查询整张采购车辆计划档案。
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
            validateBillNo(no);
        }
        if (fidParam == null && no == null) {
            throw new IllegalArgumentException("请输入采购车辆计划单号");
        }

        List<VehiclePlan> plans = fidParam != null
                ? safe(VehiclePlanSchema.PLAN, () -> mapper.selectPlansByFid(fidParam, bounds(1)), warnings)
                : safe(VehiclePlanSchema.PLAN,
                () -> mapper.selectPlansByBillNo(no, bounds(properties.getMaxCandidates())), warnings);

        if (plans.isEmpty()) {
            result.put("message", fidParam != null
                    ? "未找到内码为 " + fid + " 的采购车辆计划"
                    : "未找到单据编号为 " + no + " 的采购车辆计划");
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
            throw new IllegalArgumentException("请输入采购车辆计划单号，多个单号用逗号分隔");
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
        long vehicleCount = 0;
        String mode = null;
        for (Map<String, Object> item : items) {
            if (Boolean.TRUE.equals(item.get("found"))) {
                foundCount++;
                Map<String, Object> summary = (Map<String, Object>) item.get("summary");
                if (summary != null) {
                    entryCount += longOf(summary.get("entryCount"));
                    boxCount += longOf(summary.get("boxCount"));
                    vehicleCount += longOf(summary.get("vehicleCount"));
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
        totals.put("vehicleCount", vehicleCount);

        Map<String, Object> batch = new LinkedHashMap<>();
        batch.put("total", items.size());
        batch.put("foundCount", foundCount);
        batch.put("missingCount", items.size() - foundCount);
        batch.put("totals", totals);
        batch.put("mode", mode == null ? "demo" : mode);
        batch.put("items", items);
        return batch;
    }

    private void buildDossier(VehiclePlan plan, Map<String, Object> result, List<String> warnings) {
        final Object fid = plan.getFid();

        List<VehiclePlanTc> planTc = safe(VehiclePlanSchema.PLAN_TC,
                () -> mapper.selectPlanTc(fid, bounds()), warnings);
        List<ContainerEntry> containerEntries = safe(VehiclePlanSchema.CONTAINER_ENTRY,
                () -> mapper.selectContainerEntries(fid, bounds()), warnings);
        List<VehicleEntry> vehicleEntries = safe(VehiclePlanSchema.VEHICLE_ENTRY,
                () -> mapper.selectVehicleEntries(fid, bounds()), warnings);
        List<Object> entryIds = RowUtils.distinctValues(containerEntries, ContainerEntry::getFentryid);
        // 没有物料信息时直接跳过，避免生成非法的 IN () 条件
        List<Subentryentity> subEntries = entryIds.isEmpty()
                ? Collections.emptyList()
                : safe(VehiclePlanSchema.SUB_ENTRY, () -> mapper.selectSubEntries(entryIds, bounds()), warnings);

        Map<String, List<Subentryentity>> subGroup = RowUtils.groupBy(subEntries, Subentryentity::getFentryid);

        Map<String, Object> tables = new LinkedHashMap<>();
        putSection(tables, VehiclePlanSchema.PLAN, Collections.singletonList(plan), VehiclePlan.class);
        putSection(tables, VehiclePlanSchema.PLAN_TC, planTc, VehiclePlanTc.class);
        putSection(tables, VehiclePlanSchema.VEHICLE_ENTRY, vehicleEntries, VehicleEntry.class);
        putSection(tables, VehiclePlanSchema.CONTAINER_ENTRY, containerEntries, ContainerEntry.class);
        putSection(tables, VehiclePlanSchema.SUB_ENTRY, subEntries, Subentryentity.class);

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
            ext.put(VehiclePlanSchema.SUB_ENTRY, boxes);
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
        summary.put("vehicleCount", vehicleEntries.size());
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
        TableDef def = VehiclePlanSchema.tableDef(tableName);
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

    private List<Map<String, Object>> candidatesFromPlans(List<VehiclePlan> plans) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (VehiclePlan plan : plans) {
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
        meta.put("tableCount", VehiclePlanSchema.tableDefs().size());
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

    /** 单号白名单：字母/数字/汉字及 . _ - / ，杜绝引号、空格、通配符等注入字符。 */
    private void validateBillNo(String value) {
        if (value.length() > 64) {
            throw new IllegalArgumentException("单据编号长度不能超过 64 个字符");
        }
        for (char c : value.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '.' && c != '_' && c != '-' && c != '/') {
                throw new IllegalArgumentException("单据编号包含非法字符：" + c);
            }
        }
    }

    @FunctionalInterface
    private interface SqlQuery<T> {
        List<T> execute();
    }
}
