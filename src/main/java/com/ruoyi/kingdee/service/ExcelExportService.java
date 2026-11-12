package com.ruoyi.kingdee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.constant.PurReceiveSchema;
import com.ruoyi.kingdee.constant.PurReceiveSchema.TableDef;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 把查询结果导出为 .xlsx：一张表一个工作表，表头同时给出中文标签与数据库列名。
 * 所有单元格按文本写入，避免 19 位内码在 Excel 中被转成 double 后丢精度。
 */
@Service
public class ExcelExportService {

    /** 导出全部 13 张表。 */
    public static final String ALL_TABLES = "all";

    private static final int MIN_COLUMN_WIDTH = 14;
    private static final int MAX_COLUMN_WIDTH = 60;

    private final PurReceiveService queryService;
    private final ObjectMapper objectMapper;

    public ExcelExportService(PurReceiveService queryService, ObjectMapper objectMapper) {
        this.queryService = queryService;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成 xlsx 字节流。所有校验与查询都在调用方设置下载响应头之前完成，
     * 失败时可由 GlobalExceptionHandler 正常返回 JSON 错误提示。
     *
     * @param table 表名，留空或 all 表示全部表
     */
    public byte[] build(String billNo, String fid, String table) throws IOException {
        Map<String, Object> dossier = queryService.query(billNo, fid, false);
        if (!Boolean.TRUE.equals(dossier.get("found"))) {
            throw new IllegalArgumentException("未找到可导出的采购收货单：" + (billNo != null ? billNo : fid));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> tables = (Map<String, Object>) dossier.get("tables");
        List<String> tableNames = resolveTables(table, tables);
        Map<String, String> labels = PurReceiveSchema.columnLabels();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            for (String name : tableNames) {
                writeSheet(workbook, headerStyle, name, tables, labels);
            }
            workbook.write(out);
        }
        return out.toByteArray();
    }

    /** table 为空 / all 时按表字典顺序导出全部表；否则只导出指定表。 */
    private List<String> resolveTables(String table, Map<String, Object> tables) {
        if (table == null || table.trim().isEmpty() || ALL_TABLES.equalsIgnoreCase(table.trim())) {
            List<String> names = new ArrayList<>();
            for (TableDef def : PurReceiveSchema.tableDefs()) {
                names.add(def.getName());
            }
            return names;
        }
        String name = table.trim();
        if (!tables.containsKey(name)) {
            throw new IllegalArgumentException("不支持导出的表：" + name);
        }
        return Collections.singletonList(name);
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void writeSheet(XSSFWorkbook workbook, CellStyle headerStyle, String tableName,
                            Map<String, Object> tables, Map<String, String> labels) {
        @SuppressWarnings("unchecked")
        Map<String, Object> section = (Map<String, Object>) tables.get(tableName);
        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) section.get("columns");
        @SuppressWarnings("unchecked")
        List<Object> rows = (List<Object>) section.getOrDefault("rows", Collections.emptyList());

        Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(tableName));
        sheet.createFreezePane(0, 1);

        int[] widths = new int[columns.size()];
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            String text = headerOf(labels, columns.get(i));
            Cell cell = header.createCell(i);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
            widths[i] = displayLength(text);
        }

        int rowIndex = 1;
        for (Object rowObject : rows) {
            Map<?, ?> values = objectMapper.convertValue(rowObject, Map.class);
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                Object value = values.get(columns.get(i));
                String text = value == null ? "" : String.valueOf(value);
                row.createCell(i).setCellValue(text);
                widths[i] = Math.max(widths[i], displayLength(text));
            }
        }

        for (int i = 0; i < widths.length; i++) {
            int width = Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, widths[i] + 4));
            sheet.setColumnWidth(i, width * 256);
        }
    }

    /** 表头同时展示中文标签和数据库列名，便于导出后核对字段。 */
    private String headerOf(Map<String, String> labels, String column) {
        String label = labels.get(column);
        return label == null ? column : label + " (" + column + ")";
    }

    /** 中文按两个字符宽估算，避免表头被截断。 */
    private int displayLength(String text) {
        int length = 0;
        for (char c : text.toCharArray()) {
            length += c > 127 ? 2 : 1;
        }
        return length;
    }
}
