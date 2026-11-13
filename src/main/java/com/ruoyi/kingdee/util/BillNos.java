package com.ruoyi.kingdee.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 多单号解析：兼容中英文逗号，去除空白与空项、去重并保持输入顺序。
 * 只负责拆分，单号合法性与数量上限由 PurReceiveService 统一校验。
 */
public final class BillNos {

    private BillNos() {
    }

    public static List<String> parse(String value) {
        Set<String> numbers = new LinkedHashSet<>();
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>(numbers);
        }
        for (String part : value.split("[,，]")) {
            String no = part.trim();
            if (!no.isEmpty()) {
                numbers.add(no);
            }
        }
        return new ArrayList<>(numbers);
    }

    /** 输入框是否包含多个单号，供前端/后端选择批量通道。 */
    public static boolean isMultiple(String value) {
        return value != null && (value.indexOf(',') >= 0 || value.indexOf('，') >= 0);
    }
}
