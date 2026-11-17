package com.ruoyi.kingdee.service.fix;

import com.ruoyi.kingdee.domain.ReceivingBoxEntry;

import java.util.List;
import java.util.Map;

/**
 * 箱号信息修正的处理逻辑扩展点。
 * 需要补充业务规则时只改 DefaultBoxNoFixHandler 的实现，接口与调用方保持不变。
 */
public interface BoxNoFixHandler {

    /**
     * @param uploaded 上传的正确箱号信息数据行，key = Excel 列名（已去空格），值统一为字符串
     * @param current  当前查询到的箱号信息行（采购收货单箱号实体）
     * @return 每行的处理结果，顺序与 current 一致
     */
    List<BoxNoFixRow> fix(List<Map<String, String>> uploaded, List<ReceivingBoxEntry> current);
}