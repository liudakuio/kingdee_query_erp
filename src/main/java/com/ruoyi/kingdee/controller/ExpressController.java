package com.ruoyi.kingdee.controller;

import com.ruoyi.kingdee.common.AjaxResult;
import com.ruoyi.kingdee.dto.BatchBillQuery;
import com.ruoyi.kingdee.dto.BillQuery;
import com.ruoyi.kingdee.service.ExpressExcelService;
import com.ruoyi.kingdee.service.ExpressService;
import com.ruoyi.kingdee.util.BillNos;
import com.ruoyi.kingdee.util.ExcelResponseWriter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 采购物流单查询接口。响应体与若依前端 request.js 约定一致：{code, msg, data}。
 */
@RestController
@RequestMapping("/kingdee/express")
public class ExpressController {

    private final ExpressService service;
    private final ExpressExcelService excelExportService;

    public ExpressController(ExpressService service, ExpressExcelService excelExportService) {
        this.service = service;
        this.excelExportService = excelExportService;
    }

    /** 主查询：按单号（或主表内码 fid）查整张采购物流单的关联数据。 */
    @GetMapping("/query")
    public AjaxResult query(@ModelAttribute BillQuery query) {
        return AjaxResult.success(service.query(query));
    }

    /** 批量查询：billNos 以中英文逗号分隔。 */
    @GetMapping("/queryBatch")
    public AjaxResult queryBatch(@ModelAttribute BatchBillQuery query) {
        return AjaxResult.success(service.queryBatch(query.getBillNos()));
    }

    /** 导出 Excel：table 留空或 all 时导出全部表，每张一个工作表。 */
    @GetMapping("/export")
    public void export(@ModelAttribute BillQuery query, HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.build(query.getBillNo(), query.getFid(), query.getTable());
        String subject = query.getBillNo() != null && !query.getBillNo().trim().isEmpty()
                ? query.getBillNo().trim()
                : (query.getFid() != null && !query.getFid().trim().isEmpty() ? query.getFid().trim() : "query");
        ExcelResponseWriter.write(response, data,
                "采购物流单_" + subject + "_" + ExcelResponseWriter.scopeOf(
                        query.getTable(), ExpressExcelService.ALL_TABLES) + ".xlsx");
    }

    /** 批量导出 Excel：多个单据的数据追加到同一张表，末尾标注来源单据编号。 */
    @GetMapping("/exportBatch")
    public void exportBatch(@ModelAttribute BatchBillQuery query, HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.buildBatch(query.getBillNos(), query.getTable());
        ExcelResponseWriter.write(response, data,
                "采购物流单_批量" + BillNos.parse(query.getBillNos()).size() + "单_" + ExcelResponseWriter.scopeOf(
                        query.getTable(), ExpressExcelService.ALL_TABLES) + ".xlsx");
    }
}
