package com.ruoyi.kingdee.service.support;

import com.ruoyi.kingdee.vo.BatchResultVO;
import com.ruoyi.kingdee.vo.BatchTotalsVO;
import com.ruoyi.kingdee.vo.DossierResultVO;

import java.util.List;

/**
 * 批量查询结果的统一收尾：统计命中数、确定数据源模式并封装批次 VO。
 */
public final class BatchAssembler {

    private BatchAssembler() {
    }

    /**
     * 构造批次响应。
     *
     * @param items       每个单号的查询结果
     * @param totals      业务侧计算好的合计值
     * @param defaultMode 没有任何子结果时使用的默认模式
     */
    public static BatchResultVO build(List<? extends DossierResultVO> items,
                                      BatchTotalsVO totals, String defaultMode) {
        int foundCount = 0;
        String mode = null;
        for (DossierResultVO item : items) {
            if (item.isFound()) {
                foundCount++;
            }
            if (mode == null && item.getMeta() != null && item.getMeta().getMode() != null) {
                mode = item.getMeta().getMode();
            }
        }

        BatchResultVO batch = new BatchResultVO();
        batch.setTotal(items.size());
        batch.setFoundCount(foundCount);
        batch.setMissingCount(items.size() - foundCount);
        batch.setTotals(totals);
        batch.setMode(mode == null ? defaultMode : mode);
        batch.setItems(new java.util.ArrayList<DossierResultVO>(items));
        return batch;
    }

    /** 构造单个单号查询失败时的占位结果，便于前端按相同结构渲染。 */
    public static DossierResultVO error(String billNo, String message) {
        DossierResultVO item = new DossierResultVO();
        item.setBillNo(billNo);
        item.setFound(false);
        item.setAmbiguous(false);
        item.setMessage(message);
        item.setError(Boolean.TRUE);
        return item;
    }
}
