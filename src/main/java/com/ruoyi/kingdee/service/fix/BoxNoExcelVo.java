package com.ruoyi.kingdee.service.fix;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 箱号信息修正结果的导出行 VO：当前箱号信息各列的文本值 + 修正结果。
 * 导出层只依赖本 VO，不再直接读取业务层的 Map 与处理结果对象。
 */
@Data
public class BoxNoExcelVo {

    /** 当前箱号信息各列的单元格文本（key = 数据库列名） */
    private Map<String, String> values = new LinkedHashMap<>();
    /** 正确箱号（取自上传表命中行的箱号列，上传表没有箱号列时为空） */
    private String correctBoxNo = "";
    /** 处理说明 */
    private String message = "";
    /** 是否命中（决定底色：浅黄 = 已修正，浅红 = 未匹配） */
    private boolean matched;

    /** 由处理结果组装导出行：列值统一转文本，正确箱号从上传命中行取值 */
    public static BoxNoExcelVo of(BoxNoFixRow row) {
        BoxNoExcelVo vo = new BoxNoExcelVo();
        if (row == null) {
            return vo;
        }
        Map<String, Object> current = row.getCurrent();
        if (current != null) {
            Map<String, String> values = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : current.entrySet()) {
                values.put(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            }
            vo.setValues(values);
        }
        String boxNo = DefaultBoxNoFixHandler.valueOfColumn(row.getApplied(), DefaultBoxNoFixHandler.BOX_NO_KEYS);
        vo.setCorrectBoxNo(boxNo == null ? "" : boxNo);
        vo.setMessage(row.getMessage() == null ? "" : row.getMessage());
        vo.setMatched(row.isMatched());
        return vo;
    }
}