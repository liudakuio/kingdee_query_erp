package com.ruoyi.kingdee.constant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 箱号 Excel 的列名别名。
 *
 * <p>上传表头由用户提供且大小写不敏感，这里集中维护候选列名，
 * 业务 Handler 不再散落字符串常量。</p>
 */
public final class BoxNoColumnAliases {

    /** 匹配键 fentryid 的候选列名。 */
    public static final List<String> MATCH_KEYS = Collections.unmodifiableList(
            Arrays.asList("fentryid", "明细行内码", "明细内码"));

    /** 正确箱号的候选列名。 */
    public static final List<String> BOX_NO_KEYS = Collections.unmodifiableList(
            Arrays.asList("fk_usp_sendboxno", "fk_usp_boxno", "fk_usp_sendbox", "箱号", "箱件号", "发货箱件(袋)号", "sendboxno"));

    private BoxNoColumnAliases() {
    }
}
