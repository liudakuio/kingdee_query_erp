package com.ruoyi.kingdee.service.fix;

import com.ruoyi.kingdee.domain.ReceivingBoxEntry;

import java.util.List;

/**
 * 箱号信息修正的处理逻辑扩展点。
 *
 * <p>接口保持非泛型，入参改为明确的上传行实体；需要补充业务规则时只改
 * DefaultBoxNoFixHandler 的实现，调用方保持不变。</p>
 */
public interface BoxNoFixHandler {

    /**
     * @param uploaded 上传的正确箱号信息实体行
     * @param current  当前查询到的箱号信息行（采购收货单箱号实体）
     * @return 每行的处理结果，顺序与 current 一致
     */
    List<BoxNoFixRow> fix(List<UploadedBoxNoRow> uploaded, List<ReceivingBoxEntry> current);
}
