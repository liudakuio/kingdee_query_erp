package com.ruoyi.kingdee.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Excel 下载响应的统一写入工具，消除三个 Controller 中重复的响应头处理。
 */
public final class ExcelResponseWriter {

    private ExcelResponseWriter() {
    }

    /** table 留空或 all 时返回「全部表」，否则返回原表名。 */
    public static String scopeOf(String table, String allTables) {
        return table == null || table.trim().isEmpty() || allTables.equalsIgnoreCase(table.trim())
                ? "全部表" : table.trim();
    }

    /**
     * 写入 xlsx 下载响应；文件名同时兼容 filename 与 RFC 5987 的 filename*。
     */
    public static void write(HttpServletResponse response, byte[] data, String rawFileName) throws IOException {
        String fileName = URLEncoder.encode(rawFileName, "UTF-8").replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(data.length);
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + fileName);
        response.getOutputStream().write(data);
    }
}
