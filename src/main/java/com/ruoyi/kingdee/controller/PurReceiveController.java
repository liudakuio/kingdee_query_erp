package com.ruoyi.kingdee.controller;

import com.ruoyi.kingdee.common.AjaxResult;
import com.ruoyi.kingdee.service.ExcelExportService;
import com.ruoyi.kingdee.service.PurReceiveService;
import com.ruoyi.kingdee.util.BillNos;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 采购收货单查询接口。响应体与若依前端 request.js 约定一致：{code, msg, data}。
 */
@RestController
@RequestMapping("/kingdee/receive")
public class PurReceiveController {

    private final PurReceiveService service;
    private final ExcelExportService excelExportService;

    public PurReceiveController(PurReceiveService service, ExcelExportService excelExportService) {
        this.service = service;
        this.excelExportService = excelExportService;
    }

    /**
     * 表字典、字段字典和 12 条关联关系，前端用于生成动态列标题。
     */
    @GetMapping("/meta")
    public AjaxResult meta() {
        return AjaxResult.success(service.meta());
    }

    /**
     * 单号联想 / 模糊匹配候选单据。
     */
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(name = "billNo", required = false) String billNo) {
        return AjaxResult.success(service.suggest(billNo));
    }

    /**
     * 主查询：按单号查整张收货单的 13 张表关联数据。
     * fid 用于从重复单号候选中精确选择；fuzzy=true 时只返回候选单据列表。
     */
    @GetMapping("/query")
    public AjaxResult query(@RequestParam(name = "billNo", required = false) String billNo,
                            @RequestParam(name = "fid", required = false) String fid,
                            @RequestParam(name = "fuzzy", required = false, defaultValue = "false") boolean fuzzy) {
        return AjaxResult.success(service.query(billNo, fid, fuzzy));
    }

    /**
     * 批量查询：billNos 以中英文逗号分隔，返回批次汇总对象（数量、合计、mode）+ items 全量单据结果。
     * 单个单号非法或未找到只标记该项，不影响其它单号。
     */
    @GetMapping("/queryBatch")
    public AjaxResult queryBatch(@RequestParam(name = "billNos") String billNos) {
        return AjaxResult.success(service.queryBatch(billNos));
    }

    /**
     * 导出 Excel：table 留空或 all 时导出全部 13 张表（每张一个工作表），指定表名时只导出该表。
     * 先生成完整文件再写响应头，查询失败时仍由 GlobalExceptionHandler 返回 JSON 提示。
     */
    @GetMapping("/export")
    public void export(@RequestParam(name = "billNo", required = false) String billNo,
                       @RequestParam(name = "fid", required = false) String fid,
                       @RequestParam(name = "table", required = false) String table,
                       HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.build(billNo, fid, table);
        String subject = billNo != null && !billNo.trim().isEmpty() ? billNo.trim()
                : fid != null && !fid.trim().isEmpty() ? fid.trim() : "query";
        writeExcel(response, data, "采购收货单_" + subject + "_" + scopeOf(table) + ".xlsx");
    }

    /**
     * 批量导出 Excel：billNos 以逗号分隔，每张表只生成一个工作表，
     * 多个单据的数据顺序追加并以「单据编号」列标识来源。表级自定义列对每一行分别生效。
     */
    @GetMapping("/exportBatch")
    public void exportBatch(@RequestParam(name = "billNos") String billNos,
                            @RequestParam(name = "table", required = false) String table,
                            HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.buildBatch(billNos, table);
        writeExcel(response, data,
                "采购收货单_批量" + BillNos.parse(billNos).size() + "单_" + scopeOf(table) + ".xlsx");
    }

    private String scopeOf(String table) {
        return table == null || table.trim().isEmpty()
                || ExcelExportService.ALL_TABLES.equalsIgnoreCase(table.trim()) ? "全部表" : table.trim();
    }

    /** 统一设置下载响应头，文件名同时兼容 filename 与 RFC 5987 的 filename*。 */
    private void writeExcel(HttpServletResponse response, byte[] data, String rawFileName) throws IOException {
        String fileName = URLEncoder.encode(rawFileName, "UTF-8").replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(data.length);
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
        response.getOutputStream().write(data);
    }
}
