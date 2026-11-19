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
 * 接口 JSON 契约兼容性测试。
 *
 * <p>重构后 VO 的字段名、字段顺序和可选字段缺失行为必须与旧 Map 响应保持一致，
 * 避免影响现有 ruoyi-ui 页面。</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiContractCompatibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void foundDossierShouldKeepBusinessFieldNames() throws Exception {
        mockMvc.perform(get("/kingdee/receive/query").param("billNo", "CGSH-20261111-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.found").value(true))
                .andExpect(jsonPath("$.data.ambiguous").value(false))
                .andExpect(jsonPath("$.data.candidates").isArray())
                .andExpect(jsonPath("$.data.billNo").value("CGSH-20261111-0001"))
                .andExpect(jsonPath("$.data.fid").value("1001"))
                .andExpect(jsonPath("$.data.bill").exists())
                .andExpect(jsonPath("$.data.tables").exists())
                .andExpect(jsonPath("$.data.entries").isArray())
                .andExpect(jsonPath("$.data.summary.entryCount").value(2))
                .andExpect(jsonPath("$.data.warnings").isArray())
                .andExpect(jsonPath("$.data.meta.queriedAt").exists())
                .andExpect(jsonPath("$.data.meta.elapsedMs").exists())
                .andExpect(jsonPath("$.data.meta.mode").value("demo"))
                .andExpect(jsonPath("$.data.meta.tableCount").value(13))
                .andExpect(jsonPath("$.data.message").doesNotExist())
                .andExpect(jsonPath("$.data.error").doesNotExist());
    }

    @Test
    void fuzzyQueryShouldOmitFullDossierFields() throws Exception {
        mockMvc.perform(get("/kingdee/receive/query")
                        .param("billNo", "CGSH-20261111-000").param("fuzzy", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.found").value(false))
                .andExpect(jsonPath("$.data.candidates.length()").value(2))
                .andExpect(jsonPath("$.data.message").exists())
                .andExpect(jsonPath("$.data.billNo").doesNotExist())
                .andExpect(jsonPath("$.data.bill").doesNotExist())
                .andExpect(jsonPath("$.data.tables").doesNotExist())
                .andExpect(jsonPath("$.data.entries").doesNotExist())
                .andExpect(jsonPath("$.data.summary").doesNotExist());
    }

    @Test
    void batchItemShouldKeepErrorFieldOnlyOnFailure() throws Exception {
        mockMvc.perform(get("/kingdee/receive/queryBatch").param("billNos", "CGSH-20261111-0001,A'B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.foundCount").value(1))
                .andExpect(jsonPath("$.data.missingCount").value(1))
                .andExpect(jsonPath("$.data.totals.entryCount").value(2))
                .andExpect(jsonPath("$.data.totals.attachCount").value(3))
                .andExpect(jsonPath("$.data.mode").value("demo"))
                .andExpect(jsonPath("$.data.items[0].error").doesNotExist())
                .andExpect(jsonPath("$.data.items[1].error").value(true))
                .andExpect(jsonPath("$.data.items[1].billNo").value("A'B"));
    }
}
