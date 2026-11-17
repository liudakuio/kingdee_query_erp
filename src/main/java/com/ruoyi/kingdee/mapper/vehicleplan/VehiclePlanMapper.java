package com.ruoyi.kingdee.mapper.vehicleplan;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.domain.VehicleEntry;
import com.ruoyi.kingdee.domain.VehiclePlan;
import com.ruoyi.kingdee.domain.VehiclePlanTc;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 采购车辆计划查询 Mapper。
 *
 * <p>主表使用 MyBatis-Plus {@link BaseMapper}，其余 4 张表保留 XML 固定 SQL；
 * 数据源由 Service 层的 @DS("vehicleplan") 决定，不再按包绑定。</p>
 */
public interface VehiclePlanMapper extends BaseMapper<VehiclePlan> {

    List<VehiclePlanTc> selectPlanTc(@Param("fid") Object fid, RowBounds rowBounds);

    List<ContainerEntry> selectContainerEntries(@Param("fid") Object fid, RowBounds rowBounds);

    List<Subentryentity> selectSubEntries(@Param("ids") List<Object> ids, RowBounds rowBounds);

    List<VehicleEntry> selectVehicleEntries(@Param("fid") Object fid, RowBounds rowBounds);
}
