package com.ruoyi.kingdee;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端验证：H2 演示库 → MyBatis → Service 关联组装 → REST 响应。
 */
@SpringBootTest
@AutoConfigureMockMvc
class PurReceiveIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void metaShouldDescribeAllThirteenTables() throws Exception {
        mockMvc.perform(get("/kingdee/receive/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.tableCount").value(13))
                .andExpect(jsonPath("$.data.tables.length()").value(13))
                .andExpect(jsonPath("$.data.relations.length()").value(12));
    }

    @Test
    void queryShouldReturnFullLinkedDossier() throws Exception {
        mockMvc.perform(get("/kingdee/receive/query").param("billNo", "CGSH-20261111-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.found").value(true))
                .andExpect(jsonPath("$.data.billNo").value("CGSH-20261111-0001"))
                .andExpect(jsonPath("$.data.fid").value("1001"))
                .andExpect(jsonPath("$.data.tables.t_im_purrecbill.count").value(1))
                .andExpect(jsonPath("$.data.tables.t_im_purrecbillentry.count").value(2))
                .andExpect(jsonPath("$.data.tables.tk_usp_receivingboxentry.count").value(4))
                .andExpect(jsonPath("$.data.tables.tk_usp_cgentryattachment.count").value(4))
                .andExpect(jsonPath("$.data.summary.entryCount").value(2))
                .andExpect(jsonPath("$.data.summary.boxCount").value(4))
                .andExpect(jsonPath("$.data.summary.fileCount").value(4))
                .andExpect(jsonPath("$.data.entries.length()").value(2))
                .andExpect(jsonPath("$.data.entries[0].boxes.length()").value(3))
                .andExpect(jsonPath("$.data.entries[0].attachList.length()").value(2))
                .andExpect(jsonPath("$.data.entries[0].attachList[0].files.length()").value(2));
    }

    @Test
    void tablesShouldExposeErDiagramColumns() throws Exception {
        // 除主表与明细表使用精简白名单外，其余表按 ER 图展示全部字段
        mockMvc.perform(get("/kingdee/receive/query").param("billNo", "CGSH-20261111-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tables.t_im_purrecbill.columns.length()").value(15))
                .andExpect(jsonPath("$.data.tables.t_im_purrecbillentry.columns.length()").value(20))
                .andExpect(jsonPath("$.data.tables.tk_usp_receivingboxentry.columns.length()").value(9))
                .andExpect(jsonPath("$.data.tables.tk_usp_cgshentryattach.columns.length()").value(11))
                .andExpect(jsonPath("$.data.tables.tk_usp_cgentryattachment.columns.length()").value(3))
                .andExpect(jsonPath("$.data.tables.t_im_purrecbillentry_r.columns.length()").value(62))
                .andExpect(jsonPath("$.data.tables.tk_usp_receivingboxentry.rows[0].fk_usp_sendboxno")
                        .value("BOX-0001"))
                .andExpect(jsonPath("$.data.tables.tk_usp_cgshentryattach.rows[0].fk_usp_attachname")
                        .value("材质证明书"));
    }

    @Test
    void queryByFidShouldWorkAndEmptyChildTablesShouldBeHandled() throws Exception {
        mockMvc.perform(get("/kingdee/receive/query").param("fid", "1003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.found").value(true))
                .andExpect(jsonPath("$.data.billNo").value("CGSH-20261110-0003"))
                .andExpect(jsonPath("$.data.summary.boxCount").value(0))
                .andExpect(jsonPath("$.data.summary.attachCount").value(0))
                .andExpect(jsonPath("$.data.tables.tk_usp_receivingboxentry.count").value(0));
    }

    @Test
    void missingBillShouldReturnFuzzyCandidates() throws Exception {
        mockMvc.perform(get("/kingdee/receive/query").param("billNo", "CGSH-20261111-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.found").value(false))
                .andExpect(jsonPath("$.data.candidates.length()").value(2));
    }

    @Test
    void illegalBillNoShouldBeRejected() throws Exception {
        mockMvc.perform(get("/kingdee/receive/query").param("billNo", "A' OR 1=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void fuzzyListAndMetaShouldBeReadOnly() throws Exception {
        mockMvc.perform(get("/kingdee/receive/list").param("billNo", "0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].billNo").value("CGSH-20261111-0002"));
    }

    @Test
    void exportAllTablesShouldCreateOneSheetPerTable() throws Exception {
        MvcResult result = mockMvc.perform(get("/kingdee/receive/export")
                        .param("billNo", "CGSH-20261111-0001").param("table", "all"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 0);
        assertEquals('P', bytes[0]);
        assertEquals('K', bytes[1]);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(13, workbook.getNumberOfSheets());
            assertEquals("t_im_purrecbill", workbook.getSheetName(0));
            assertEquals("tk_usp_cgentryattachment", workbook.getSheetName(12));
        }
    }

    @Test
    void exportSingleTableShouldCreateOnlyThatSheet() throws Exception {
        MvcResult result = mockMvc.perform(get("/kingdee/receive/export")
                        .param("billNo", "CGSH-20261111-0001")
                        .param("table", "tk_usp_receivingboxentry"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("tk_usp_receivingboxentry", workbook.getSheetName(0));
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(9, sheet.getRow(0).getPhysicalNumberOfCells());
            assertTrue(sheet.getRow(0).getCell(0).getStringCellValue().contains("fk_usp_sendboxno"));
            assertEquals(4, sheet.getLastRowNum());
            assertEquals("BOX-0001", sheet.getRow(1).getCell(0).getStringCellValue());
        }
    }

    @Test
    void exportUnknownTableShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/kingdee/receive/export")
                        .param("billNo", "CGSH-20261111-0001").param("table", "not_exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}
