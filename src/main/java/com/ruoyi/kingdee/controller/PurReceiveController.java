package com.ruoyi.kingdee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.common.AjaxResult;
import com.ruoyi.kingdee.dto.BatchBillQuery;
import com.ruoyi.kingdee.dto.FixBoxNoRequest;
import com.ruoyi.kingdee.dto.PurReceiveQuery;
import com.ruoyi.kingdee.service.BoxNoFixService;
import com.ruoyi.kingdee.service.ExcelExportService;
import com.ruoyi.kingdee.service.PurReceiveService;
import com.ruoyi.kingdee.util.BillNos;
import com.ruoyi.kingdee.util.ExcelResponseWriter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 采购收货单查询接口。响应体与若依前端 request.js 约定一致：{code, msg, data}。
 */
@RestController
@RequestMapping("/kingdee/receive")
public class PurReceiveController {

    private final PurReceiveService service;
    private final ExcelExportService excelExportService;
    private final BoxNoFixService boxNoFixService;
    private final ObjectMapper objectMapper;

    public PurReceiveController(PurReceiveService service, ExcelExportService excelExportService,
                                BoxNoFixService boxNoFixService, ObjectMapper objectMapper) {
        this.service = service;
        this.excelExportService = excelExportService;
        this.boxNoFixService = boxNoFixService;
        this.objectMapper = objectMapper;
    }

    /** 表字典、字段字典和 12 条关联关系，前端用于生成动态列标题。 */
    @GetMapping("/meta")
    public AjaxResult meta() {
        return AjaxResult.success(service.meta());
    }

    /** 单号联想 / 模糊匹配候选单据。 */
    @GetMapping("/list")
    public AjaxResult list(@ModelAttribute PurReceiveQuery query) {
        return AjaxResult.success(service.suggest(query.getBillNo()));
    }

    /**
     * 主查询：按单号查整张收货单的 13 张表关联数据。
     * fid 用于从重复单号候选中精确选择；fuzzy=true 时只返回候选单据列表。
     */
    @GetMapping("/query")
    public AjaxResult query(@ModelAttribute PurReceiveQuery query) {
        return AjaxResult.success(service.query(query));
    }

    /**
     * 批量查询：billNos 以中英文逗号分隔，返回批次汇总对象 + items 全量单据结果。
     * 单个单号非法或未找到只标记该项，不影响其它单号。
     */
    @GetMapping("/queryBatch")
    public AjaxResult queryBatch(@ModelAttribute BatchBillQuery query) {
        return AjaxResult.success(service.queryBatch(query.getBillNos()));
    }

    /**
     * 导出 Excel：table 留空或 all 时导出全部 13 张表（每张一个工作表），指定表名时只导出该表。
     * 先生成完整文件再写响应头，查询失败时仍由 GlobalExceptionHandler 返回 JSON 提示。
     */
    @GetMapping("/export")
    public void export(@ModelAttribute PurReceiveQuery query, HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.build(query.getBillNo(), query.getFid(), query.getTable());
        String subject = query.getBillNo() != null && !query.getBillNo().trim().isEmpty()
                ? query.getBillNo().trim()
                : (query.getFid() != null && !query.getFid().trim().isEmpty() ? query.getFid().trim() : "query");
        ExcelResponseWriter.write(response, data,
                "采购收货单_" + subject + "_" + ExcelResponseWriter.scopeOf(
                        query.getTable(), ExcelExportService.ALL_TABLES) + ".xlsx");
    }

    /**
     * 批量导出 Excel：billNos 以逗号分隔，每张表只生成一个工作表，
     * 多个单据的数据顺序追加并以「单据编号」列标识来源。
     */
    @GetMapping("/exportBatch")
    public void exportBatch(@ModelAttribute BatchBillQuery query, HttpServletResponse response) throws IOException {
        byte[] data = excelExportService.buildBatch(query.getBillNos(), query.getTable());
        ExcelResponseWriter.write(response, data,
                "采购收货单_批量" + BillNos.parse(query.getBillNos()).size() + "单_" + ExcelResponseWriter.scopeOf(
                        query.getTable(), ExcelExportService.ALL_TABLES) + ".xlsx");
    }

    /**
     * 修改箱号信息：上传正确的箱号信息 Excel + 当前查询到的箱号信息行，后端处理后返回结果 Excel。
     * 处理规则由 service/fix 下的 BoxNoFixHandler 扩展点决定。
     */
    @PostMapping("/fixBoxNo")
    public void fixBoxNo(@ModelAttribute FixBoxNoRequest request, HttpServletResponse response) throws IOException {
        byte[] data = boxNoFixService.fix(request.getFile(), request.parseRows(objectMapper));
        ExcelResponseWriter.write(response, data,
                "箱号信息修正结果_" + System.currentTimeMillis() + ".xlsx");
    }
}
