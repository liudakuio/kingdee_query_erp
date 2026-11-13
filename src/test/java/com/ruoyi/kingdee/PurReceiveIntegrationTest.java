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
            // 未注册处理器的表列数不变，只有箱唛表在末尾多一列
            assertEquals(15, workbook.getSheet("t_im_purrecbill").getRow(0).getPhysicalNumberOfCells());
            assertEquals(20, workbook.getSheet("t_im_purrecbillentry").getRow(0).getPhysicalNumberOfCells());
            assertEquals(10, workbook.getSheet("tk_usp_receivingboxentry").getRow(0).getPhysicalNumberOfCells());
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
            assertEquals(10, sheet.getRow(0).getPhysicalNumberOfCells());
            assertTrue(sheet.getRow(0).getCell(0).getStringCellValue().contains("fk_usp_sendboxno"));
            // 自定义列内容由 BoxEntryExportCustomizer 决定，这里只验证列存在且非空
            assertTrue(sheet.getRow(0).getCell(9).getStringCellValue().length() > 0);
            assertEquals(4, sheet.getLastRowNum());
            assertEquals("BOX-0001", sheet.getRow(1).getCell(0).getStringCellValue());
            assertTrue(sheet.getRow(1).getCell(9).getStringCellValue().length() > 0);
        }
    }

    @Test
    void exportEmptyBoxTableShouldContainOnlyHeaderWithCustomColumn() throws Exception {
        MvcResult result = mockMvc.perform(get("/kingdee/receive/export")
                        .param("fid", "1003").param("table", "tk_usp_receivingboxentry"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(0, sheet.getLastRowNum());
            assertEquals(10, sheet.getRow(0).getPhysicalNumberOfCells());
            assertTrue(sheet.getRow(0).getCell(9).getStringCellValue().length() > 0);
        }
    }

    @Test
    void exportUnknownTableShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/kingdee/receive/export")
                        .param("billNo", "CGSH-20261111-0001").param("table", "not_exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void queryBatchShouldReturnAggregateWithAllItems() throws Exception {
        mockMvc.perform(get("/kingdee/receive/queryBatch")
                        .param("billNos", "CGSH-20261111-0001,CGSH-20261111-0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.foundCount").value(2))
                .andExpect(jsonPath("$.data.missingCount").value(0))
                .andExpect(jsonPath("$.data.totals.entryCount").value(3))
                .andExpect(jsonPath("$.data.totals.boxCount").value(5))
                .andExpect(jsonPath("$.data.mode").value("demo"))
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].billNo").value("CGSH-20261111-0001"))
                .andExpect(jsonPath("$.data.items[0].found").value(true))
                .andExpect(jsonPath("$.data.items[1].billNo").value("CGSH-20261111-0002"))
                .andExpect(jsonPath("$.data.items[1].found").value(true))
                .andExpect(jsonPath("$.data.items[1].summary.entryCount").value(1));
    }

    @Test
    void queryBatchShouldMarkMissingAndInvalidBills() throws Exception {
        // 兼容中文逗号；单个单号未找到或非法只标记该项，其它单号照常返回
        mockMvc.perform(get("/kingdee/receive/queryBatch")
                        .param("billNos", "CGSH-20261111-0001，NOT-EXIST,A'B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.foundCount").value(1))
                .andExpect(jsonPath("$.data.missingCount").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.items[0].found").value(true))
                .andExpect(jsonPath("$.data.items[1].found").value(false))
                .andExpect(jsonPath("$.data.items[2].error").value(true));
    }

    @Test
    void queryBatchShouldRejectTooManyBills() throws Exception {
        StringBuilder billNos = new StringBuilder();
        for (int i = 1; i <= 21; i++) {
            if (i > 1) {
                billNos.append(',');
            }
            billNos.append("CGSH-").append(i);
        }
        mockMvc.perform(get("/kingdee/receive/queryBatch").param("billNos", billNos.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void exportBatchShouldMergeBillsIntoOneSheetPerTable() throws Exception {
        MvcResult result = mockMvc.perform(get("/kingdee/receive/exportBatch")
                        .param("billNos", "CGSH-20261111-0001,CGSH-20261111-0002")
                        .param("table", "all"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            // 13 张表各一个 sheet，不再按单据拆分，也没有额外的单据清单 sheet
            assertEquals(13, workbook.getNumberOfSheets());
            assertEquals("t_im_purrecbill", workbook.getSheetName(0));
            assertEquals("tk_usp_cgentryattachment", workbook.getSheetName(12));
            Sheet boxes = workbook.getSheet("tk_usp_receivingboxentry");
            assertEquals(5, boxes.getLastRowNum()); // 表头 + 1001 的 4 行 + 1002 的 1 行
            assertEquals("CGSH-20261111-0001", boxes.getRow(1).getCell(10).getStringCellValue());
            assertEquals("CGSH-20261111-0002", boxes.getRow(5).getCell(10).getStringCellValue());
        }
    }

    @Test
    void exportBatchSingleTableShouldMergeAllBillsIntoOneSheet() throws Exception {
        MvcResult result = mockMvc.perform(get("/kingdee/receive/exportBatch")
                        .param("billNos", "CGSH-20261111-0001,CGSH-20261111-0002")
                        .param("table", "tk_usp_receivingboxentry"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("tk_usp_receivingboxentry", workbook.getSheetName(0));
            Sheet sheet = workbook.getSheetAt(0);
            // 9 个原列 + 1 个表级自定义列 + 1 个「单据编号」列
            assertEquals(11, sheet.getRow(0).getPhysicalNumberOfCells());
            assertEquals("单据编号", sheet.getRow(0).getCell(10).getStringCellValue());
            assertTrue(sheet.getRow(0).getCell(9).getStringCellValue().length() > 0);
            assertEquals(5, sheet.getLastRowNum());
            assertEquals("BOX-0001", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("CGSH-20261111-0001", sheet.getRow(1).getCell(10).getStringCellValue());
            assertEquals("BOX-0005", sheet.getRow(5).getCell(0).getStringCellValue());
            assertEquals("CGSH-20261111-0002", sheet.getRow(5).getCell(10).getStringCellValue());
        }
    }
}
