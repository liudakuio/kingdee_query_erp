package com.ruoyi.kingdee.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.kingdee.converter.NullableIntegerConverter;
import lombok.Data;

/**
 * 通用 Excel 处理行模型。
 *
 * <p>列索引与外部参考实现保持一致：A~I 为业务数据，J 为 insertSql，K 为查询用 code；
 * K 列在 Excel 表头显示为 updateSql，Java 字段保留 code 名称。</p>
 */
@Data
public class ExcelProcessRowVO {

    /** A 列：发货箱件/袋号 */
    @ExcelProperty(value = "fk_usp_sendboxno", index = 0)
    private String fkUspSendboxno;

    /** B 列：集装箱号 */
    @ExcelProperty(value = "fk_usp_wxno", index = 1)
    private String fkUspWxno;

    /** C 列：源单ID */
    @ExcelProperty(value = "fk_usp_srcsubentryid", index = 2)
    private String fkUspSrcsubentryid;

    /** D 列：分录行号，业务处理时会重新生成 */
    @ExcelProperty(value = "fseq", index = 3, converter = NullableIntegerConverter.class)
    private Integer fseq;

    /** E 列：是否到达 */
    @ExcelProperty(value = "fk_usp_arrived", index = 4)
    private String fkUspArrived;

    /** F 列：实收数量 */
    @ExcelProperty(value = "fk_usp_realqty", index = 5)
    private String fkUspRealqty;

    /** G 列：箱明细内码，业务处理时生成雪花 ID */
    @ExcelProperty(value = "fdetailid", index = 6)
    private String fdetailid;

    /** H 列：应收数量 */
    @ExcelProperty(value = "fk_usp_detailqty", index = 7)
    private String fkUspDetailqty;

    /** I 列：明细行内码，业务处理时由数据库查询结果回填 */
    @ExcelProperty(value = "fentryid", index = 8)
    private String fentryid;

    /** J 列：预留新增 SQL */
    @ExcelProperty(value = "insertSql", index = 9)
    private String insertSql;

    /** K 列：表头为 updateSql，实际作为 selectByCode 的查询编码 */
    @ExcelProperty(value = "updateSql", index = 10)
    private String code;
}
