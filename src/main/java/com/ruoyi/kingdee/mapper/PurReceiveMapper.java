package com.ruoyi.kingdee.mapper;

import com.ruoyi.kingdee.domain.CgEntryAttachment;
import com.ruoyi.kingdee.domain.CgShEntryAttach;
import com.ruoyi.kingdee.domain.PurRecBill;
import com.ruoyi.kingdee.domain.PurRecBillEntry;
import com.ruoyi.kingdee.domain.PurRecBillEntryL;
import com.ruoyi.kingdee.domain.PurRecBillEntryLk;
import com.ruoyi.kingdee.domain.PurRecBillEntryR;
import com.ruoyi.kingdee.domain.PurRecBillF;
import com.ruoyi.kingdee.domain.PurRecBillL;
import com.ruoyi.kingdee.domain.PurRecBillLk;
import com.ruoyi.kingdee.domain.PurRecBillTc;
import com.ruoyi.kingdee.domain.PurRecBillWb;
import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 13 张表全部使用固定表名 + #{} 绑定参数，不做任何表名/列名拼接。
 * 每张表返回对应的 entity，一行一个对象，不再返回 Map。
 * 一张明细可能对应多条箱唛/附件，因此各表分开查询后在 Service 层按内码组装，避免一对多 JOIN 造成行数膨胀。
 */
public interface PurReceiveMapper {

    List<PurRecBill> selectBillsByBillNo(@Param("billNo") String billNo, RowBounds rowBounds);

    List<PurRecBill> selectBillsByBillNoFuzzy(@Param("pattern") String pattern, RowBounds rowBounds);

    List<PurRecBill> selectBillsByFid(@Param("fid") Object fid, RowBounds rowBounds);

    List<PurRecBillL> selectBillL(@Param("fid") Object fid, RowBounds rowBounds);

    List<PurRecBillLk> selectBillLk(@Param("fid") Object fid, RowBounds rowBounds);

    List<PurRecBillTc> selectBillTc(@Param("fid") Object fid, RowBounds rowBounds);

    List<PurRecBillWb> selectBillWb(@Param("fid") Object fid, RowBounds rowBounds);

    List<PurRecBillF> selectBillF(@Param("fid") Object fid, RowBounds rowBounds);

    List<PurRecBillEntry> selectEntries(@Param("fid") Object fid, RowBounds rowBounds);

    List<PurRecBillEntryL> selectEntryL(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<PurRecBillEntryR> selectEntryR(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<PurRecBillEntryLk> selectEntryLk(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<ReceivingBoxEntry> selectBoxes(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<CgShEntryAttach> selectAttachList(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<CgEntryAttachment> selectAttachFiles(@Param("ids") List<Object> ids, RowBounds rowBounds);
}
