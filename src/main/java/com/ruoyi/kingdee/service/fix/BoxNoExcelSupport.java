package com.ruoyi.kingdee.service.fix;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 箱号信息 Excel 的读写工具：
 * 读取时按「列名」识别字段（兼容完整箱件字段表），生成时以当前箱号信息为底追加「正确箱号 / 处理说明」列，
 * 并给已修正、未匹配的行上色。所有单元格按文本写入，避免 19 位内码丢精度。
 */
public final class BoxNoExcelSupport {

    /** 追加列：上传表中命中的正确箱号 */
    public static final String CORRECT_BOX_NO_HEADER = "正确箱号";
    /** 追加列：处理说明 */
    public static final String MESSAGE_HEADER = "处理说明";
    /** 已修正行的底色（浅黄） */
    private static final short MATCHED_COLOR = IndexedColors.LIGHT_YELLOW.getIndex();
    /** 未匹配行的底色（浅红） */
    private static final short UNMATCHED_COLOR = IndexedColors.ROSE.getIndex();

    private BoxNoExcelSupport() {
    }

    /**
     * 解析上传的箱号信息表：取首个工作表，首行为表头（列名去空格），数据行转换为实体。
     *
     * @return 上传行实体列表；业务层不再直接消费动态 Map
     */
    public static List<UploadedBoxNoRow> read(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传箱号信息 Excel");
        }
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException("上传的 Excel 没有工作表");
            }
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) {
                throw new IllegalArgumentException("上传的 Excel 缺少表头行");
            }
            List<String> headers = new ArrayList<>();
            for (int c = header.getFirstCellNum(); c < header.getLastCellNum(); c++) {
                headers.add(cellText(header.getCell(c)));
            }
            List<UploadedBoxNoRow> rows = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                boolean blank = true;
                for (int c = 0; c < headers.size(); c++) {
                    String name = headers.get(c);
                    if (name.isEmpty()) {
                        continue;
                    }
                    String value = cellText(row.getCell(c));
                    values.put(name, value);
                    blank = blank && value.isEmpty();
                }
                if (!blank) {
                    rows.add(UploadedBoxNoRow.of(values));
                }
            }
            return rows;
        }
    }

    /** 单元格转文本：数字用普通字符串表示，日期按 yyyy-MM-dd HH:mm:ss，公式取其文本结果。 */
    private static String cellText(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                }
                return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula() == null ? "" : cell.getCellFormula().trim();
            default:
                return "";
        }
    }

    /**
     * 生成「当前箱号信息 + 处理结果」的 Excel：
     * 表头 = 当前箱号信息列 + 正确箱号 + 处理说明；已修正行浅黄、未匹配行浅红。
     */
    public static byte[] write(List<BoxNoExcelVo> rows, List<String> columns, String sheetName) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
            sheet.createFreezePane(0, 1);

            CellStyle headerStyle = workbook.createCellStyle();
            Font bold = workbook.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle matchedStyle = workbook.createCellStyle();
            matchedStyle.setFillForegroundColor(MATCHED_COLOR);
            matchedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle unmatchedStyle = workbook.createCellStyle();
            unmatchedStyle.setFillForegroundColor(UNMATCHED_COLOR);
            unmatchedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            int[] widths = new int[columns.size() + 2];
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                String name = columns.get(i);
                Cell cell = header.createCell(i);
                cell.setCellValue(name);
                cell.setCellStyle(headerStyle);
                widths[i] = displayLength(name);
            }
            int correctIndex = columns.size();
            int messageIndex = columns.size() + 1;
            Cell correctHeader = header.createCell(correctIndex);
            correctHeader.setCellValue(CORRECT_BOX_NO_HEADER);
            correctHeader.setCellStyle(headerStyle);
            widths[correctIndex] = displayLength(CORRECT_BOX_NO_HEADER);
            Cell messageHeader = header.createCell(messageIndex);
            messageHeader.setCellValue(MESSAGE_HEADER);
            messageHeader.setCellStyle(headerStyle);
            widths[messageIndex] = displayLength(MESSAGE_HEADER);

            int rowIndex = 1;
            for (BoxNoExcelVo vo : rows) {
                Row row = sheet.createRow(rowIndex++);
                CellStyle style = vo.isMatched() ? matchedStyle : unmatchedStyle;
                Map<String, String> values = vo.getValues();
                for (int i = 0; i < columns.size(); i++) {
                    String value = values.get(columns.get(i));
                    Cell cell = row.createCell(i);
                    cell.setCellValue(value == null ? "" : value);
                    cell.setCellStyle(style);
                    widths[i] = Math.max(widths[i], displayLength(cell.getStringCellValue()));
                }
                Cell correct = row.createCell(correctIndex);
                correct.setCellValue(vo.getCorrectBoxNo());
                correct.setCellStyle(style);
                widths[correctIndex] = Math.max(widths[correctIndex], displayLength(correct.getStringCellValue()));
                Cell message = row.createCell(messageIndex);
                message.setCellValue(vo.getMessage());
                message.setCellStyle(style);
                widths[messageIndex] = Math.max(widths[messageIndex], displayLength(message.getStringCellValue()));
            }

            for (int i = 0; i < widths.length; i++) {
                int width = Math.max(14, Math.min(60, widths[i] + 4));
                sheet.setColumnWidth(i, width * 256);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /** 中文按两个字符宽估算，避免表头与内容被截断 */
    private static int displayLength(String text) {
        int length = 0;
        for (char c : text.toCharArray()) {
            length += c > 127 ? 2 : 1;
        }
        return length;
    }
}