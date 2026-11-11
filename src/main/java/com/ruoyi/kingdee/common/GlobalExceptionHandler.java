package com.ruoyi.kingdee.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常出口：参数错误直接返回提示，其余异常记录日志后返回 500。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public AjaxResult handleIllegalArgument(IllegalArgumentException e) {
        return AjaxResult.error(AjaxResult.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        log.error("采购收货单查询失败", e);
        return AjaxResult.error("查询失败：" + e.getMessage());
    }
}
