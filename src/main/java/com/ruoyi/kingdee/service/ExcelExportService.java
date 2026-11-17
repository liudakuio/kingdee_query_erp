package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.constant.PurReceiveSchema;
import com.ruoyi.kingdee.service.export.TableExportCustomizer;
import com.ruoyi.kingdee.vo.BatchResultVO;
import com.ruoyi.kingdee.vo.DossierResultVO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 采购收货单导出：复用通用导出逻辑，本类只提供查询入口、13 张表名与字段标签。
 */
@Service
public class ExcelExportService extends AbstractTableExportService {

    /** 导出全部 13 张表。 */
    public static final String ALL_TABLES = AbstractTableExportService.ALL_TABLES;

    private final PurReceiveService queryService;

    public ExcelExportService(PurReceiveService queryService,
                              ObjectProvider<TableExportCustomizer> customizers) {
        super(customizers);
        this.queryService = queryService;
    }

    @Override
    protected DossierResultVO queryDossier(String billNo, String fid) {
        return queryService.query(billNo, fid, false);
    }

    @Override
    protected BatchResultVO queryBatchDossier(String billNos) {
        return queryService.queryBatch(billNos);
    }

    @Override
    protected List<String> tableNames() {
        List<String> names = new ArrayList<>();
        for (PurReceiveSchema.TableDef def : PurReceiveSchema.tableDefs()) {
            names.add(def.getName());
        }
        return names;
    }

    @Override
    protected Map<String, String> columnLabels() {
        return PurReceiveSchema.columnLabels();
    }
}
