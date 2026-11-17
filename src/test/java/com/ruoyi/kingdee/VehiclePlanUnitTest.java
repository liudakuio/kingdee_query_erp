package com.ruoyi.kingdee;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.domain.VehicleEntry;
import com.ruoyi.kingdee.domain.VehiclePlan;
import com.ruoyi.kingdee.mapper.vehicleplan.VehiclePlanMapper;
import com.ruoyi.kingdee.service.VehiclePlanService;
import com.ruoyi.kingdee.util.BillNos;
import com.ruoyi.kingdee.vo.BatchResultVO;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 采购车辆计划查询的纯逻辑测试：单号解析、批量拒绝与 summary 合计口径。
 * 断言读取强类型 VO，避免测试再依赖动态 Map key。
 */
class VehiclePlanUnitTest {

    private VehiclePlanMapper mapper;
    private VehiclePlanService service;

    @BeforeEach
    void setUp() {
        mapper = mock(VehiclePlanMapper.class);
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        service = new VehiclePlanService(mapper, new KingdeeProperties(), environment);
    }

    @Test
    void billNosShouldSplitTrimAndDeduplicate() {
        List<String> numbers = BillNos.parse(" A-001, B-002，A-001 ,, ");
        assertEquals(Arrays.asList("A-001", "B-002"), numbers);
    }

    @Test
    void queryShouldRejectIllegalBillNo() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.query("A'B", null));
        assertTrue(error.getMessage().contains("非法字符"));
    }

    @Test
    void queryBatchShouldRejectTooManyBills() {
        StringBuilder billNos = new StringBuilder();
        for (int i = 1; i <= 21; i++) {
            billNos.append(i > 1 ? "," : "").append("CL-").append(i);
        }
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.queryBatch(billNos.toString()));
        assertTrue(error.getMessage().contains("最多查询"));
    }

    @Test
    void queryBatchShouldMarkIllegalBillButKeepOthers() {
        BatchResultVO batch = service.queryBatch("CL-001,A'B");
        assertEquals(2, batch.getTotal());
        // CL-001 未命中（mock 未返回数据），A'B 非法字符被标记为 error
        assertEquals(0, batch.getFoundCount());
        assertEquals(2, batch.getMissingCount());
        assertEquals(false, batch.getItems().get(0).isFound());
        assertEquals(false, batch.getItems().get(1).isFound());
        assertEquals(Boolean.TRUE, batch.getItems().get(1).getError());
    }

    @Test
    void queryBatchShouldAggregateSummaryAcrossBills() {
        VehiclePlan plan = new VehiclePlan();
        plan.setFid(1001L);
        plan.setFbillno("CL-001");
        plan.setFbillstatus("C");
        when(mapper.selectPlansByBillNo(anyString(), any(RowBounds.class)))
                .thenReturn(Collections.singletonList(plan));

        ContainerEntry first = new ContainerEntry();
        first.setFentryid(2001L);
        first.setFseq(1);
        ContainerEntry second = new ContainerEntry();
        second.setFentryid(2002L);
        second.setFseq(2);
        when(mapper.selectContainerEntries(any(), any(RowBounds.class)))
                .thenReturn(Arrays.asList(first, second));

        Subentryentity boxOne = new Subentryentity();
        boxOne.setFentryid(2001L);
        Subentryentity boxTwo = new Subentryentity();
        boxTwo.setFentryid(2002L);
        when(mapper.selectSubEntries(any(), any(RowBounds.class)))
                .thenReturn(Arrays.asList(boxOne, boxTwo));

        VehicleEntry vehicle = new VehicleEntry();
        vehicle.setFentryid(3001L);
        when(mapper.selectVehicleEntries(any(), any(RowBounds.class)))
                .thenReturn(Collections.singletonList(vehicle));

        BatchResultVO batch = service.queryBatch("CL-001");
        assertEquals(1, batch.getFoundCount());
        assertEquals(2L, batch.getTotals().getEntryCount());
        assertEquals(2L, batch.getTotals().getBoxCount());
        assertEquals(1L, batch.getTotals().getVehicleCount());
    }
}
