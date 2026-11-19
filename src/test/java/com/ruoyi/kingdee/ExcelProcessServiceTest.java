package com.ruoyi.kingdee;

import com.alibaba.excel.EasyExcel;
import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import com.ruoyi.kingdee.mapper.receive.PurReceiveMapper;
import com.ruoyi.kingdee.service.ExcelProcessService;
import com.ruoyi.kingdee.service.impl.ExcelProcessServiceImpl;
import com.ruoyi.kingdee.vo.ExcelProcessRowVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用 Excel 处理服务测试：覆盖字段回填、同编码缓存与 fseq 分组递增。
 */
class ExcelProcessServiceTest {

    private PurReceiveMapper mapper;
    private ExcelProcessService service;

    @BeforeEach
    void setUp() {
        mapper = mock(PurReceiveMapper.class);
        service = new ExcelProcessServiceImpl(mapper);
    }

    @Test
    void processShouldFillFentryidAndGroupFseq() throws Exception {
        ExcelProcessRowVO first = row("CGSH-20261111-0001", "BOX-0001");
        ExcelProcessRowVO second = row("CGSH-20261111-0001", "BOX-0002");

        ReceivingBoxEntry entry = new ReceivingBoxEntry();
        entry.setFentryid(1001L);
        when(mapper.selectByCode("CGSH-20261111-0001")).thenReturn(entry);

        byte[] resultBytes = service.process(new ByteArrayInputStream(writeRows(Arrays.asList(first, second))), "", "");
        List<ExcelProcessRowVO> result = readRows(resultBytes);

        assertEquals(2, result.size());
        assertEquals("1001", result.get(0).getFentryid());
        assertEquals("1001", result.get(1).getFentryid());
        assertEquals(Integer.valueOf(1), result.get(0).getFseq());
        assertEquals(Integer.valueOf(2), result.get(1).getFseq());
        assertNotNull(result.get(0).getFdetailid());
        assertNotEquals(result.get(0).getFdetailid(), result.get(1).getFdetailid());
        verify(mapper, times(1)).selectByCode("CGSH-20261111-0001");
    }

    @Test
    void processShouldRejectUnmatchedCode() throws Exception {
        ExcelProcessRowVO row = row("NOT-EXIST", "BOX-9999");
        byte[] input = writeRows(Collections.singletonList(row));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> service.process(new ByteArrayInputStream(input), "", ""));
        assertEquals(true, error.getMessage().contains("未匹配到箱号信息"));
    }

    private ExcelProcessRowVO row(String code, String boxNo) {
        ExcelProcessRowVO row = new ExcelProcessRowVO();
        row.setCode(code);
        row.setFkUspSendboxno(boxNo);
        return row;
    }

    private byte[] writeRows(List<ExcelProcessRowVO> rows) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out)
                .head(ExcelProcessRowVO.class)
                .sheet("上传模板")
                .doWrite(rows);
        return out.toByteArray();
    }

    private List<ExcelProcessRowVO> readRows(byte[] bytes) {
        return EasyExcel.read(new ByteArrayInputStream(bytes))
                .head(ExcelProcessRowVO.class)
                .sheet()
                .doReadSync();
    }
}
