package com.ruoyi.kingdee;

import com.ruoyi.kingdee.controller.ExcelProcessController;
import com.ruoyi.kingdee.service.ExcelProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 通用 Excel 处理接口测试：校验文件格式、下载响应头与错误 JSON。
 */
class ExcelProcessControllerTest {

    private ExcelProcessService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ExcelProcessService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ExcelProcessController(service)).build();
    }

    @Test
    void processShouldReturnExcelAttachment() throws Exception {
        byte[] bytes = "mock-excel-content".getBytes("UTF-8");
        when(service.process(any(InputStream.class), eq(""), eq(""))).thenReturn(bytes);

        MockMultipartFile file = new MockMultipartFile(
                "file", "箱号信息.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bytes);

        mockMvc.perform(multipart("/kingdee/tool/excel/process").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("filename*=UTF-8''"),
                                org.hamcrest.Matchers.endsWith(".xlsx"))))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void processShouldRejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.xlsx", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/kingdee/tool/excel/process").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("请选择要上传的 Excel 文件"));
    }

    @Test
    void processShouldRejectInvalidExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "bad.txt", "text/plain", "not-excel".getBytes("UTF-8"));

        mockMvc.perform(multipart("/kingdee/tool/excel/process").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("仅支持 .xlsx / .xls 格式的文件"));
    }
}
