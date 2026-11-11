package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.constant.PurReceiveSchema;
import com.ruoyi.kingdee.constant.PurReceiveSchema.TableDef;
import com.ruoyi.kingdee.mapper.PurReceiveMapper;
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
import java.util.function.BiFunction;

/**
 * 按文档给出的三个关联键逐层取数并组装完整的收货单档案：
 * fid（主表→明细） → fentryid（明细→扩展/箱唛/附件清单） → fdetailid（附件清单→附件表）。
 * 各表分开查询，避免一对多 JOIN 造成明细行重复展开。
 */
@Service
public class PurReceiveService {

    private static final Logger log = LoggerFactory.getLogger(PurReceiveService.class);

    private final PurReceiveMapper mapper;
    private final KingdeeProperties properties;
    private final Environment environment;

    public PurReceiveService(PurReceiveMapper mapper, KingdeeProperties properties, Environment environment) {
        this.mapper = mapper;
        this.properties = properties;
        this.environment = environment;
    }

    /** 供前端渲染使用的表字典、字段字典、关联关系。 */
    public Map<String, Object> meta() {
        List<Map<String, Object>> tables = new ArrayList<>();
        for (TableDef def : PurReceiveSchema.tableDefs()) {
            tables.add(def.toMap());
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("tables", tables);
        meta.put("relations", PurReceiveSchema.relations());
        meta.put("columnLabels", PurReceiveSchema.columnLabels());
        meta.put("tableCount", tables.size());
        return meta;
    }

    /** 单号联想 / 模糊查询。 */
    public List<Map<String, Object>> suggest(String keyword) {
        return suggest(keyword, new ArrayList<String>());
    }

    private List<Map<String, Object>> suggest(String keyword, List<String> warnings) {
        String value = trimToNull(keyword);
        if (value == null) {
            return Collections.emptyList();
        }
        validateBillNo(value);
        List<Map<String, Object>> rows = safe(PurReceiveSchema.BILL,
                () -> mapper.selectBillsByBillNoFuzzy("%" + value + "%", bounds(properties.getMaxCandidates())),
                warnings);
        return candidatesFromRows(rows);
    }

    /**
     * 按单号（或内码）查询整张收货单的关联数据。
     *
     * @param billNo 单据编号
     * @param fid    主表内码，用于用户从重复单号候选中精确选择
     * @param fuzzy  是否只做单号模糊匹配
     */
    public Map<String, Object> query(String billNo, String fid, boolean fuzzy) {
        long start = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("found", false);
        result.put("ambiguous", false);
        result.put("candidates", Collections.emptyList());

        String no = trimToNull(billNo);
        Object fidParam = RowUtils.keyParam(fid);
        if (no != null) {
            validateBillNo(no);
        }
        if (fidParam != null && fid != null) {
            validateBillNo(fid.trim());
        }

        if (fuzzy) {
            if (no == null) {
                throw new IllegalArgumentException("请输入用于模糊匹配的单据编号");
            }
            List<Map<String, Object>> candidates = suggest(no, warnings);
            result.put("candidates", candidates);
            result.put("message", candidates.isEmpty()
                    ? "没有匹配的单据编号"
                    : "模糊匹配到 " + candidates.size() + " 条单据，请选择后查询");
            return finish(result, start, warnings);
        }

        if (no == null && fidParam == null) {
            throw new IllegalArgumentException("请输入采购收货单号");
        }

        List<Map<String, Object>> bills = fidParam != null
                ? safe(PurReceiveSchema.BILL, () -> mapper.selectBillsByFid(fidParam, bounds(1)), warnings)
                : safe(PurReceiveSchema.BILL,
                () -> mapper.selectBillsByBillNo(no, bounds(properties.getMaxCandidates())), warnings);

        if (bills.isEmpty()) {
            result.put("message", fidParam != null
                    ? "未找到内码为 " + fid + " 的采购收货单"
                    : "未找到单据编号为 " + no + " 的采购收货单");
            if (no != null) {
                result.put("candidates", suggest(no, warnings));
            }
        } else if (fidParam == null && bills.size() > 1) {
            result.put("ambiguous", true);
            result.put("candidates", candidatesFromRows(bills));
            result.put("message", "同一单据编号存在 " + bills.size() + " 条记录，请按内码精确选择");
        } else {
            buildDossier(bills.get(0), result, warnings);
        }
        return finish(result, start, warnings);
    }

    private void buildDossier(Map<String, Object> bill, Map<String, Object> result, List<String> warnings) {
        final Object fid = bill.get("fid");
        Map<String, List<Map<String, Object>>> raw = new LinkedHashMap<>();
        for (TableDef def : PurReceiveSchema.tableDefs()) {
            raw.put(def.getName(), Collections.<Map<String, Object>>emptyList());
        }

        raw.put(PurReceiveSchema.BILL, Collections.singletonList(bill));
        raw.put(PurReceiveSchema.BILL_L, safe(PurReceiveSchema.BILL_L, () -> mapper.selectBillL(fid, bounds()), warnings));
        raw.put(PurReceiveSchema.BILL_LK, safe(PurReceiveSchema.BILL_LK, () -> mapper.selectBillLk(fid, bounds()), warnings));
        raw.put(PurReceiveSchema.BILL_TC, safe(PurReceiveSchema.BILL_TC, () -> mapper.selectBillTc(fid, bounds()), warnings));
        raw.put(PurReceiveSchema.BILL_WB, safe(PurReceiveSchema.BILL_WB, () -> mapper.selectBillWb(fid, bounds()), warnings));
        raw.put(PurReceiveSchema.BILL_F, safe(PurReceiveSchema.BILL_F, () -> mapper.selectBillF(fid, bounds()), warnings));

        List<Map<String, Object>> entries = safe(PurReceiveSchema.ENTRY,
                () -> mapper.selectEntries(fid, bounds()), warnings);
        raw.put(PurReceiveSchema.ENTRY, entries);
        List<Object> entryIds = RowUtils.distinctValues(entries, "fentryid");

        raw.put(PurReceiveSchema.ENTRY_L, queryByKeys(PurReceiveSchema.ENTRY_L, entryIds, mapper::selectEntryL, warnings));
        raw.put(PurReceiveSchema.ENTRY_R, queryByKeys(PurReceiveSchema.ENTRY_R, entryIds, mapper::selectEntryR, warnings));
        raw.put(PurReceiveSchema.ENTRY_LK, queryByKeys(PurReceiveSchema.ENTRY_LK, entryIds, mapper::selectEntryLk, warnings));
        raw.put(PurReceiveSchema.BOX, queryByKeys(PurReceiveSchema.BOX, entryIds, mapper::selectBoxes, warnings));
        raw.put(PurReceiveSchema.ATTACH, queryByKeys(PurReceiveSchema.ATTACH, entryIds, mapper::selectAttachList, warnings));

        List<Object> detailIds = RowUtils.distinctValues(raw.get(PurReceiveSchema.ATTACH), "fdetailid");
        raw.put(PurReceiveSchema.ATTACH_FILE,
                queryByKeys(PurReceiveSchema.ATTACH_FILE, detailIds, mapper::selectAttachFiles, warnings));

        Map<String, Object> tables = new LinkedHashMap<>();
        int nonEmptyTableCount = 0;
        for (TableDef def : PurReceiveSchema.tableDefs()) {
            List<Map<String, Object>> rows = raw.getOrDefault(def.getName(), Collections.<Map<String, Object>>emptyList());
            if (!rows.isEmpty()) {
                nonEmptyTableCount++;
            }
            Map<String, Object> section = def.toMap();
            section.put("count", rows.size());
            section.put("columns", RowUtils.columnsOf(rows));
            section.put("rows", rows);
            section.put("truncated", rows.size() >= properties.getMaxRowsPerTable());
            tables.put(def.getName(), section);
        }

        Map<String, List<Map<String, Object>>> entryLGroup = RowUtils.groupBy(raw.get(PurReceiveSchema.ENTRY_L), "fentryid");
        Map<String, List<Map<String, Object>>> entryRGroup = RowUtils.groupBy(raw.get(PurReceiveSchema.ENTRY_R), "fentryid");
        Map<String, List<Map<String, Object>>> entryLkGroup = RowUtils.groupBy(raw.get(PurReceiveSchema.ENTRY_LK), "fentryid");
        Map<String, List<Map<String, Object>>> boxGroup = RowUtils.groupBy(raw.get(PurReceiveSchema.BOX), "fentryid");
        Map<String, List<Map<String, Object>>> attachGroup = RowUtils.groupBy(raw.get(PurReceiveSchema.ATTACH), "fentryid");
        Map<String, List<Map<String, Object>>> fileGroup = RowUtils.groupBy(raw.get(PurReceiveSchema.ATTACH_FILE), "fdetailid");

        List<Map<String, Object>> items = new ArrayList<>();
        int boxCount = 0;
        int attachCount = 0;
        int fileCount = 0;
        for (Map<String, Object> entryRow : entries) {
            String key = RowUtils.keyOf(entryRow.get("fentryid"));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fentryid", key);
            item.put("fseq", entryRow.get("fseq"));
            item.put("row", entryRow);

            Map<String, Object> ext = new LinkedHashMap<>();
            ext.put(PurReceiveSchema.ENTRY_L, rowsOf(entryLGroup, key));
            ext.put(PurReceiveSchema.ENTRY_R, rowsOf(entryRGroup, key));
            ext.put(PurReceiveSchema.ENTRY_LK, rowsOf(entryLkGroup, key));
            item.put("ext", ext);

            List<Map<String, Object>> boxes = rowsOf(boxGroup, key);
            item.put("boxes", boxes);

            List<Map<String, Object>> attachItems = new ArrayList<>();
            int entryFileCount = 0;
            for (Map<String, Object> attachRow : rowsOf(attachGroup, key)) {
                String detailKey = RowUtils.keyOf(attachRow.get("fdetailid"));
                List<Map<String, Object>> files = rowsOf(fileGroup, detailKey);
                Map<String, Object> attachItem = new LinkedHashMap<>();
                attachItem.put("fdetailid", detailKey);
                attachItem.put("row", attachRow);
                attachItem.put("files", files);
                attachItems.add(attachItem);
                entryFileCount += files.size();
            }
            item.put("attachList", attachItems);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("boxes", boxes.size());
            stats.put("attachments", attachItems.size());
            stats.put("files", entryFileCount);
            item.put("stats", stats);
            items.add(item);

            boxCount += boxes.size();
            attachCount += attachItems.size();
            fileCount += entryFileCount;
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("entryCount", entries.size());
        summary.put("boxCount", boxCount);
        summary.put("attachCount", attachCount);
        summary.put("fileCount", fileCount);
        summary.put("tableCount", tables.size());
        summary.put("nonEmptyTableCount", nonEmptyTableCount);

        result.put("found", true);
        result.put("billNo", bill.get("fbillno"));
        result.put("fid", fid);
        result.put("bill", bill);
        result.put("tables", tables);
        result.put("entries", items);
        result.put("summary", summary);
    }

    private List<Map<String, Object>> queryByKeys(String table, List<Object> ids,
                                                  BiFunction<List<Object>, RowBounds, List<Map<String, Object>>> query,
                                                  List<String> warnings) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> all = new ArrayList<>();
        for (List<Object> batch : RowUtils.partition(ids, properties.getInClauseBatchSize())) {
            try {
                all.addAll(RowUtils.normalize(query.apply(batch, bounds())));
            } catch (DataAccessException e) {
                warnings.add(rootMessage(table, e));
                log.warn("表 {} 查询失败", table, e);
                break;
            }
        }
        if (all.size() >= properties.getMaxRowsPerTable()) {
            all = new ArrayList<>(all.subList(0, properties.getMaxRowsPerTable()));
            warnings.add("表 " + table + " 数据已截断至 " + properties.getMaxRowsPerTable() + " 行");
        }
        return all;
    }

    private List<Map<String, Object>> safe(String table, SqlQuery query, List<String> warnings) {
        try {
            return RowUtils.normalize(query.execute());
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

    private List<Map<String, Object>> rowsOf(Map<String, List<Map<String, Object>>> grouped, String key) {
        return key == null ? Collections.<Map<String, Object>>emptyList()
                : grouped.getOrDefault(key, Collections.<Map<String, Object>>emptyList());
    }

    private List<Map<String, Object>> candidatesFromRows(List<Map<String, Object>> rows) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> candidate = new LinkedHashMap<>();
            Object fid = row.get("fid");
            Object billNo = row.get("fbillno");
            candidate.put("fid", fid);
            candidate.put("billNo", billNo);
            candidate.put("label", String.valueOf(billNo) + "（内码 " + String.valueOf(fid) + "）");
            candidate.put("status", row.get("fstatus"));
            candidate.put("bizDate", row.get("fbizdate"));
            candidates.add(candidate);
        }
        return candidates;
    }

    private Map<String, Object> finish(Map<String, Object> result, long start, List<String> warnings) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("queriedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        meta.put("elapsedMs", System.currentTimeMillis() - start);
        meta.put("mode", Arrays.asList(environment.getActiveProfiles()).contains("kingdee") ? "kingdee" : "demo");
        meta.put("tableCount", PurReceiveSchema.tableDefs().size());
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
    private interface SqlQuery {
        List<Map<String, Object>> execute();
    }
}
