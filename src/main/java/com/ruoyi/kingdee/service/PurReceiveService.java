package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.constant.PurReceiveSchema;
import com.ruoyi.kingdee.constant.PurReceiveSchema.TableDef;
import com.ruoyi.kingdee.domain.BillCandidate;
import com.ruoyi.kingdee.domain.CgEntryAttachment;
import com.ruoyi.kingdee.domain.CgShEntryAttach;
import com.ruoyi.kingdee.domain.PurRecBill;
import com.ruoyi.kingdee.domain.PurRecBillEntry;
import com.ruoyi.kingdee.domain.PurRecBillEntryL;
import com.ruoyi.kingdee.domain.PurRecBillEntryLk;
import com.ruoyi.kingdee.domain.PurRecBillEntryR;
import com.ruoyi.kingdee.domain.PurRecBillF;
import com.ruoyi.kingdee.domain.PurRecBillL;
import com.ruoyi.kingdee.domain.PurRecBillLk;
import com.ruoyi.kingdee.domain.PurRecBillTc;
import com.ruoyi.kingdee.domain.PurRecBillWb;
import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import com.ruoyi.kingdee.mapper.PurReceiveMapper;
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
import java.util.function.BiFunction;

/**
 * 按文档给出的三个关联键逐层取数并组装完整的收货单档案：
 * fid（主表→明细） → fentryid（明细→扩展/箱唛/附件清单） → fdetailid（附件清单→附件表）。
 * 所有查询结果均为 domain 下的 entity，各表分开查询，避免一对多 JOIN 造成明细行重复展开。
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

    /**
     * 供前端渲染使用的表字典、字段字典、关联关系。
     */
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

    /**
     * 单号联想 / 模糊查询。
     */
    public List<BillCandidate> suggest(String keyword) {
        return suggest(keyword, new ArrayList<String>());
    }

    private List<BillCandidate> suggest(String keyword, List<String> warnings) {
        String value = trimToNull(keyword);
        if (value == null) {
            return Collections.emptyList();
        }
        validateBillNo(value);
        List<PurRecBill> rows = safe(PurReceiveSchema.BILL,
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
            List<BillCandidate> candidates = suggest(no, warnings);
            result.put("candidates", candidates);
            result.put("message", candidates.isEmpty()
                    ? "没有匹配的单据编号"
                    : "模糊匹配到 " + candidates.size() + " 条单据，请选择后查询");
            return finish(result, start, warnings);
        }

        if (no == null && fidParam == null) {
            throw new IllegalArgumentException("请输入采购收货单号");
        }

        List<PurRecBill> bills = fidParam != null
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

    /**
     * 批量查询：单号以中英文逗号分隔，逐个复用单号查询逻辑。
     * 返回批次汇总对象（数量、合计、数据源模式由后端算好）+ items 全量单据结果。
     * 空输入或超过数量上限整体拒绝；单个单号非法 / 查询失败只标记该项，不影响其它单号。
     */
    public Map<String, Object> queryBatch(String billNos) {
        List<String> numbers = BillNos.parse(billNos);
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("请输入采购收货单号，多个单号用逗号分隔");
        }
        if (numbers.size() > properties.getMaxBatchBills()) {
            throw new IllegalArgumentException("单次最多查询 " + properties.getMaxBatchBills() + " 个单号");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (String no : numbers) {
            Map<String, Object> item;
            try {
                item = query(no, null, false);
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
        long attachCount = 0;
        long fileCount = 0;
        String mode = null;
        for (Map<String, Object> item : items) {
            if (Boolean.TRUE.equals(item.get("found"))) {
                foundCount++;
                @SuppressWarnings("unchecked")
                Map<String, Object> summary = (Map<String, Object>) item.get("summary");
                if (summary != null) {
                    entryCount += longOf(summary.get("entryCount"));
                    boxCount += longOf(summary.get("boxCount"));
                    attachCount += longOf(summary.get("attachCount"));
                    fileCount += longOf(summary.get("fileCount"));
                }
            }
            if (mode == null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> meta = (Map<String, Object>) item.get("meta");
                if (meta != null && meta.get("mode") != null) {
                    mode = String.valueOf(meta.get("mode"));
                }
            }
        }

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("entryCount", entryCount);
        totals.put("boxCount", boxCount);
        totals.put("attachCount", attachCount);
        totals.put("fileCount", fileCount);

        Map<String, Object> batch = new LinkedHashMap<>();
        batch.put("total", items.size());
        batch.put("foundCount", foundCount);
        batch.put("missingCount", items.size() - foundCount);
        batch.put("totals", totals);
        batch.put("mode", mode == null ? "demo" : mode);
        batch.put("items", items);
        return batch;
    }

    private long longOf(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    private void buildDossier(PurRecBill bill, Map<String, Object> result, List<String> warnings) {
        final Object fid = bill.getFid();

        List<PurRecBillL> billL = safe(PurReceiveSchema.BILL_L, () -> mapper.selectBillL(fid, bounds()), warnings);
        List<PurRecBillLk> billLk = safe(PurReceiveSchema.BILL_LK, () -> mapper.selectBillLk(fid, bounds()), warnings);
        List<PurRecBillTc> billTc = safe(PurReceiveSchema.BILL_TC, () -> mapper.selectBillTc(fid, bounds()), warnings);
        List<PurRecBillWb> billWb = safe(PurReceiveSchema.BILL_WB, () -> mapper.selectBillWb(fid, bounds()), warnings);
        List<PurRecBillF> billF = safe(PurReceiveSchema.BILL_F, () -> mapper.selectBillF(fid, bounds()), warnings);
        List<PurRecBillEntry> entries = safe(PurReceiveSchema.ENTRY, () -> mapper.selectEntries(fid, bounds()), warnings);

        List<Object> entryIds = RowUtils.distinctValues(entries, PurRecBillEntry::getFentryid);
        List<PurRecBillEntryL> entryL = queryByKeys(PurReceiveSchema.ENTRY_L, entryIds, mapper::selectEntryL, warnings);
        List<PurRecBillEntryR> entryR = queryByKeys(PurReceiveSchema.ENTRY_R, entryIds, mapper::selectEntryR, warnings);
        List<PurRecBillEntryLk> entryLk = queryByKeys(PurReceiveSchema.ENTRY_LK, entryIds, mapper::selectEntryLk, warnings);
        List<ReceivingBoxEntry> boxes = queryByKeys(PurReceiveSchema.BOX, entryIds, mapper::selectBoxes, warnings);
        List<CgShEntryAttach> attach = queryByKeys(PurReceiveSchema.ATTACH, entryIds, mapper::selectAttachList, warnings);

        List<Object> detailIds = RowUtils.distinctValues(attach, CgShEntryAttach::getFdetailid);
        List<CgEntryAttachment> attachFiles = queryByKeys(PurReceiveSchema.ATTACH_FILE, detailIds,
                mapper::selectAttachFiles, warnings);

        Map<String, Object> tables = new LinkedHashMap<>();
        putSection(tables, PurReceiveSchema.BILL, Collections.singletonList(bill), PurRecBill.class);
        putSection(tables, PurReceiveSchema.BILL_L, billL, PurRecBillL.class);
        putSection(tables, PurReceiveSchema.BILL_LK, billLk, PurRecBillLk.class);
        putSection(tables, PurReceiveSchema.BILL_TC, billTc, PurRecBillTc.class);
        putSection(tables, PurReceiveSchema.BILL_WB, billWb, PurRecBillWb.class);
        putSection(tables, PurReceiveSchema.BILL_F, billF, PurRecBillF.class);
        putSection(tables, PurReceiveSchema.ENTRY, entries, PurRecBillEntry.class);
        putSection(tables, PurReceiveSchema.ENTRY_L, entryL, PurRecBillEntryL.class);
        putSection(tables, PurReceiveSchema.ENTRY_R, entryR, PurRecBillEntryR.class);
        putSection(tables, PurReceiveSchema.ENTRY_LK, entryLk, PurRecBillEntryLk.class);
        putSection(tables, PurReceiveSchema.BOX, boxes, ReceivingBoxEntry.class);
        putSection(tables, PurReceiveSchema.ATTACH, attach, CgShEntryAttach.class);
        putSection(tables, PurReceiveSchema.ATTACH_FILE, attachFiles, CgEntryAttachment.class);

        int nonEmptyTableCount = 0;
        for (Object value : tables.values()) {
            Map<?, ?> section = (Map<?, ?>) value;
            if ((Integer) section.get("count") > 0) {
                nonEmptyTableCount++;
            }
        }

        Map<String, List<PurRecBillEntryL>> entryLGroup = RowUtils.groupBy(entryL, PurRecBillEntryL::getFentryid);
        Map<String, List<PurRecBillEntryR>> entryRGroup = RowUtils.groupBy(entryR, PurRecBillEntryR::getFentryid);
        Map<String, List<PurRecBillEntryLk>> entryLkGroup = RowUtils.groupBy(entryLk, PurRecBillEntryLk::getFentryid);
        Map<String, List<ReceivingBoxEntry>> boxGroup = RowUtils.groupBy(boxes, ReceivingBoxEntry::getFentryid);
        Map<String, List<CgShEntryAttach>> attachGroup = RowUtils.groupBy(attach, CgShEntryAttach::getFentryid);
        Map<String, List<CgEntryAttachment>> fileGroup = RowUtils.groupBy(attachFiles, CgEntryAttachment::getFdetailid);

        List<Map<String, Object>> items = new ArrayList<>();
        int boxCount = 0;
        int attachCount = 0;
        int fileCount = 0;
        for (PurRecBillEntry entryRow : entries) {
            String key = RowUtils.keyOf(entryRow.getFentryid());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fentryid", key);
            item.put("fseq", entryRow.getFseq());
            item.put("row", entryRow);

            Map<String, Object> ext = new LinkedHashMap<>();
            ext.put(PurReceiveSchema.ENTRY_L, rowsOf(entryLGroup, key));
            ext.put(PurReceiveSchema.ENTRY_R, rowsOf(entryRGroup, key));
            ext.put(PurReceiveSchema.ENTRY_LK, rowsOf(entryLkGroup, key));
            item.put("ext", ext);

            List<ReceivingBoxEntry> entryBoxes = rowsOf(boxGroup, key);
            item.put("boxes", entryBoxes);

            List<Map<String, Object>> attachItems = new ArrayList<>();
            int entryFileCount = 0;
            for (CgShEntryAttach attachRow : rowsOf(attachGroup, key)) {
                String detailKey = RowUtils.keyOf(attachRow.getFdetailid());
                List<CgEntryAttachment> files = rowsOf(fileGroup, detailKey);
                Map<String, Object> attachItem = new LinkedHashMap<>();
                attachItem.put("fdetailid", detailKey);
                attachItem.put("row", attachRow);
                attachItem.put("files", files);
                attachItems.add(attachItem);
                entryFileCount += files.size();
            }
            item.put("attachList", attachItems);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("boxes", entryBoxes.size());
            stats.put("attachments", attachItems.size());
            stats.put("files", entryFileCount);
            item.put("stats", stats);
            items.add(item);

            boxCount += entryBoxes.size();
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
        result.put("billNo", bill.getFbillno());
        result.put("fid", fid);
        result.put("bill", bill);
        result.put("tables", tables);
        result.put("entries", items);
        result.put("summary", summary);
    }

    private <T> void putSection(Map<String, Object> tables, String tableName, List<T> rows, Class<T> entityType) {
        TableDef def = PurReceiveSchema.tableDef(tableName);
        Map<String, Object> section = def.toMap();
        section.put("count", rows.size());
        section.put("columns", RowUtils.columnsOf(entityType));
        section.put("rows", rows);
        section.put("truncated", rows.size() >= properties.getMaxRowsPerTable());
        tables.put(def.getName(), section);
    }

    private <T> List<T> queryByKeys(String table, List<Object> ids,
                                    BiFunction<List<Object>, RowBounds, List<T>> query,
                                    List<String> warnings) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> all = new ArrayList<>();
        for (List<Object> batch : RowUtils.partition(ids, properties.getInClauseBatchSize())) {
            try {
                List<T> rows = query.apply(batch, bounds());
                if (rows != null) {
                    all.addAll(rows);
                }
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

    private <T> List<T> rowsOf(Map<String, List<T>> grouped, String key) {
        return key == null ? Collections.<T>emptyList()
                : grouped.getOrDefault(key, Collections.<T>emptyList());
    }

    private List<BillCandidate> candidatesFromRows(List<PurRecBill> rows) {
        List<BillCandidate> candidates = new ArrayList<>();
        for (PurRecBill row : rows) {
            BillCandidate candidate = new BillCandidate();
            candidate.setFid(row.getFid());
            candidate.setBillNo(row.getFbillno());
            candidate.setLabel(String.valueOf(row.getFbillno()) + "（内码 " + String.valueOf(row.getFid()) + "）");
            candidate.setStatus(row.getFbillstatus());
            candidate.setBizDate(row.getFbiztime());
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

    /**
     * 单号白名单：字母/数字/汉字及 . _ - / ，杜绝引号、空格、通配符等注入字符。
     */
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
