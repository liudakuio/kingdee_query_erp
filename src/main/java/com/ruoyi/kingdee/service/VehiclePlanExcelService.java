package com.ruoyi.kingdee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.constant.VehiclePlanSchema;
import com.ruoyi.kingdee.service.export.TableExportCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 采购车辆计划导出：复用 AbstractTableExportService 的通用导出逻辑，
 * 本类只提供档案查询、可导出表名与字段标签。
 */
@Service
public class VehiclePlanExcelService extends AbstractTableExportService {

    private final VehiclePlanService queryService;

    public VehiclePlanExcelService(VehiclePlanService queryService, ObjectMapper objectMapper,
                                   ObjectProvider<TableExportCustomizer> customizers) {
        super(objectMapper, customizers);
        this.queryService = queryService;
    }

    @Override
    protected Map<String, Object> queryDossier(String billNo, String fid) {
        return queryService.query(billNo, fid);
    }

    @Override
    protected Map<String, Object> queryBatchDossier(String billNos) {
        return queryService.queryBatch(billNos);
    }

    @Override
    protected List<String> tableNames() {
        List<String> names = new ArrayList<>();
        for (VehiclePlanSchema.TableDef def : VehiclePlanSchema.tableDefs()) {
            names.add(def.getName());
        }
        return names;
    }

    @Override
    protected Map<String, String> columnLabels() {
        return VehiclePlanSchema.columnLabels();
    }
}