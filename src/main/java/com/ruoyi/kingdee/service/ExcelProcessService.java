package com.ruoyi.kingdee.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * 通用 Excel 处理服务。
 */
public interface ExcelProcessService {

    /**
     * 读取上传 Excel → 回填业务字段 → 输出处理结果 xlsx。
     *
     * @param inputStream     上传文件输入流，由调用方关闭
     * @param insertFentryIds 预留新增内码参数，当前不参与计算
     * @param updateFentryIds 预留修改内码参数，当前不参与计算
     * @return 结果文件字节内容
     */
    byte[] process(InputStream inputStream, String insertFentryIds, String updateFentryIds) throws IOException;
}
