package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.constant.VehiclePlanSchema;
import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.domain.VehicleEntry;
import com.ruoyi.kingdee.domain.VehiclePlan;
import com.ruoyi.kingdee.domain.VehiclePlanTc;
import com.ruoyi.kingdee.dto.BillQuery;
import com.ruoyi.kingdee.mapper.vehicleplan.VehiclePlanMapper;
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
import com.ruoyi.kingdee.vo.SchemaMetaVO;
import com.ruoyi.kingdee.vo.TableDefVO;
import com.ruoyi.kingdee.vo.TableSectionVO;
import com.ruoyi.kingdee.vo.VehiclePlanDossierVO;
import com.ruoyi.kingdee.vo.VehiclePlanEntryVO;
import com.ruoyi.kingdee.vo.VehiclePlanSummaryVO;
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
 * 采购车辆计划查询：按 fid（主表 → 物料信息/车辆明细 → 箱件信息）逐层查询并组装档案。
 *
 * <p>与物流单、收货单共用 DossierAssembler / QuerySupport，但不共享业务 Map 协议。</p>
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
    public SchemaMetaVO meta() {
        SchemaMetaVO meta = new SchemaMetaVO();
        meta.setTables(new ArrayList<>(VehiclePlanSchema.tableVOs()));
        meta.setRelations(VehiclePlanSchema.relationVOs());
        meta.setColumnLabels(VehiclePlanSchema.columnLabels());
        meta.setTableCount(meta.getTables().size());
        return meta;
    }

    /** 兼容内部调用的简化入口；Controller 统一使用 {@link #query(BillQuery)}。 */
    public VehiclePlanDossierVO query(String billNo, String fid) {
        BillQuery query = new BillQuery();
        query.setBillNo(billNo);
        query.setFid(fid);
        return query(query);
    }

    /**
     * 按单据编号（或主表内码）查询整张采购车辆计划档案。
     *
     * @param query 查询条件，billNo 与 fid 二选一
     */
    public VehiclePlanDossierVO query(BillQuery query) {
        long start = System.currentTimeMillis();
        List<String> warnings = new ArrayList<>();
        VehiclePlanDossierVO result = new VehiclePlanDossierVO();

        Object fidParam = RowUtils.keyParam(query.getFid());
        String no = QuerySupport.trimToNull(query.getBillNo());
        if (no != null) {
            BillNos.validate(no);
        }
        if (fidParam == null && no == null) {
            throw new IllegalArgumentException("请输入采购车辆计划单号");
        }

        List<VehiclePlan> plans = fidParam != null
                ? QuerySupport.safe(VehiclePlanSchema.PLAN, () -> mapper.selectPlansByFid(fidParam, bounds(1)), warnings, log)
                : QuerySupport.safe(VehiclePlanSchema.PLAN, () -> mapper.selectPlansByBillNo(no, bounds(properties.getMaxCandidates())), warnings, log);

        if (plans.isEmpty()) {
            result.setMessage(fidParam != null ? "未找到内码为 " + query.getFid() + " 的采购车辆计划" : "未找到单据编号为 " + no + " 的采购车辆计划");
        } else if (fidParam == null && plans.size() > 1) {
            result.setAmbiguous(true);
            result.setCandidates(candidatesFromPlans(plans));
            result.setMessage("同一单据编号存在 " + plans.size() + " 条记录，请按内码 fid 精确选择");
        } else {
            buildDossier(plans.get(0), result, warnings);
        }
        DossierAssembler.finish(result, start, warnings, environment, VehiclePlanSchema.tableDefs().size());
        return result;
    }

    /**
     * 批量查询：单号以中英文逗号分隔，返回批次汇总对象 + 每个单据的完整档案。
     */
    public BatchResultVO queryBatch(String billNos) {
        List<String> numbers = BillNos.parse(billNos);
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("请输入采购车辆计划单号，多个单号用逗号分隔");
        }
        if (numbers.size() > properties.getMaxBatchBills()) {
            throw new IllegalArgumentException("单次最多查询 " + properties.getMaxBatchBills() + " 个单号");
        }

        List<VehiclePlanDossierVO> items = new ArrayList<>();
        for (String no : numbers) {
            VehiclePlanDossierVO item;
            try {
                item = query(no, null);
                if (item.getBillNo() == null) {
                    item.setBillNo(no);
                }
            } catch (RuntimeException e) {
                item = errorItem(no, e.getMessage());
            }
            items.add(item);
        }

        BatchTotalsVO totals = new BatchTotalsVO();
        for (VehiclePlanDossierVO item : items) {
            if (item.isFound() && item.getSummary() != null) {
                totals.setEntryCount(totals.getEntryCount() + item.getSummary().getEntryCount());
                totals.setBoxCount(totals.getBoxCount() + item.getSummary().getBoxCount());
                totals.setVehicleCount((totals.getVehicleCount() == null ? 0L : totals.getVehicleCount())
                        + item.getSummary().getVehicleCount());
            }
        }
        return BatchAssembler.build(items, totals, "demo");
    }

    /** 构造单项查询失败结果，保持批量结果结构一致。 */
    private VehiclePlanDossierVO errorItem(String billNo, String message) {
        VehiclePlanDossierVO item = new VehiclePlanDossierVO();
        item.setBillNo(billNo);
        item.setMessage(message);
        item.setError(Boolean.TRUE);
        return item;
    }

    /** 逐表查询并组装一个完整档案。 */
    private void buildDossier(VehiclePlan plan, VehiclePlanDossierVO result, List<String> warnings) {
        final Object fid = plan.getFid();

        List<VehiclePlanTc> planTc = QuerySupport.safe(VehiclePlanSchema.PLAN_TC, () -> mapper.selectPlanTc(fid, bounds()), warnings, log);
        List<ContainerEntry> containerEntries = QuerySupport.safe(VehiclePlanSchema.CONTAINER_ENTRY, () -> mapper.selectContainerEntries(fid, bounds()), warnings, log);
        List<VehicleEntry> vehicleEntries = QuerySupport.safe(VehiclePlanSchema.VEHICLE_ENTRY, () -> mapper.selectVehicleEntries(fid, bounds()), warnings, log);
        List<Object> entryIds = RowUtils.distinctValues(containerEntries, ContainerEntry::getFentryid);
        // 没有物料信息时直接跳过，避免生成非法的 IN () 条件
        List<Subentryentity> subEntries = entryIds.isEmpty()
                ? Collections.emptyList()
                : QuerySupport.safe(VehiclePlanSchema.SUB_ENTRY, () -> mapper.selectSubEntries(entryIds, bounds()), warnings, log);

        Map<String, List<Subentryentity>> subGroup = RowUtils.groupBy(subEntries, Subentryentity::getFentryid);

        Map<String, TableSectionVO> tables = new LinkedHashMap<>();
        putSection(tables, def(VehiclePlanSchema.PLAN), Collections.singletonList(plan), VehiclePlan.class);
        putSection(tables, def(VehiclePlanSchema.PLAN_TC), planTc, VehiclePlanTc.class);
        putSection(tables, def(VehiclePlanSchema.VEHICLE_ENTRY), vehicleEntries, VehicleEntry.class);
        putSection(tables, def(VehiclePlanSchema.CONTAINER_ENTRY), containerEntries, ContainerEntry.class);
        putSection(tables, def(VehiclePlanSchema.SUB_ENTRY), subEntries, Subentryentity.class);

        List<VehiclePlanEntryVO> entries = new ArrayList<>();
        int boxCount = 0;
        for (ContainerEntry row : containerEntries) {
            String key = RowUtils.keyOf(row.getFentryid());
            List<Subentryentity> boxes = QuerySupport.rowsOf(subGroup, key);
            EntryExtVO ext = new EntryExtVO();
            ext.put(VehiclePlanSchema.SUB_ENTRY, boxes);

            EntryStatsVO stats = new EntryStatsVO();
            stats.setBoxes(boxes.size());

            VehiclePlanEntryVO item = new VehiclePlanEntryVO();
            item.setFentryid(key);
            item.setFseq(row.getFseq());
            item.setRow(row);
            item.setExt(ext);
            item.setStats(stats);

            entries.add(item);
            boxCount += boxes.size();
        }

        VehiclePlanSummaryVO summary = new VehiclePlanSummaryVO();
        summary.setEntryCount(entries.size());
        summary.setBoxCount(boxCount);
        summary.setVehicleCount(vehicleEntries.size());
        summary.setTableCount(tables.size());
        summary.setNonEmptyTableCount(DossierAssembler.countNonEmpty(tables));

        result.setFound(true);
        result.setBillNo(text(plan.getFbillno()));
        result.setFid(RowUtils.keyOf(fid));
        result.setBill(plan);
        result.setTables(tables);
        result.setEntries(entries);
        result.setSummary(summary);
    }

    /** 把 Schema 中的表定义转换为业务 VO。 */
    private TableDefVO def(String tableName) {
        return VehiclePlanSchema.tableDef(tableName).toVO();
    }

    /** 按表定义构造数据块，避免手工拼 count/columns/rows 等动态 key。 */
    private void putSection(Map<String, TableSectionVO> tables,
                            TableDefVO def, List<?> rows, Class<?> entityType) {
        TableSectionVO section = DossierAssembler.section(def, rows, entityType, properties.getMaxRowsPerTable());
        tables.put(def.getName(), section);
    }

    /** 重复单号候选列表：已有实体字段转换为明确 VO。 */
    private List<BillCandidateVO> candidatesFromPlans(List<VehiclePlan> plans) {
        List<BillCandidateVO> candidates = new ArrayList<>();
        for (VehiclePlan plan : plans) {
            candidates.add(DossierAssembler.candidate(
                    plan.getFid(), plan.getFbillno(), plan.getFbillstatus(), null));
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
