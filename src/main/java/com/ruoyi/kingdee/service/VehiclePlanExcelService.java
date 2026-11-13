package com.ruoyi.kingdee.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.constant.VehiclePlanSchema;
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
 * 采购车辆计划导出：一张表一个工作表；批量导出时多单据的行顺序追加到同一张表，
 * 并在末尾追加「单据编号」列标识来源。所有单元格按文本写入，避免 19 位内码丢精度。
 */
@Service
public class VehiclePlanExcelService {

    /** 导出全部表。 */
    public static final String ALL_TABLES = "all";

    private static final Logger log = LoggerFactory.getLogger(VehiclePlanExcelService.class);
    private static final int MIN_COLUMN_WIDTH = 14;
    private static final int MAX_COLUMN_WIDTH = 60;
    /** 批量导出中标识该行来自哪个单据的追加列 */
    private static final String BILL_NO_HEADER = "单据编号";

    private final VehiclePlanService queryService;
    private final ObjectMapper objectMapper;
    private final Map<String, TableExportCustomizer> customizerByTable;

    public VehiclePlanExcelService(VehiclePlanService queryService, ObjectMapper objectMapper,
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

    /** 单据导出：每张表一个工作表。 */
    public byte[] build(String billNo, String fid, String table) throws IOException {
        Map<String, Object> dossier = queryService.query(billNo, fid);
        if (!Boolean.TRUE.equals(dossier.get("found"))) {
            throw new IllegalArgumentException("未找到可导出的采购车辆计划：" + (billNo != null ? billNo : fid));
        }
        List<String> tableNames = resolveTableNames(table);
        Map<String, String> labels = VehiclePlanSchema.columnLabels();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            for (String name : tableNames) {
                Map<String, Object> section = section(dossier, name);
                List<RowSource> rows = new ArrayList<>();
                for (Object row : rowsOf(section)) {
                    rows.add(new RowSource(row, text(dossier.get("billNo"))));
                }
                writeSheet(workbook, headerStyle, name, columnsOf(section), rows, false, labels);
            }
            workbook.write(out);
        }
        return out.toByteArray();
    }

    /** 批量导出：每张表一个工作表，多单据的行顺序追加，末尾追加「单据编号」列。 */
    public byte[] buildBatch(String billNos, String table) throws IOException {
        Map<String, Object> batch = queryService.queryBatch(billNos);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) batch.get("items");
        List<String> tableNames = resolveTableNames(table);
        Map<String, String> labels = VehiclePlanSchema.columnLabels();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            for (String name : tableNames) {
                List<String> columns = Collections.emptyList();
                List<RowSource> rows = new ArrayList<>();
                for (Map<String, Object> item : items) {
                    if (!Boolean.TRUE.equals(item.get("found"))) {
                        continue;
                    }
                    Map<String, Object> section = section(item, name);
                    if (columns.isEmpty()) {
                        columns = columnsOf(section);
                    }
                    String billNo = text(item.get("billNo"));
                    for (Object row : rowsOf(section)) {
                        rows.add(new RowSource(row, billNo));
                    }
                }
                writeSheet(workbook, headerStyle, name, columns, rows, true, labels);
            }
            workbook.write(out);
        }
        return out.toByteArray();
    }

    private void writeSheet(XSSFWorkbook workbook, CellStyle headerStyle, String tableName,
                            List<String> columns, List<RowSource> rows, boolean withBillNo,
                            Map<String, String> labels) {
        List<CustomColumn> extraColumns = extraColumnsOf(tableName);
        int billNoIndex = columns.size() + extraColumns.size();
        int[] widths = new int[billNoIndex + (withBillNo ? 1 : 0)];

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
        for (int j = 0; j < extraColumns.size(); j++) {
            String text = extraColumns.get(j).getHeader();
            Cell cell = header.createCell(columns.size() + j);
            cell.setCellValue(text);
            cell.setCellStyle(headerStyle);
            widths[columns.size() + j] = displayLength(text);
        }
        if (withBillNo) {
            Cell cell = header.createCell(billNoIndex);
            cell.setCellValue(BILL_NO_HEADER);
            cell.setCellStyle(headerStyle);
            widths[billNoIndex] = displayLength(BILL_NO_HEADER);
        }

        Set<String> warnedTables = new HashSet<>();
        int rowIndex = 1;
        for (RowSource source : rows) {
            @SuppressWarnings("unchecked")
            Map<String, Object> values = objectMapper.convertValue(source.entity, Map.class);
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                Object value = values.get(columns.get(i));
                String text = value == null ? "" : String.valueOf(value);
                row.createCell(i).setCellValue(text);
                widths[i] = Math.max(widths[i], displayLength(text));
            }
            RowExportContext context = new RowExportContext();
            context.setRow(values);
            context.setBillNo(source.billNo);
            context.setTableName(tableName);
            for (int j = 0; j < extraColumns.size(); j++) {
                CustomColumn column = extraColumns.get(j);
                String text = resolve(column, context, tableName, warnedTables);
                row.createCell(columns.size() + j).setCellValue(text);
                widths[columns.size() + j] = Math.max(widths[columns.size() + j], displayLength(text));
            }
            if (withBillNo) {
                row.createCell(billNoIndex).setCellValue(source.billNo);
                widths[billNoIndex] = Math.max(widths[billNoIndex], displayLength(source.billNo));
            }
        }

        for (int i = 0; i < widths.length; i++) {
            int width = Math.max(MIN_COLUMN_WIDTH, Math.min(MAX_COLUMN_WIDTH, widths[i] + 4));
            sheet.setColumnWidth(i, width * 256);
        }
    }

    /** 单个自定义列取值失败只写空值，整份导出不中断；同一张表最多告警一次。 */
    private String resolve(CustomColumn column, RowExportContext context, String tableName,
                           Set<String> warnedTables) {
        try {
            return column.resolve(context);
        } catch (RuntimeException e) {
            if (warnedTables.add(tableName)) {
                log.warn("表 {} 的自定义列「{}」取值失败，已写空值", tableName, column.getHeader(), e);
            }
            return "";
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> section(Map<String, Object> dossier, String tableName) {
        Map<String, Object> tables = (Map<String, Object>) dossier.get("tables");
        if (tables == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> section = (Map<String, Object>) tables.get(tableName);
        return section == null ? Collections.emptyMap() : section;
    }

    @SuppressWarnings("unchecked")
    private List<Object> rowsOf(Map<String, Object> section) {
        List<Object> rows = (List<Object>) section.get("rows");
        return rows == null ? Collections.emptyList() : rows;
    }

    @SuppressWarnings("unchecked")
    private List<String> columnsOf(Map<String, Object> section) {
        List<String> columns = (List<String>) section.get("columns");
        return columns == null ? Collections.emptyList() : columns;
    }

    /** table 为空 / all 时按表字典顺序导出全部表；否则只导出指定表。 */
    private List<String> resolveTableNames(String table) {
        if (table == null || table.trim().isEmpty() || ALL_TABLES.equalsIgnoreCase(table.trim())) {
            List<String> names = new ArrayList<>();
            for (VehiclePlanSchema.TableDef def : VehiclePlanSchema.tableDefs()) {
                names.add(def.getName());
            }
            return names;
        }
        String name = table.trim();
        VehiclePlanSchema.tableDef(name);
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

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** 导出行的来源：实体数据 + 所属单据编号。 */
    private static final class RowSource {
        private final Object entity;
        private final String billNo;

        private RowSource(Object entity, String billNo) {
            this.entity = entity;
            this.billNo = billNo;
        }
    }
}
