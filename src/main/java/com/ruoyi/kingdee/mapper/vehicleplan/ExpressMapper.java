package com.ruoyi.kingdee.mapper.vehicleplan;

import com.ruoyi.kingdee.domain.Express;
import com.ruoyi.kingdee.domain.ExpressTc;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 采购物流单查询。与 VehiclePlanMapper 同包，自动使用物流单所在的第二数据源。
 * 物料明细（tk_usp_container_entry）与箱件信息（tk_usp_subentryentity）与车辆计划同表，
 * 直接复用 VehiclePlanMapper 的 selectContainerEntries / selectSubEntries，不重复定义。
 *
 * <p>fid 参数保留 Object 是 SQL 绑定边界要求：数字内码必须按 Long 绑定，
 * 否则 PostgreSQL 的 int8 = varchar 会报类型错误；对外 VO 已统一为 String。</p>
 */
public interface ExpressMapper {

    List<Express> selectExpressByBillNo(@Param("billNo") String billNo, RowBounds rowBounds);

    List<Express> selectExpressByFid(@Param("fid") Object fid, RowBounds rowBounds);

    List<ExpressTc> selectExpressTc(@Param("fid") Object fid, RowBounds rowBounds);
}