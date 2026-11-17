package com.ruoyi.kingdee.mapper.vehicleplan;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.kingdee.domain.Express;
import com.ruoyi.kingdee.domain.ExpressTc;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * 采购物流单查询 Mapper。
 *
 * <p>主表使用 MyBatis-Plus {@link BaseMapper}，关联追踪表保留 XML 固定 SQL；
 * 数据源由 Service 层的 @DS("vehicleplan") 决定，不再按包绑定。</p>
 */
public interface ExpressMapper extends BaseMapper<Express> {

    List<ExpressTc> selectExpressTc(@Param("fid") Object fid, RowBounds rowBounds);
}