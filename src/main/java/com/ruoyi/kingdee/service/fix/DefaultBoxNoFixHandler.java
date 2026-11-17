package com.ruoyi.kingdee.service.fix;

import com.ruoyi.kingdee.constant.BoxNoColumnAliases;
import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 箱号信息修正的默认实现：按 fentryid（明细行内码）精确匹配上传表，
 * 命中则采用上传行数据，未命中则仅标记说明。
 */
@Component
public class DefaultBoxNoFixHandler implements BoxNoFixHandler {

    @Override
    public List<BoxNoFixRow> fix(List<UploadedBoxNoRow> uploaded, List<ReceivingBoxEntry> current) {
        Map<String, UploadedBoxNoRow> uploadedByKey = indexByMatchKey(uploaded);
        List<BoxNoFixRow> result = new ArrayList<>();
        for (ReceivingBoxEntry row : current) {
            BoxNoFixRow fixRow = BoxNoFixRow.of(row);
            String key = matchKeyOf(row);
            UploadedBoxNoRow matched = key == null ? null : uploadedByKey.get(key);
            if (matched == null) {
                fixRow.setMatched(false);
                fixRow.setMessage(key == null ? "当前行缺少 fentryid，无法匹配" : "上传表中未找到该 fentryid：" + key);
            } else {
                fixRow.setMatched(true);
                fixRow.setApplied(new LinkedHashMap<>(matched.getValues()));
                fixRow.setMessage("已按上传表修正（fentryid=" + key + "）");
            }
            result.add(fixRow);
        }
        return result;
    }

    /** 按 fentryid 建立上传表索引，同名以先出现的行为准。 */
    private Map<String, UploadedBoxNoRow> indexByMatchKey(List<UploadedBoxNoRow> uploaded) {
        Map<String, UploadedBoxNoRow> index = new LinkedHashMap<>();
        if (uploaded == null) {
            return index;
        }
        for (UploadedBoxNoRow row : uploaded) {
            String key = matchKeyOfRow(row);
            if (key != null) {
                index.putIfAbsent(key, row);
            }
        }
        return index;
    }

    /** 当前行的 fentryid（实体字段，列名固定）。 */
    String matchKeyOf(ReceivingBoxEntry entity) {
        if (entity == null) {
            return null;
        }
        Object value = entity.getFentryid();
        return value == null || String.valueOf(value).trim().isEmpty() ? null : String.valueOf(value).trim();
    }

    /** 上传实体的匹配键。 */
    String matchKeyOfRow(UploadedBoxNoRow row) {
        return row == null ? null : row.getMatchKey();
    }

    /**
     * 按候选列名取非空值；保留为静态工具供结果导出复用，列名大小写不敏感。
     */
    static String valueOfColumn(Map<String, ?> row, List<String> candidates) {
        return UploadedBoxNoRow.valueOfColumn(row, candidates);
    }

    /** 供外部复用的箱号列候选，避免再维护第二份字符串常量。 */
    static List<String> boxNoKeys() {
        return BoxNoColumnAliases.BOX_NO_KEYS;
    }
}
