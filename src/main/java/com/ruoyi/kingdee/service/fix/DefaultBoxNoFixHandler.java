package com.ruoyi.kingdee.service.fix;

import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 箱号信息修正的默认实现：按 fentryid（明细行内码）精确匹配上传表，
 * 命中则采用上传行数据、未命中则仅标记说明。
 */
@Component
public class DefaultBoxNoFixHandler implements BoxNoFixHandler {

    /**
     * 匹配键：采购收货单箱号信息里的 fentryid（上传表列名大小写不敏感）
     */
    static final List<String> MATCH_KEYS = Arrays.asList("fentryid", "明细行内码", "明细内码");

    /**
     * 结果「正确箱号」列的取值来源（箱号列候选，不参与匹配）
     */
    static final List<String> BOX_NO_KEYS = Arrays.asList(
            "fk_usp_sendboxno", "fk_usp_boxno", "fk_usp_sendbox", "箱号", "箱件号", "发货箱件(袋)号", "sendboxno");

    @Override
    public List<BoxNoFixRow> fix(List<Map<String, String>> uploaded, List<ReceivingBoxEntry> current) {
        // TODO 待补充：箱号信息修正的业务规则（当前按 fentryid 精确匹配上传表）
        Map<String, Map<String, String>> uploadedByKey = indexByMatchKey(uploaded);
        List<BoxNoFixRow> result = new ArrayList<>();
        for (ReceivingBoxEntry row : current) {
            BoxNoFixRow fixRow = BoxNoFixRow.of(row);
            String key = matchKeyOf(row);
            Map<String, String> matched = key == null ? null : uploadedByKey.get(key);
            if (matched == null) {
                fixRow.setMatched(false);
                fixRow.setMessage(key == null ? "当前行缺少 fentryid，无法匹配" : "上传表中未找到该 fentryid：" + key);
            } else {
                fixRow.setMatched(true);
                fixRow.setApplied(new LinkedHashMap<>(matched));
                fixRow.setMessage("已按上传表修正（fentryid=" + key + "）");
            }
            result.add(fixRow);
        }
        return result;
    }

    /**
     * 按 fentryid 建立上传表索引，同名以先出现的行为准
     */
    private Map<String, Map<String, String>> indexByMatchKey(List<Map<String, String>> uploaded) {
        Map<String, Map<String, String>> index = new LinkedHashMap<>();
        for (Map<String, String> row : uploaded) {
            String key = matchKeyOfRow(row);
            if (key != null) {
                index.putIfAbsent(key, row);
            }
        }
        return index;
    }

    /**
     * 当前行的 fentryid（实体字段，列名固定）
     */
    String matchKeyOf(ReceivingBoxEntry entity) {
        if (entity == null) {
            return null;
        }
        Object value = entity.getFentryid();
        return value == null || String.valueOf(value).trim().isEmpty() ? null : String.valueOf(value).trim();
    }

    /**
     * 上传表中某行的 fentryid（Excel 列名大小写不敏感）
     */
    String matchKeyOfRow(Map<String, ?> row) {
        return valueOfColumn(row, MATCH_KEYS);
    }

    /**
     * 按候选列名依次取第一个非空值；列名大小写不敏感，
     * 兼容 Excel 表头写成 FENTRYID / FentryId 的情况。
     */
    static String valueOfColumn(Map<String, ?> row, List<String> candidates) {
        if (row == null || row.isEmpty()) {
            return null;
        }
        for (String candidate : candidates) {
            for (Map.Entry<String, ?> entry : row.entrySet()) {
                String name = entry.getKey();
                if (name == null || !name.trim().equalsIgnoreCase(candidate)) {
                    continue;
                }
                Object value = entry.getValue();
                if (value != null && !String.valueOf(value).trim().isEmpty()) {
                    return String.valueOf(value).trim();
                }
            }
        }
        return null;
    }
}