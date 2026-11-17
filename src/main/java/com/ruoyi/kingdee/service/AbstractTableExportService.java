package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.service.export.CustomColumn;
import com.ruoyi.kingdee.service.export.RowExportContext;
import com.ruoyi.kingdee.service.export.TableExportCustomizer;
import com.ruoyi.kingdee.vo.BatchResultVO;
import com.ruoyi.kingdee.vo.DossierResultVO;
import com.ruoyi.kingdee.vo.TableSectionVO;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 表格导出的公共实现：一张表一个工作表；批量导出时多单据的行顺序追加到同一张表，
 * 并在末尾追加「单据编号」列标识来源。所有单元格按文本写入，避免 19 位内码丢精度。
 *
 * <p>导出层只消费强类型 VO 与 domain 实体，不再通过字符串 key 读取查询结果。</p>
 */
public abstract class AbstractTableExportService {

    /** 导出全部表。 */
    public static final String ALL_TABLES = "all";

    private static final Logger log = LoggerFactory.getLogger(AbstractTableExportService.class);
    private static final int MIN_COLUMN_WIDTH = 14;
    private static final int MAX_COLUMN_WIDTH = 60;
    /** 批量导出中标识该行来自哪个单据的追加列 */
    private static final String BILL_NO_HEADER = "单据编号";
    /** 实体字段按列名缓存，避免每行重复反射解析。 */
    private static final ConcurrentMap<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private final Map<String, TableExportCustomizer> customizerByTable;

    protected AbstractTableExportService(ObjectProvider<TableExportCustomizer> customizers) {
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

    /** 子类提供单据档案；返回类型为强类型 VO 基类。 */
    protected abstract DossierResultVO queryDossier(String billNo, String fid);

    /** 子类提供批量档案。 */
    protected abstract BatchResultVO queryBatchDossier(String billNos);

    /** 子类提供可导出的表名列表（决定工作表顺序）。 */
    protected abstract List<String> tableNames();

    /** 子类提供字段中文标签。 */
    protected abstract Map<String, String> columnLabels();

    /** 单据导出：每张表一个工作表。 */
    public byte[] build(String billNo, String fid, String table) throws IOException {
        DossierResultVO dossier = queryDossier(billNo, fid);
        if (!dossier.isFound()) {
            throw new IllegalArgumentException("未找到可导出的单据：" + (billNo != null ? billNo : fid));
        }
        List<String> names = resolveTableNames(table);
        Map<String, String> labels = columnLabels();
        String sourceBillNo = text(dossier.getBillNo());
        String sourceFid = text(dossier.getFid());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            for (String name : names) {
                TableSectionVO section = sectionOf(dossier, name);
                List<ExportRow> rows = new ArrayList<>();
                for (Object row : section.getRows()) {
                    rows.add(new ExportRow(row, sourceBillNo, sourceFid));
                }
                writeSheet(workbook, headerStyle, name, section.getColumns(), rows, false, labels);
            }
            workbook.write(out);
        }
        return out.toByteArray();
    }

    /** 批量导出：每张表一个工作表，多单据的行顺序追加，末尾追加「单据编号」列。 */
    public byte[] buildBatch(String billNos, String table) throws IOException {
        BatchResultVO batch = queryBatchDossier(billNos);
        List<DossierResultVO> items = batch.getItems() == null ? Collections.<DossierResultVO>emptyList() : batch.getItems();
        List<String> names = resolveTableNames(table);
        Map<String, String> labels = columnLabels();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = headerStyle(workbook);
            for (String name : names) {
                List<ExportRow> rows = new ArrayList<>();
                List<String> columns = Collections.emptyList();
                for (DossierResultVO item : items) {
                    if (!item.isFound()) {
                        continue;
                    }
                    TableSectionVO section = sectionOf(item, name);
                    if (columns.isEmpty()) {
                        columns = section.getColumns();
                    }
                    String billNo = text(item.getBillNo());
                    String fid = text(item.getFid());
                    for (Object row : section.getRows()) {
                        rows.add(new ExportRow(row, billNo, fid));
                    }
                }
                writeSheet(workbook, headerStyle, name, columns, rows, true, labels);
            }
            workbook.write(out);
        }
        return out.toByteArray();
    }

    /** 写入一个工作表；告警按 sheet 去重，不因单行取值失败中断整份导出。 */
    private void writeSheet(XSSFWorkbook workbook, CellStyle headerStyle, String tableName,
                            List<String> columns, List<ExportRow> rows,
                            boolean withBillNo, Map<String, String> labels) {
        List<CustomColumn> extraColumns = extraColumnsOf(tableName);
        int billNoIndex = columns.size() + extraColumns.size();
        int[] widths = new int[billNoIndex + (withBillNo ? 1 : 0)];
        Set<String> warnedTables = new HashSet<>();

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

        int rowIndex = 1;
        for (ExportRow source : rows) {
            Map<String, Object> values = valuesOf(source.entity);
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                String text = text(values.get(columns.get(i)));
                row.createCell(i).setCellValue(text);
                widths[i] = Math.max(widths[i], displayLength(text));
            }
            RowExportContext context = new RowExportContext(
                    source.entity, values, source.billNo, source.fid, tableName);
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

    /** table 为空 / all 时按业务定义顺序导出全部表；否则只导出指定表。 */
    protected List<String> resolveTableNames(String table) {
        if (table == null || table.trim().isEmpty() || ALL_TABLES.equalsIgnoreCase(table.trim())) {
            return tableNames();
        }
        String name = table.trim();
        if (!tableNames().contains(name)) {
            throw new IllegalArgumentException("不支持导出的表：" + name);
        }
        return Collections.singletonList(name);
    }

    /** 从强类型档案中取指定表的数据块。 */
    private TableSectionVO sectionOf(DossierResultVO dossier, String tableName) {
        Map<String, TableSectionVO> tables = dossier.getTables();
        TableSectionVO section = tables == null ? null : tables.get(tableName);
        if (section == null) {
            TableSectionVO empty = new TableSectionVO(tableName, tableName, "", "");
            empty.setColumns(Collections.<String>emptyList());
            empty.setRows(Collections.<Object>emptyList());
            return empty;
        }
        return section;
    }

    /** 读取实体字段值并按列名建立索引；字段元数据按类型缓存。 */
    private Map<String, Object> valuesOf(Object entity) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (entity == null) {
            return values;
        }
        for (Map.Entry<String, Field> entry : fieldsOf(entity.getClass()).entrySet()) {
            try {
                values.put(entry.getKey(), entry.getValue().get(entity));
            } catch (IllegalAccessException e) {
                values.put(entry.getKey(), null);
            }
        }
        return values;
    }

    private Map<String, Field> fieldsOf(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, clazz -> {
            Map<String, Field> fields = new LinkedHashMap<>();
            for (Class<?> current = clazz; current != null && current != Object.class;
                 current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    fields.putIfAbsent(field.getName(), field);
                }
            }
            return fields;
        });
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

    /** 导出行的来源：实体数据 + 所属单据编号与内码。 */
    private static final class ExportRow {
        private final Object entity;
        private final String billNo;
        private final String fid;

        private ExportRow(Object entity, String billNo, String fid) {
            this.entity = entity;
            this.billNo = billNo;
            this.fid = fid;
        }
    }
}
