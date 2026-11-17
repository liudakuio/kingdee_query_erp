package com.ruoyi.kingdee;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Express;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.mapper.vehicleplan.ExpressMapper;
import com.ruoyi.kingdee.mapper.vehicleplan.VehiclePlanMapper;
import com.ruoyi.kingdee.service.ExpressService;
import com.ruoyi.kingdee.util.BillNos;
import com.ruoyi.kingdee.vo.BatchResultVO;
import com.ruoyi.kingdee.vo.ExpressDossierVO;
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
 * 采购物流单查询的纯逻辑测试：单号解析与校验、批量拆分与拒绝、summary 合计口径。
 * 不依赖真实金蝶库，Mapper 用 mock 替身；断言直接读取强类型 VO。
 */
class ExpressUnitTest {

    private ExpressMapper expressMapper;
    private VehiclePlanMapper sharedMapper;
    private ExpressService service;

    @BeforeEach
    void setUp() {
        expressMapper = mock(ExpressMapper.class);
        sharedMapper = mock(VehiclePlanMapper.class);
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        service = new ExpressService(expressMapper, sharedMapper, new KingdeeProperties(), environment);
    }

    @Test
    void billNosShouldSplitTrimAndDeduplicate() {
        List<String> numbers = BillNos.parse(" WL-001, WL-002，WL-001 ,, ");
        assertEquals(Arrays.asList("WL-001", "WL-002"), numbers);
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
            billNos.append(i > 1 ? "," : "").append("WL-").append(i);
        }
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.queryBatch(billNos.toString()));
        assertTrue(error.getMessage().contains("最多查询"));
    }

    @Test
    void queryBatchShouldMarkIllegalBillButKeepOthers() {
        BatchResultVO batch = service.queryBatch("WL-001,A'B");
        assertEquals(2, batch.getTotal());
        assertEquals(0, batch.getFoundCount());
        assertEquals(2, batch.getMissingCount());
        assertEquals(false, batch.getItems().get(1).isFound());
        assertEquals(Boolean.TRUE, batch.getItems().get(1).getError());
    }

    @Test
    void queryShouldReturnCandidatesWhenBillNoDuplicated() {
        Express first = new Express();
        first.setFid(1001L);
        first.setFbillno("WL-001");
        Express second = new Express();
        second.setFid(1002L);
        second.setFbillno("WL-001");
        when(expressMapper.selectExpressByBillNo(anyString(), any(RowBounds.class)))
                .thenReturn(Arrays.asList(first, second));

        ExpressDossierVO result = service.query("WL-001", null);
        assertEquals(false, result.isFound());
        assertEquals(true, result.isAmbiguous());
        assertEquals(2, result.getCandidates().size());
    }

    @Test
    void queryBatchShouldAggregateSummaryAcrossBills() {
        Express plan = new Express();
        plan.setFid(2001L);
        plan.setFbillno("WL-100");
        plan.setFbillstatus("C");
        when(expressMapper.selectExpressByBillNo(anyString(), any(RowBounds.class)))
                .thenReturn(Collections.singletonList(plan));

        ContainerEntry first = new ContainerEntry();
        first.setFentryid(3001L);
        first.setFseq(1);
        ContainerEntry second = new ContainerEntry();
        second.setFentryid(3002L);
        second.setFseq(2);
        when(sharedMapper.selectContainerEntries(any(), any(RowBounds.class)))
                .thenReturn(Arrays.asList(first, second));

        Subentryentity boxOne = new Subentryentity();
        boxOne.setFentryid(3001L);
        Subentryentity boxTwo = new Subentryentity();
        boxTwo.setFentryid(3002L);
        when(sharedMapper.selectSubEntries(any(), any(RowBounds.class)))
                .thenReturn(Arrays.asList(boxOne, boxTwo));

        BatchResultVO batch = service.queryBatch("WL-100");
        assertEquals(1, batch.getFoundCount());
        assertEquals(2L, batch.getTotals().getEntryCount());
        assertEquals(2L, batch.getTotals().getBoxCount());
    }
}
