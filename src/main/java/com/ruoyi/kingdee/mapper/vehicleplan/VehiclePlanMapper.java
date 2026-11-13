package com.ruoyi.kingdee.mapper.vehicleplan;

import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.domain.VehicleEntry;
import com.ruoyi.kingdee.domain.VehiclePlan;
import com.ruoyi.kingdee.domain.VehiclePlanTc;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 采购车辆计划查询：5 张表全部使用固定表名 + #{} 绑定参数，不做任何表名/列名拼接。
 * 关联链路：主表 fid → 物料信息 fentryid → 箱件信息；主表 fid → 车辆明细。
 */
public interface VehiclePlanMapper {

    List<VehiclePlan> selectPlansByBillNo(@Param("billNo") String billNo, RowBounds rowBounds);

    List<VehiclePlan> selectPlansByFid(@Param("fid") Object fid, RowBounds rowBounds);

    List<VehiclePlanTc> selectPlanTc(@Param("fid") Object fid, RowBounds rowBounds);

    List<ContainerEntry> selectContainerEntries(@Param("fid") Object fid, RowBounds rowBounds);

    List<Subentryentity> selectSubEntries(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<VehicleEntry> selectVehicleEntries(@Param("fid") Object fid, RowBounds rowBounds);
}
