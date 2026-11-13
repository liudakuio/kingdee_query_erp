package com.ruoyi.kingdee;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 采购车辆计划查询接口的集成测试（演示档案 H2 数据源）。
 * 需要以 demo profile 运行：mvn "-Dspring.profiles.active=demo" test
 */
@SpringBootTest
@AutoConfigureMockMvc
class VehiclePlanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void queryShouldReturnVehiclePlanDossier() throws Exception {
        mockMvc.perform(get("/kingdee/vehicleplan/query").param("billNo", "VP-20261113-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.found").value(true))
                .andExpect(jsonPath("$.data.billNo").value("VP-20261113-0001"))
                .andExpect(jsonPath("$.data.tables.tk_usp_vehicleplan.count").value(1))
                .andExpect(jsonPath("$.data.tables.tk_usp_container_entry.count").value(2))
                .andExpect(jsonPath("$.data.tables.tk_usp_subentryentity.count").value(3))
                .andExpect(jsonPath("$.data.tables.tk_usp_vehicleentry.count").value(1))
                .andExpect(jsonPath("$.data.entries.length()").value(2))
                .andExpect(jsonPath("$.data.summary.entryCount").value(2))
                .andExpect(jsonPath("$.data.summary.boxCount").value(3))
                .andExpect(jsonPath("$.data.summary.vehicleCount").value(1));
    }

    @Test
    void queryBatchShouldAggregateTwoBills() throws Exception {
        mockMvc.perform(get("/kingdee/vehicleplan/queryBatch")
                        .param("billNos", "VP-20261113-0001,VP-20261113-0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.foundCount").value(2))
                .andExpect(jsonPath("$.data.totals.entryCount").value(3))
                .andExpect(jsonPath("$.data.totals.boxCount").value(4))
                .andExpect(jsonPath("$.data.totals.vehicleCount").value(2));
    }

    @Test
    void exportShouldCreateOneSheetPerTable() throws Exception {
        MvcResult result = mockMvc.perform(get("/kingdee/vehicleplan/export")
                        .param("billNo", "VP-20261113-0001").param("table", "all"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(5, workbook.getNumberOfSheets());
            assertEquals("tk_usp_vehicleplan", workbook.getSheetName(0));
        }
    }
}