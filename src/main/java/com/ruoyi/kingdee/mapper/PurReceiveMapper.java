package com.ruoyi.kingdee.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

/**
 * 13 张表全部使用固定表名 + #{} 绑定参数，不做任何表名/列名拼接。
 * 一张明细可能对应多条箱唛/附件，因此各表分开查询后在 Service 层按内码组装，避免一对多 JOIN 造成行数膨胀。
 */
public interface PurReceiveMapper {

    List<Map<String, Object>> selectBillsByBillNo(@Param("billNo") String billNo, RowBounds rowBounds);

    List<Map<String, Object>> selectBillsByBillNoFuzzy(@Param("pattern") String pattern, RowBounds rowBounds);

    List<Map<String, Object>> selectBillsByFid(@Param("fid") Object fid, RowBounds rowBounds);

    List<Map<String, Object>> selectBillL(@Param("fid") Object fid, RowBounds rowBounds);

    List<Map<String, Object>> selectBillLk(@Param("fid") Object fid, RowBounds rowBounds);

    List<Map<String, Object>> selectBillTc(@Param("fid") Object fid, RowBounds rowBounds);

    List<Map<String, Object>> selectBillWb(@Param("fid") Object fid, RowBounds rowBounds);

    List<Map<String, Object>> selectBillF(@Param("fid") Object fid, RowBounds rowBounds);

    List<Map<String, Object>> selectEntries(@Param("fid") Object fid, RowBounds rowBounds);

    List<Map<String, Object>> selectEntryL(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<Map<String, Object>> selectEntryR(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<Map<String, Object>> selectEntryLk(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<Map<String, Object>> selectBoxes(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<Map<String, Object>> selectAttachList(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<Map<String, Object>> selectAttachFiles(@Param("ids") List<Object> ids, RowBounds rowBounds);
}
