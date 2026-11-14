package com.ruoyi.kingdee;

import com.ruoyi.kingdee.config.KingdeeProperties;
import com.ruoyi.kingdee.domain.ContainerEntry;
import com.ruoyi.kingdee.domain.Express;
import com.ruoyi.kingdee.domain.Subentryentity;
import com.ruoyi.kingdee.mapper.vehicleplan.ExpressMapper;
import com.ruoyi.kingdee.mapper.vehicleplan.VehiclePlanMapper;
import com.ruoyi.kingdee.service.ExpressService;
import com.ruoyi.kingdee.util.BillNos;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 采购物流单查询的纯逻辑测试：单号解析与校验、批量拆分与拒绝、summary 合计口径。
 * 不依赖真实金蝶库，Mapper 用 mock 替身。
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
        Map<String, Object> batch = service.queryBatch("WL-001,A'B");
        assertEquals(2, ((Number) batch.get("total")).intValue());
        assertEquals(0, ((Number) batch.get("foundCount")).intValue());
        assertEquals(2, ((Number) batch.get("missingCount")).intValue());
        List<?> items = (List<?>) batch.get("items");
        Map<?, ?> illegal = (Map<?, ?>) items.get(1);
        assertEquals(Boolean.FALSE, illegal.get("found"));
        assertEquals(Boolean.TRUE, illegal.get("error"));
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

        Map<String, Object> result = service.query("WL-001", null);
        assertEquals(Boolean.FALSE, result.get("found"));
        assertEquals(Boolean.TRUE, result.get("ambiguous"));
        assertEquals(2, ((List<?>) result.get("candidates")).size());
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

        Map<String, Object> batch = service.queryBatch("WL-100");
        assertEquals(1, ((Number) batch.get("foundCount")).intValue());
        Map<?, ?> totals = (Map<?, ?>) batch.get("totals");
        assertEquals(2L, ((Number) totals.get("entryCount")).longValue());
        assertEquals(2L, ((Number) totals.get("boxCount")).longValue());
    }
}