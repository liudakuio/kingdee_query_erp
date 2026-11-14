package com.ruoyi.kingdee;

import com.ruoyi.kingdee.service.BoxNoFixService;
import com.ruoyi.kingdee.service.fix.BoxNoExcelSupport;
import com.ruoyi.kingdee.service.fix.BoxNoExcelVo;
import com.ruoyi.kingdee.service.fix.BoxNoFixRow;
import com.ruoyi.kingdee.service.fix.DefaultBoxNoFixHandler;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 箱号信息修正的单元测试：按 fentryid 匹配上传表、列名大小写容错、追加列与颜色标记、异常分支。
 */
class BoxNoFixServiceTest {

    private final BoxNoFixService service = new BoxNoFixService(new DefaultBoxNoFixHandler());

    @Test
    void fixShouldMatchByFentryIdAndMarkRows() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "box.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", buildUploadWorkbook());

        Map<String, Object> matched = new LinkedHashMap<>();
        matched.put("fentryid", 3001L);
        matched.put("fk_usp_sendboxno", "BOX-OLD");
        matched.put("fk_usp_wxno", "CONT-OLD");
        matched.put("fk_usp_realqty", 10);

        Map<String, Object> notInUpload = new LinkedHashMap<>();
        notInUpload.put("fentryid", 3009L);
        notInUpload.put("fk_usp_sendboxno", "BOX-009");
        notInUpload.put("fk_usp_wxno", "CONT-Z");
        notInUpload.put("fk_usp_realqty", 5);

        Map<String, Object> withoutKey = new LinkedHashMap<>();
        withoutKey.put("fk_usp_sendboxno", "BOX-NOKEY");
        withoutKey.put("fk_usp_wxno", "CONT-Y");
        withoutKey.put("fk_usp_realqty", 1);

        byte[] result = service.fix(file, Arrays.asList(matched, notInUpload, withoutKey));

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            assertEquals(6, header.getPhysicalNumberOfCells());
            assertEquals(BoxNoExcelSupport.CORRECT_BOX_NO_HEADER, header.getCell(4).getStringCellValue());
            assertEquals(BoxNoExcelSupport.MESSAGE_HEADER, header.getCell(5).getStringCellValue());

            Row first = sheet.getRow(1);
            assertEquals("3001", first.getCell(0).getStringCellValue());
            assertEquals("BOX-0001", first.getCell(4).getStringCellValue());
            assertEquals("已按上传表修正（fentryid=3001）", first.getCell(5).getStringCellValue());
            assertEquals(FillPatternType.SOLID_FOREGROUND, first.getCell(0).getCellStyle().getFillPattern());

            Row second = sheet.getRow(2);
            assertEquals("3009", second.getCell(0).getStringCellValue());
            assertEquals("", second.getCell(4).getStringCellValue());
            assertEquals("上传表中未找到该 fentryid：3009", second.getCell(5).getStringCellValue());

            Row third = sheet.getRow(3);
            assertEquals("", third.getCell(4).getStringCellValue());
            assertEquals("当前行缺少 fentryid，无法匹配", third.getCell(5).getStringCellValue());
            assertEquals(3, sheet.getLastRowNum());
        }
    }

    @Test
    void excelVoShouldCarryColumnsAndResult() {
        Map<String, Object> current = new LinkedHashMap<>();
        current.put("fentryid", 3001L);
        current.put("fk_usp_sendboxno", "BOX-OLD");
        current.put("fk_usp_realqty", null);
        BoxNoFixRow row = BoxNoFixRow.of(current);
        row.setMatched(true);
        row.setMessage("已按上传表修正（fentryid=3001）");
        Map<String, String> applied = new LinkedHashMap<>();
        applied.put("FENTRYID", "3001");
        applied.put("fk_usp_sendboxno", "BOX-0001");
        row.setApplied(applied);

        BoxNoExcelVo vo = BoxNoExcelVo.of(row);
        assertEquals("3001", vo.getValues().get("fentryid"));
        assertEquals("", vo.getValues().get("fk_usp_realqty"));
        assertEquals("BOX-0001", vo.getCorrectBoxNo());
        assertEquals("已按上传表修正（fentryid=3001）", vo.getMessage());
        assertTrue(vo.isMatched());
    }

    @Test
    void fixShouldRejectEmptyUploadOrRows() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "box.xlsx", null, new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> service.fix(empty, Arrays.asList()));

        MockMultipartFile file = new MockMultipartFile("file", "box.xlsx", null, buildUploadWorkbook());
        assertThrows(IllegalArgumentException.class, () -> service.fix(file, Arrays.asList()));
    }

    /** 上传的完整箱号信息表：fentryid（故意用大写表头验证大小写容错）+ 箱号、集装箱号、数量 */
    private byte[] buildUploadWorkbook() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("箱号信息");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("FENTRYID");
            header.createCell(1).setCellValue("fk_usp_sendboxno");
            header.createCell(2).setCellValue("fk_usp_wxno");
            header.createCell(3).setCellValue("fk_usp_realqty");
            Row first = sheet.createRow(1);
            first.createCell(0).setCellValue(3001);
            first.createCell(1).setCellValue("BOX-0001");
            first.createCell(2).setCellValue("CONT-NEW");
            first.createCell(3).setCellValue(10);
            Row second = sheet.createRow(2);
            second.createCell(0).setCellValue(3003);
            second.createCell(1).setCellValue("BOX-0003");
            second.createCell(2).setCellValue("CONT-B");
            second.createCell(3).setCellValue(20);
            workbook.write(out);
            return out.toByteArray();
        }
    }
}