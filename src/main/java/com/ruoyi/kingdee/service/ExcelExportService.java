package com.ruoyi.kingdee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.constant.PurReceiveSchema;
import com.ruoyi.kingdee.constant.PurReceiveSchema.TableDef;
import com.ruoyi.kingdee.service.export.CustomColumn;
import com.ruoyi.kingdee.service.export.RowExportContext;
import com.ruoyi.kingdee.service.export.TableExportCustomizer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 把查询结果导出为 .xlsx：一张表一个工作表，表头同时给出中文标签与数据库列名。
 * 所有单元格按文本写入，避免 19 位内码在 Excel 中被转成 double 后丢精度。
 * 表级定制通过 TableExportCustomizer 扩展点注入，仅在原有列末尾追加自定义列。
 */
@Service
public class ExcelExportService {

    /** 导出全部 13 张表。 */
    public static final String ALL_TABLES = "all";

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
    private static final int MIN_COLUMN_WIDTH = 14;
    private static final int MAX_COLUMN_WIDTH = 60;
    /** 批量导出中标识该行来自哪个单据的追加列 */
    private static final String BILL_NO_HEADER = "单据编号";

    private final PurReceiveService queryService;
    private final ObjectMapper objectMapper;
    private final Map<String, TableExportCustomizer> customizerByTable;

    public ExcelExportService(PurReceiveService queryService, ObjectMapper objectMapper,
                              ObjectProvider<TableExportCustomizer> customizers) {
        this.queryService = queryService;
        this.objectMapper = objectMapper;
        Map<String, TableExportCustomizer> index = new HashMap<>();
        for (TableExportCustomizer customizer : customizers.orderedStream().collect(Collectors.toList())) {
            TableExportCustomizer previous = index.put(customizer.tableName(), customizer);
            if (previous != null) {
                throw new IllegalStateException("表 " + customizer.tableName() + " 重复注册导出定制器："
                        + previous.getClass().getName() + " / " + customizer.getClass().getName());
            }
        }
        this.customizerByTable = Collections.unmodifiableMap(index);
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
        ExportSession session = new ExportSession(
                dossier.get("billNo") == null ? billNo : String.valueOf(dossier.get("billNo")),
                dossier.get("fid") == null ? fid : dossier.get("fid"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            for (String name : tableNames) {
                writeSheet(workbook, headerStyle, name, name, tables, labels, session);
            }
            workbook.write(out);
        }
        return out.toByteArray();
    }

    /**
     * 批量导出：多个单号按逗号分隔，每张表只生成一个工作表。
     * 同一张表的多个单据数据顺序追加，并在末尾以「单据编号」列标识来源。
     * 表级自定义列按每一行所属单据的上下文计算。
     */
    public byte[] buildBatch(String billNos, String table) throws IOException {
        Map<String, Object> batch = queryService.queryBatch(billNos);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) batch.get("items");
        List<String> tableNames = resolveTableNames(table);
        Map<String, String> labels = PurReceiveSchema.columnLabels();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            for (String name : tableNames) {
                writeMergedSheet(workbook, headerStyle, name, items, labels);
            }
            workbook.write(out);
        }
        return out.toByteArray();
    }

    /** 批量导出时按表字典校验表名，不依赖某个单据的 tables 分区。 */
    private List<String> resolveTableNames(String table) {
        if (table == null || table.trim().isEmpty() || ALL_TABLES.equalsIgnoreCase(table.trim())) {
            List<String> names = new ArrayList<>();
            for (TableDef def : PurReceiveSchema.tableDefs()) {
                names.add(def.getName());
            }
            return names;
        }
        String name = table.trim();
        PurReceiveSchema.tableDef(name);
        return Collections.singletonList(name);
    }

    /** 同一张表一个 sheet：表头只写一次，多个单据的行按顺序追加。 */
    private void writeMergedSheet(XSSFWorkbook workbook, CellStyle headerStyle, String tableName,
                                  List<Map<String, Object>> items, Map<String, String> labels) {
        List<String> columns = columnsOfFirst(items, tableName);
        List<CustomColumn> extraColumns = extraColumnsOf(tableName);
        int billNoIndex = columns.size() + extraColumns.size();
        int[] widths = new int[billNoIndex + 1];

        Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(tableName));
        sheet.createFreezePane(0, 1);

        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            String text = headerOf(labels, columns.get(i));
            Cell cell = header.createCell(i);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
            widths[i] = displayLength(text);
        }
        writeExtraHeaders(header, headerStyle, columns.size(), extraColumns, widths);
        Cell sourceHeader = header.createCell(billNoIndex);
        sourceHeader.setCellValue(BILL_NO_HEADER);
        sourceHeader.setCellStyle(headerStyle);
        widths[billNoIndex] = displayLength(BILL_NO_HEADER);

        // 告警去重按 sheet 共享；单据标识按行传入，避免不同单据的行串号
        ExportSession session = new ExportSession("", null);
        int rowIndex = 1;
        for (Map<String, Object> item : items) {
            if (!Boolean.TRUE.equals(item.get("found"))) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> tables = (Map<String, Object>) item.get("tables");
            if (tables == null) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> section = (Map<String, Object>) tables.get(tableName);
            if (section == null) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<Object> rows = (List<Object>) section.getOrDefault("rows", Collections.emptyList());
            String billNo = text(item.get("billNo"));
            Object fid = item.get("fid");
            for (Object rowObject : rows) {
                @SuppressWarnings("unchecked")
                Map<String, Object> values = objectMapper.convertValue(rowObject, Map.class);
                Row row = sheet.createRow(rowIndex++);
                for (int i = 0; i < columns.size(); i++) {
                    Object value = values.get(columns.get(i));
                    String text = value == null ? "" : String.valueOf(value);
                    row.createCell(i).setCellValue(text);
                    widths[i] = Math.max(widths[i], displayLength(text));
                }
                writeExtraCells(row, columns.size(), extraColumns, values, tableName,
                        session, widths, billNo, fid);
                row.createCell(billNoIndex).setCellValue(billNo);
                widths[billNoIndex] = Math.max(widths[billNoIndex], displayLength(billNo));
            }
        }

        for (int i = 0; i < widths.length; i++) {
            int width = Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, widths[i] + 4));
            sheet.setColumnWidth(i, width * 256);
        }
    }

    /** 取第一个查到的单据的列定义，保证同一 sheet 只有一套表头。 */
    private List<String> columnsOfFirst(List<Map<String, Object>> items, String tableName) {
        for (Map<String, Object> item : items) {
            if (!Boolean.TRUE.equals(item.get("found"))) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> tables = (Map<String, Object>) item.get("tables");
            if (tables == null) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> section = (Map<String, Object>) tables.get(tableName);
            if (section == null) {
                continue;
            }
            @SuppressWarnings("unchecked")
            List<String> columns = (List<String>) section.get("columns");
            if (columns != null) {
                return columns;
            }
        }
        return Collections.emptyList();
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private void writeSheet(XSSFWorkbook workbook, CellStyle headerStyle, String sheetName,
                            String tableName, Map<String, Object> tables, Map<String, String> labels,
                            ExportSession session) {
        @SuppressWarnings("unchecked")
        Map<String, Object> section = (Map<String, Object>) tables.get(tableName);
        @SuppressWarnings("unchecked")
        List<String> columns = (List<String>) section.get("columns");
        @SuppressWarnings("unchecked")
        List<Object> rows = (List<Object>) section.getOrDefault("rows", Collections.emptyList());
        List<CustomColumn> extraColumns = extraColumnsOf(tableName);

        Sheet sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));
        sheet.createFreezePane(0, 1);

        int[] widths = new int[columns.size() + extraColumns.size()];
        Row header = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            String text = headerOf(labels, columns.get(i));
            Cell cell = header.createCell(i);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
            widths[i] = displayLength(text);
        }
        writeExtraHeaders(header, headerStyle, columns.size(), extraColumns, widths);

        int rowIndex = 1;
        for (Object rowObject : rows) {
            @SuppressWarnings("unchecked")
            Map<String, Object> values = objectMapper.convertValue(rowObject, Map.class);
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                Object value = values.get(columns.get(i));
                String text = value == null ? "" : String.valueOf(value);
                row.createCell(i).setCellValue(text);
                widths[i] = Math.max(widths[i], displayLength(text));
            }
            writeExtraCells(row, columns.size(), extraColumns, values, tableName,
                    session, widths, session.billNo, session.fid);
        }

        for (int i = 0; i < widths.length; i++) {
            int width = Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, widths[i] + 4));
            sheet.setColumnWidth(i, width * 256);
        }
    }

    /** 未注册表级处理器时返回空列表，其余表零额外开销。 */
    private List<CustomColumn> extraColumnsOf(String tableName) {
        TableExportCustomizer customizer = customizerByTable.get(tableName);
        if (customizer == null) {
            return Collections.emptyList();
        }
        List<CustomColumn> columns = customizer.extraColumns();
        return columns == null ? Collections.emptyList() : columns;
    }

    private void writeExtraHeaders(Row header, CellStyle headerStyle, int offset,
                                   List<CustomColumn> extraColumns, int[] widths) {
        for (int j = 0; j < extraColumns.size(); j++) {
            String text = extraColumns.get(j).getHeader();
            Cell cell = header.createCell(offset + j);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
            widths[offset + j] = displayLength(text);
        }
    }

    private void writeExtraCells(Row row, int offset, List<CustomColumn> extraColumns,
                                 Map<String, Object> values, String tableName,
                                 ExportSession session, int[] widths,
                                 String billNo, Object fid) {
        if (extraColumns.isEmpty()) {
            return;
        }
        RowExportContext context = new RowExportContext();
        context.setRow(values);
        context.setBillNo(billNo);
        context.setFid(fid);
        context.setTableName(tableName);

        for (int j = 0; j < extraColumns.size(); j++) {
            CustomColumn column = extraColumns.get(j);
            String text = resolve(column, context, tableName, session);
            row.createCell(offset + j).setCellValue(text);
            widths[offset + j] = Math.max(widths[offset + j], displayLength(text));
        }
    }

    /** 单个自定义列取值失败只写空值，整份导出不中断；同一张表最多告警一次。 */
    private String resolve(CustomColumn column, RowExportContext context,
                           String tableName, ExportSession session) {
        try {
            return column.resolve(context);
        } catch (RuntimeException e) {
            if (session.warnedTables.add(tableName)) {
                log.warn("表 {} 的自定义列「{}」取值失败，已写空值", tableName, column.getHeader(), e);
            }
            return "";
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

    /** 一次导出内的共享信息：单据标识与已告警表集合，避免逐行刷日志。 */
    private static final class ExportSession {
        private final String billNo;
        private final Object fid;
        private final Set<String> warnedTables = new HashSet<>();

        private ExportSession(String billNo, Object fid) {
            this.billNo = billNo;
            this.fid = fid;
        }
    }
}
