package com.ruoyi.kingdee.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 箱号信息修正请求。
 *
 * <p>上传文件与当前结果行一起提交；rows 在 DTO 内转换为实体列表，
 * Controller 不再承担 JSON 反序列化职责。</p>
 */
@Data
public class FixBoxNoRequest {

    /** 上传的正确箱号信息 Excel。 */
    private MultipartFile file;

    /** 当前查询结果的 JSON 数组字符串。 */
    private String rows;

    /**
     * 将前端传来的箱号行还原为强类型实体。
     *
     * @param mapper Spring 容器中的 ObjectMapper
     * @return 当前箱号信息实体列表
     */
    public List<ReceivingBoxEntry> parseRows(ObjectMapper mapper) {
        if (rows == null || rows.trim().isEmpty()) {
            throw new IllegalArgumentException("缺少当前箱号信息数据，请先查询后再上传");
        }
        try {
            return mapper.readValue(rows, new TypeReference<List<ReceivingBoxEntry>>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("箱号信息数据格式不正确：" + e.getMessage());
        }
    }
}
