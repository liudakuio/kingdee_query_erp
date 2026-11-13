package com.ruoyi.kingdee.controller;

import com.ruoyi.kingdee.common.AjaxResult;
import com.ruoyi.kingdee.service.VehiclePlanExcelService;
import com.ruoyi.kingdee.service.VehiclePlanService;
import com.ruoyi.kingdee.util.BillNos;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * 采购车辆计划查询接口。响应体与若依前端 request.js 约定一致：{code, msg, data}。
 */
@RestController
@RequestMapping("/kingdee/vehicleplan")
public class VehiclePlanController {

    private final VehiclePlanService service;
    private final VehiclePlanExcelService excelExportService;

    public VehiclePlanController(VehiclePlanService service, VehiclePlanExcelService excelExportService) {
        this.service = service;
        this.excelExportService = excelExportService;
    }

    /** 表字典、关联关系与字段标签。 */
    @GetMapping("/meta")
    public AjaxResult meta() {
        return AjaxResult.success(service.meta());
    }

    /** 主查询：按单号（或主表内码 fid）查整张采购车辆计划的关联数据。 */
    @GetMapping("/query")
    public AjaxResult query(@RequestParam(name = "billNo", required = false) String billNo,
                            @RequestParam(name = "fid", required = false) String fid) {
        return AjaxResult.success(service.query(billNo, fid));
    }

    /** 批量查询：billNos 以中英文逗号分隔。 */
    @GetMapping("/queryBatch")
    public AjaxResult queryBatch(@RequestParam(name = "billNos") String billNos) {
        return AjaxResult.success(service.queryBatch(billNos));
    }

    /** 导出 Excel：table 留空或 all 时导出全部 5 张表，每张一个工作表。 */
    @GetMapping("/export")
    public void export(@RequestParam(name = "billNo", required = false) String billNo,
                       @RequestParam(name = "fid", required = false) String fid,
                       @RequestParam(name = "table", required = false) String table,
                       HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.build(billNo, fid, table);
        String subject = billNo != null && !billNo.trim().isEmpty() ? billNo.trim()
                : (fid != null && !fid.trim().isEmpty() ? fid.trim() : "query");
        writeExcel(response, data, "采购车辆计划_" + subject + "_" + scopeOf(table) + ".xlsx");
    }

    /** 批量导出 Excel：多个单据的数据追加到同一张表，末尾标注来源单据编号。 */
    @GetMapping("/exportBatch")
    public void exportBatch(@RequestParam(name = "billNos") String billNos,
                            @RequestParam(name = "table", required = false) String table,
                            HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.buildBatch(billNos, table);
        writeExcel(response, data,
                "采购车辆计划_批量" + BillNos.parse(billNos).size() + "单_" + scopeOf(table) + ".xlsx");
    }

    private String scopeOf(String table) {
        return table == null || table.trim().isEmpty()
                || VehiclePlanExcelService.ALL_TABLES.equalsIgnoreCase(table.trim()) ? "全部表" : table.trim();
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
