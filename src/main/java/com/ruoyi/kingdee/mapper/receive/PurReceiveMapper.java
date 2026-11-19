package com.ruoyi.kingdee.mapper.receive;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
 * 采购收货单查询 Mapper。
 *
 * <p>主表使用 MyBatis-Plus {@link BaseMapper} 提供单表查询，Service 通过 QueryWrapper 构造条件；
 * 其余子表保留固定 SQL + #{} 绑定的 XML 查询，避免一对多 JOIN 造成明细行膨胀。</p>
 *
 * <p>fid/ids 保留 Object/List&lt;Object&gt; 是 SQL 绑定边界要求：数字内码按 Long 绑定，
 * 非数字内码按 String 绑定；业务接口只见强类型 VO，不直接暴露该边界类型。</p>
 */
public interface PurReceiveMapper extends BaseMapper<PurRecBill> {

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

    /** 按 Excel K 列编码（单据编号）查询一条箱号信息，用于回填 fentryid。 */
    ReceivingBoxEntry selectByCode(@Param("code") String code);
}
