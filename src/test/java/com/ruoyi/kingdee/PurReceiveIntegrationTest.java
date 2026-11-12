package com.ruoyi.kingdee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
}
