package com.ruoyi.kingdee.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.constant.PurReceiveSchema;
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
import com.ruoyi.kingdee.dto.PurReceiveQuery;
import com.ruoyi.kingdee.mapper.receive.PurReceiveMapper;
import com.ruoyi.kingdee.service.support.BatchAssembler;
import com.ruoyi.kingdee.service.support.DossierAssembler;
import com.ruoyi.kingdee.util.BillNos;
import com.ruoyi.kingdee.util.QuerySupport;
import com.ruoyi.kingdee.util.RowUtils;
import com.ruoyi.kingdee.vo.BatchResultVO;
import com.ruoyi.kingdee.vo.BatchTotalsVO;
import com.ruoyi.kingdee.vo.BillCandidateVO;
import com.ruoyi.kingdee.vo.EntryExtVO;
import com.ruoyi.kingdee.vo.EntryStatsVO;
import com.ruoyi.kingdee.vo.PurReceiveAttachVO;
import com.ruoyi.kingdee.vo.PurReceiveDossierVO;
import com.ruoyi.kingdee.vo.PurReceiveEntryVO;
import com.ruoyi.kingdee.vo.PurReceiveSummaryVO;
import com.ruoyi.kingdee.vo.TableDefVO;
import com.ruoyi.kingdee.vo.TableSectionVO;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 采购收货单查询：按 fid → fentryid → fdetailid 三层关联键逐表取数并组装档案。
 *
 * <p>查询结果全部使用 VO；每行数据库数据仍由 domain 实体承载，
 * 避免一对多 JOIN 造成明细行重复展开。</p>
 */
@DS("scm")
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
     * 单号联想 / 模糊查询。
     */
    public List<BillCandidateVO> suggest(String keyword) {
        return suggest(keyword, new ArrayList<String>());
    }

    private List<BillCandidateVO> suggest(String keyword, List<String> warnings) {
        String value = QuerySupport.trimToNull(keyword);
        if (value == null) {
            return Collections.emptyList();
        }
        BillNos.validate(value);
        List<PurRecBill> rows = QuerySupport.safe(PurReceiveSchema.BILL,
                () -> mapper.selectPage(
                        new Page<PurRecBill>(1, properties.getMaxCandidates(), false),
                        new QueryWrapper<PurRecBill>()
                                .apply("UPPER(fbillno) LIKE UPPER({0})", "%" + value + "%")
                                .orderByAsc("fbillno")).getRecords(),
                warnings, log);
        return candidatesFromRows(rows);
    }

    /**
     * 兼容内部调用的简化入口；Controller 统一使用 {@link #query(PurReceiveQuery)}。
     */
    public PurReceiveDossierVO query(String billNo, String fid, boolean fuzzy) {
        PurReceiveQuery query = new PurReceiveQuery();
        query.setBillNo(billNo);
        query.setFid(fid);
        query.setFuzzy(fuzzy);
        return query(query);
    }

    /**
     * 按单号（或内码）查询整张收货单的关联数据。
     *
     * @param query 查询条件；fuzzy=true 时只返回候选单据
     */
    public PurReceiveDossierVO query(PurReceiveQuery query) {
        long start = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        PurReceiveDossierVO result = new PurReceiveDossierVO();

        String no = QuerySupport.trimToNull(query.getBillNo());
        Object fidParam = RowUtils.keyParam(query.getFid());
        if (no != null) {
            BillNos.validate(no);
        }
        if (fidParam != null && query.getFid() != null) {
            BillNos.validate(query.getFid().trim());
        }

        if (query.isFuzzy()) {
            if (no == null) {
                throw new IllegalArgumentException("请输入用于模糊匹配的单据编号");
            }
            List<BillCandidateVO> candidates = suggest(no, warnings);
            result.setCandidates(candidates);
            result.setMessage(candidates.isEmpty()
                    ? "没有匹配的单据编号"
                    : "模糊匹配到 " + candidates.size() + " 条单据，请选择后查询");
            DossierAssembler.finish(result, start, warnings, environment, PurReceiveSchema.tableDefs().size());
            return result;
        }

        if (no == null && fidParam == null) {
            throw new IllegalArgumentException("请输入采购收货单号");
        }

        List<PurRecBill> bills = fidParam != null
                ? QuerySupport.safe(PurReceiveSchema.BILL, () -> mapper.selectPage(
                        new Page<PurRecBill>(1, 1, false),
                        new QueryWrapper<PurRecBill>().eq("fid", fidParam)).getRecords(), warnings, log)
                : QuerySupport.safe(PurReceiveSchema.BILL, () -> mapper.selectPage(
                        new Page<PurRecBill>(1, properties.getMaxCandidates(), false),
                        new QueryWrapper<PurRecBill>().eq("fbillno", no)).getRecords(), warnings, log);

        if (bills.isEmpty()) {
            result.setMessage(fidParam != null
                    ? "未找到内码为 " + query.getFid() + " 的采购收货单"
                    : "未找到单据编号为 " + no + " 的采购收货单");
            if (no != null) {
                result.setCandidates(suggest(no, warnings));
            }
        } else if (fidParam == null && bills.size() > 1) {
            result.setAmbiguous(true);
            result.setCandidates(candidatesFromRows(bills));
            result.setMessage("同一单据编号存在 " + bills.size() + " 条记录，请按内码精确选择");
        } else {
            buildDossier(bills.get(0), result, warnings);
        }
        DossierAssembler.finish(result, start, warnings, environment, PurReceiveSchema.tableDefs().size());
        return result;
    }

    /**
     * 批量查询：单号以中英文逗号分隔，逐个复用单号查询逻辑。
     */
    public BatchResultVO queryBatch(String billNos) {
        List<String> numbers = BillNos.parse(billNos);
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("请输入采购收货单号，多个单号用逗号分隔");
        }
        if (numbers.size() > properties.getMaxBatchBills()) {
            throw new IllegalArgumentException("单次最多查询 " + properties.getMaxBatchBills() + " 个单号");
        }

        List<PurReceiveDossierVO> items = new ArrayList<>();
        for (String no : numbers) {
            PurReceiveDossierVO item;
            try {
                item = query(no, null, false);
                if (item.getBillNo() == null) {
                    item.setBillNo(no);
                }
            } catch (RuntimeException e) {
                item = errorItem(no, e.getMessage());
            }
            items.add(item);
        }

        BatchTotalsVO totals = new BatchTotalsVO();
        for (PurReceiveDossierVO item : items) {
            if (item.isFound() && item.getSummary() != null) {
                totals.setEntryCount(totals.getEntryCount() + item.getSummary().getEntryCount());
                totals.setBoxCount(totals.getBoxCount() + item.getSummary().getBoxCount());
                totals.setAttachCount((totals.getAttachCount() == null ? 0L : totals.getAttachCount())
                        + item.getSummary().getAttachCount());
                totals.setFileCount((totals.getFileCount() == null ? 0L : totals.getFileCount())
                        + item.getSummary().getFileCount());
            }
        }
        return BatchAssembler.build(items, totals, "demo");
    }

    /**
     * 构造单项查询失败结果，保持批量结果结构一致。
     */
    private PurReceiveDossierVO errorItem(String billNo, String message) {
        PurReceiveDossierVO item = new PurReceiveDossierVO();
        item.setBillNo(billNo);
        item.setMessage(message);
        item.setError(Boolean.TRUE);
        return item;
    }

    /**
     * 逐表查询并组装一个完整档案。
     */
    private void buildDossier(PurRecBill bill, PurReceiveDossierVO result, List<String> warnings) {
        final Object fid = bill.getFid();

        List<PurRecBillL> billL = QuerySupport.safe(PurReceiveSchema.BILL_L, () -> mapper.selectBillL(fid, bounds()), warnings, log);
        List<PurRecBillLk> billLk = QuerySupport.safe(PurReceiveSchema.BILL_LK, () -> mapper.selectBillLk(fid, bounds()), warnings, log);
        List<PurRecBillTc> billTc = QuerySupport.safe(PurReceiveSchema.BILL_TC, () -> mapper.selectBillTc(fid, bounds()), warnings, log);
        List<PurRecBillWb> billWb = QuerySupport.safe(PurReceiveSchema.BILL_WB, () -> mapper.selectBillWb(fid, bounds()), warnings, log);
        List<PurRecBillF> billF = QuerySupport.safe(PurReceiveSchema.BILL_F, () -> mapper.selectBillF(fid, bounds()), warnings, log);
        List<PurRecBillEntry> entries = QuerySupport.safe(PurReceiveSchema.ENTRY, () -> mapper.selectEntries(fid, bounds()), warnings, log);

        List<Object> entryIds = RowUtils.distinctValues(entries, PurRecBillEntry::getFentryid);
        List<PurRecBillEntryL> entryL = QuerySupport.queryByKeys(PurReceiveSchema.ENTRY_L, entryIds, mapper::selectEntryL, warnings, log, properties.getInClauseBatchSize(), properties.getMaxRowsPerTable());
        List<PurRecBillEntryR> entryR = QuerySupport.queryByKeys(PurReceiveSchema.ENTRY_R, entryIds, mapper::selectEntryR, warnings, log, properties.getInClauseBatchSize(), properties.getMaxRowsPerTable());
        List<PurRecBillEntryLk> entryLk = QuerySupport.queryByKeys(PurReceiveSchema.ENTRY_LK, entryIds, mapper::selectEntryLk, warnings, log, properties.getInClauseBatchSize(), properties.getMaxRowsPerTable());
        List<ReceivingBoxEntry> boxes = QuerySupport.queryByKeys(PurReceiveSchema.BOX, entryIds, mapper::selectBoxes, warnings, log, properties.getInClauseBatchSize(), properties.getMaxRowsPerTable());
        List<CgShEntryAttach> attach = QuerySupport.queryByKeys(PurReceiveSchema.ATTACH, entryIds, mapper::selectAttachList, warnings, log, properties.getInClauseBatchSize(), properties.getMaxRowsPerTable());

        List<Object> detailIds = RowUtils.distinctValues(attach, CgShEntryAttach::getFdetailid);
        List<CgEntryAttachment> attachFiles = QuerySupport.queryByKeys(PurReceiveSchema.ATTACH_FILE, detailIds, mapper::selectAttachFiles, warnings, log, properties.getInClauseBatchSize(), properties.getMaxRowsPerTable());

        Map<String, TableSectionVO> tables = new LinkedHashMap<>();
        putSection(tables, def(PurReceiveSchema.BILL), Collections.singletonList(bill), PurRecBill.class);
        putSection(tables, def(PurReceiveSchema.BILL_L), billL, PurRecBillL.class);
        putSection(tables, def(PurReceiveSchema.BILL_LK), billLk, PurRecBillLk.class);
        putSection(tables, def(PurReceiveSchema.BILL_TC), billTc, PurRecBillTc.class);
        putSection(tables, def(PurReceiveSchema.BILL_WB), billWb, PurRecBillWb.class);
        putSection(tables, def(PurReceiveSchema.BILL_F), billF, PurRecBillF.class);
        putSection(tables, def(PurReceiveSchema.ENTRY), entries, PurRecBillEntry.class);
        putSection(tables, def(PurReceiveSchema.ENTRY_L), entryL, PurRecBillEntryL.class);
        putSection(tables, def(PurReceiveSchema.ENTRY_R), entryR, PurRecBillEntryR.class);
        putSection(tables, def(PurReceiveSchema.ENTRY_LK), entryLk, PurRecBillEntryLk.class);
        putSection(tables, def(PurReceiveSchema.BOX), boxes, ReceivingBoxEntry.class);
        putSection(tables, def(PurReceiveSchema.ATTACH), attach, CgShEntryAttach.class);
        putSection(tables, def(PurReceiveSchema.ATTACH_FILE), attachFiles, CgEntryAttachment.class);

        Map<String, List<PurRecBillEntryL>> entryLGroup = RowUtils.groupBy(entryL, PurRecBillEntryL::getFentryid);
        Map<String, List<PurRecBillEntryR>> entryRGroup = RowUtils.groupBy(entryR, PurRecBillEntryR::getFentryid);
        Map<String, List<PurRecBillEntryLk>> entryLkGroup = RowUtils.groupBy(entryLk, PurRecBillEntryLk::getFentryid);
        Map<String, List<ReceivingBoxEntry>> boxGroup = RowUtils.groupBy(boxes, ReceivingBoxEntry::getFentryid);
        Map<String, List<CgShEntryAttach>> attachGroup = RowUtils.groupBy(attach, CgShEntryAttach::getFentryid);
        Map<String, List<CgEntryAttachment>> fileGroup = RowUtils.groupBy(attachFiles, CgEntryAttachment::getFdetailid);

        List<PurReceiveEntryVO> items = new ArrayList<>();
        int boxCount = 0;
        int attachCount = 0;
        int fileCount = 0;
        for (PurRecBillEntry entryRow : entries) {
            String key = RowUtils.keyOf(entryRow.getFentryid());
            EntryExtVO ext = new EntryExtVO();
            ext.put(PurReceiveSchema.ENTRY_L, QuerySupport.rowsOf(entryLGroup, key));
            ext.put(PurReceiveSchema.ENTRY_R, QuerySupport.rowsOf(entryRGroup, key));
            ext.put(PurReceiveSchema.ENTRY_LK, QuerySupport.rowsOf(entryLkGroup, key));

            List<ReceivingBoxEntry> entryBoxes = QuerySupport.rowsOf(boxGroup, key);
            List<PurReceiveAttachVO> attachItems = new ArrayList<>();
            int entryFileCount = 0;
            for (CgShEntryAttach attachRow : QuerySupport.rowsOf(attachGroup, key)) {
                String detailKey = RowUtils.keyOf(attachRow.getFdetailid());
                List<CgEntryAttachment> files = QuerySupport.rowsOf(fileGroup, detailKey);
                PurReceiveAttachVO attachItem = new PurReceiveAttachVO();
                attachItem.setFdetailid(detailKey);
                attachItem.setRow(attachRow);
                attachItem.setFiles(files);
                attachItems.add(attachItem);
                entryFileCount += files.size();
            }

            EntryStatsVO stats = new EntryStatsVO();
            stats.setBoxes(entryBoxes.size());
            stats.setAttachments(attachItems.size());
            stats.setFiles(entryFileCount);

            PurReceiveEntryVO item = new PurReceiveEntryVO();
            item.setFentryid(key);
            item.setFseq(entryRow.getFseq() instanceof Number
                    ? ((Number) entryRow.getFseq()).intValue() : null);
            item.setRow(entryRow);
            item.setExt(ext);
            item.setBoxes(entryBoxes);
            item.setAttachList(attachItems);
            item.setStats(stats);
            items.add(item);

            boxCount += entryBoxes.size();
            attachCount += attachItems.size();
            fileCount += entryFileCount;
        }

        PurReceiveSummaryVO summary = new PurReceiveSummaryVO();
        summary.setEntryCount(entries.size());
        summary.setBoxCount(boxCount);
        summary.setAttachCount(attachCount);
        summary.setFileCount(fileCount);
        summary.setTableCount(tables.size());
        summary.setNonEmptyTableCount(DossierAssembler.countNonEmpty(tables));

        result.setFound(true);
        result.setBillNo(text(bill.getFbillno()));
        result.setFid(RowUtils.keyOf(fid));
        result.setBill(bill);
        result.setTables(tables);
        result.setEntries(items);
        result.setSummary(summary);
    }

    /**
     * 把 Schema 中的表定义转换为业务 VO。
     */
    private TableDefVO def(String tableName) {
        return PurReceiveSchema.tableDef(tableName).toVO();
    }

    /**
     * 按表定义构造数据块，避免手工拼 count/columns/rows 等动态 key。
     */
    private void putSection(Map<String, TableSectionVO> tables,
                            TableDefVO def, List<?> rows, Class<?> entityType) {
        TableSectionVO section = DossierAssembler.section(def, rows, entityType, properties.getMaxRowsPerTable());
        tables.put(def.getName(), section);
    }

    /**
     * 主表实体转换为候选单据 VO。
     */
    private List<BillCandidateVO> candidatesFromRows(List<PurRecBill> rows) {
        List<BillCandidateVO> candidates = new ArrayList<>();
        for (PurRecBill row : rows) {
            candidates.add(DossierAssembler.candidate(
                    row.getFid(), row.getFbillno(), row.getFbillstatus(), row.getFbiztime()));
        }
        return candidates;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private RowBounds bounds() {
        return bounds(properties.getMaxRowsPerTable());
    }

    private RowBounds bounds(int limit) {
        return QuerySupport.bounds(limit);
    }
}
