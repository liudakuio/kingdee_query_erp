package com.ruoyi.kingdee.controller;

import com.ruoyi.kingdee.common.AjaxResult;
import com.ruoyi.kingdee.service.PurReceiveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购收货单查询接口。响应体与若依前端 request.js 约定一致：{code, msg, data}。
 */
@RestController
@RequestMapping("/kingdee/receive")
public class PurReceiveController {

    private final PurReceiveService service;

    public PurReceiveController(PurReceiveService service) {
        this.service = service;
    }

    /** 表字典、字段字典和 12 条关联关系，前端用于生成动态列标题。 */
    @GetMapping("/meta")
    public AjaxResult meta() {
        return AjaxResult.success(service.meta());
    }

    /** 单号联想 / 模糊匹配候选单据。 */
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
}
