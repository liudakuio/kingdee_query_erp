package com.ruoyi.kingdee.service.support;

import com.ruoyi.kingdee.util.RowUtils;
import com.ruoyi.kingdee.vo.BillCandidateVO;
import com.ruoyi.kingdee.vo.DossierResultVO;
import com.ruoyi.kingdee.vo.QueryMetaVO;
import com.ruoyi.kingdee.vo.TableDefVO;
import com.ruoyi.kingdee.vo.TableSectionVO;
import org.springframework.core.env.Environment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 档案响应的公共组装逻辑。
 *
 * <p>三个查询 Service 共用表分区构建、非空表统计、候选单据转换和查询元信息收尾，
 * 但彼此不继承、不共享 Map 协议，避免一处修改影响全部业务。</p>
 */
public final class DossierAssembler {

    private DossierAssembler() {
    }

    /**
     * 根据表定义和实体行构造一个表数据块。
     *
     * @param def        表定义
     * @param rows       该表的实体行
     * @param entityType 行实体类型，用于读取完整列名
     * @param maxRows    行数上限，达到上限时标记 truncated
     */
    public static TableSectionVO section(TableDefVO def, List<?> rows, Class<?> entityType, int maxRows) {
        TableSectionVO section = new TableSectionVO(
                def.getName(), def.getLabel(), def.getJoinKey(), def.getScope());
        int size = rows == null ? 0 : rows.size();
        section.setCount(size);
        section.setColumns(RowUtils.columnsOf(entityType));
        section.setRows(rows == null ? new ArrayList<>() : new ArrayList<>(rows));
        section.setTruncated(size >= maxRows);
        return section;
    }

    /** 统计有数据的表数量。 */
    public static int countNonEmpty(Map<String, TableSectionVO> tables) {
        int count = 0;
        for (TableSectionVO section : tables.values()) {
            if (section.getCount() > 0) {
                count++;
            }
        }
        return count;
    }

    /** 把主表实体转换为候选单据 VO；内码统一为字符串。 */
    public static BillCandidateVO candidate(Object fid, Object billNo, Object status, Object bizDate) {
        BillCandidateVO candidate = new BillCandidateVO();
        candidate.setFid(RowUtils.keyOf(fid));
        candidate.setBillNo(billNo == null ? null : String.valueOf(billNo));
        candidate.setLabel(String.valueOf(billNo) + "（内码 " + RowUtils.keyOf(fid) + "）");
        candidate.setStatus(status == null ? null : String.valueOf(status));
        candidate.setBizDate(bizDate == null ? null : String.valueOf(bizDate));
        return candidate;
    }

    /**
     * 统一写入告警与查询元信息。
     *
     * @param result    本次查询结果
     * @param start     查询开始时间（毫秒）
     * @param warnings  查询过程中收集的降级告警
     * @param environment Spring Environment，用于判断数据源模式
     * @param tableCount 当前业务的表数量
     */
    public static void finish(DossierResultVO result, long start, List<String> warnings,
                              Environment environment, int tableCount) {
        QueryMetaVO meta = new QueryMetaVO();
        meta.setQueriedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        meta.setElapsedMs(System.currentTimeMillis() - start);
        meta.setMode(Arrays.asList(environment.getActiveProfiles()).contains("kingdee") ? "kingdee" : "demo");
        meta.setTableCount(tableCount);
        result.setWarnings(warnings);
        result.setMeta(meta);
    }
}
