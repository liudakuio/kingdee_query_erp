package com.ruoyi.kingdee.service.export;

import com.ruoyi.kingdee.constant.VehiclePlanSchema;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/** 采购车辆计划箱件信息表的导出定制示例，改这里即可调整导出列内容。 */
@Component
public class VehiclePlanExportCustomizer implements TableExportCustomizer {

    private static final String HEADER = "箱件描述";

    @Override
    public String tableName() {
        return VehiclePlanSchema.SUB_ENTRY;
    }

    @Override
    public List<CustomColumn> extraColumns() {
        return Collections.singletonList(CustomColumn.of(HEADER, context -> {
            Map<String, Object> row = context.getRow();
            return text(row.get("fk_usp_sendboxno")) + " / " + text(row.get("fk_usp_volume"))
                    + " / " + text(row.get("fk_usp_gross_weight"));
        }));
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
