package com.ruoyi.kingdee.service.export;

import com.ruoyi.kingdee.constant.ExpressSchema;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 采购物流单的导出定制示例：为主表追加一列「运输路线」。
 * 想调整导出列内容，改这里的表头与取值表达式即可，无需改动导出主流程。
 */
@Component
public class ExpressExportCustomizer implements TableExportCustomizer {

    /** 追加列的表头文本 */
    private static final String HEADER = "运输路线";

    @Override
    public String tableName() {
        return ExpressSchema.EXPRESS;
    }

    @Override
    public List<CustomColumn> extraColumns() {
        return Collections.singletonList(CustomColumn.of(HEADER, context ->
                context.text("fk_usp_starting") + " → " + context.text("fk_usp_destination")
                        + "（" + context.text("fk_usp_transportnumber") + "）"
        ));
    }
}