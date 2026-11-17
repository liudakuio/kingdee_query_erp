package com.ruoyi.kingdee.common;

import java.util.HashMap;

/**
 * 与若依前端 request.js 保持一致的响应体：{code, msg, data}。
 *
 * <p>协议例外：为兼容若依约定，本类继承 HashMap 并以 Object 承载 data；
 * 业务代码只允许传入明确 DTO/VO/实体，不得把裸 Map 作为 data。</p>
 */
public class AjaxResult extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int ERROR = 500;

    public AjaxResult(int code, String msg, Object data) {
        super.put("code", code);
        super.put("msg", msg);
        if (data != null) {
            super.put("data", data);
        }
    }

    public static AjaxResult success() {
        return new AjaxResult(SUCCESS, "操作成功", null);
    }

    public static AjaxResult success(Object data) {
        return new AjaxResult(SUCCESS, "查询成功", data);
    }

    public static AjaxResult error(String msg) {
        return new AjaxResult(ERROR, msg, null);
    }

    public static AjaxResult error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }
}
