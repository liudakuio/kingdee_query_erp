package com.ruoyi.kingdee.service.export;

import com.ruoyi.kingdee.constant.PurReceiveSchema;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 收货箱明细 tk_usp_receivingboxentry 的导出定制示例。
 * 想自定义列内容，改 HEADER 常量或 extraColumns() 里的取值表达式即可，无需改动导出主流程。
 */
@Component
public class BoxEntryExportCustomizer implements TableExportCustomizer {

    /**
     * 追加列的表头文本
     */
    private static final String HEADER = "新增sql";

    @Override
    public String tableName() {
        return PurReceiveSchema.BOX;
    }

    @Override
    public List<CustomColumn> extraColumns() {
        return Collections.singletonList(CustomColumn.of(HEADER, context -> {
            Map<String, Object> row = context.getRow();
            return "INSERT INTO tk_usp_receivingboxentry (fentryid,fdetailid,fseq,fk_usp_sendboxno,fk_usp_wxno,fk_usp_arrived,fk_usp_srcsubentryid,fk_usp_detailqty,fk_usp_realqty) VALUES (\n" +
                    text(row.get("fentryid")) + ",\n" +
                    text(row.get("fdetailid")) + ",\n" +
                    text(row.get("fseq")) + ",\n" +
                    text(row.get("fk_usp_sendboxno")) + ",\n" +
                    text(row.get("fk_usp_wxno")) + ",\n" +
                    text(row.get("fk_usp_arrived")) + ",\n" +
                    text(row.get("fk_usp_srcsubentryid")) + ",\n" +
                    text(row.get("fk_usp_detailqty")) + ",\n" +
                    text(row.get("fk_usp_realqty")) + ";";
        }));
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
