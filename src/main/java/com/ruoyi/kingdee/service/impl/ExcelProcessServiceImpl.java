package com.ruoyi.kingdee.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import com.ruoyi.kingdee.mapper.receive.PurReceiveMapper;
import com.ruoyi.kingdee.service.ExcelProcessService;
import com.ruoyi.kingdee.util.SnowflakeIdUtils;
import com.ruoyi.kingdee.vo.ExcelProcessRowVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 通用 Excel 处理服务实现。
 *
 * <p>移植外部参考实现的 process / doBusiness 逻辑：固定 11 列读入，按 K 列 code 查询 fentryid，
 * 生成雪花 fdetailid，fseq 按 fentryid 分组递增。查询按 code 做本地缓存，避免大文件 N+1 查询。</p>
 */
@Service
@DS("scm")
public class ExcelProcessServiceImpl implements ExcelProcessService {

    private static final Logger log = LoggerFactory.getLogger(ExcelProcessServiceImpl.class);

    private final PurReceiveMapper mapper;

    public ExcelProcessServiceImpl(PurReceiveMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public byte[] process(InputStream inputStream, String insertFentryIds, String updateFentryIds) throws IOException {
        Set<String> insertIds = parseFentryIds(insertFentryIds);
        Set<String> updateIds = parseFentryIds(updateFentryIds);
        if (!insertIds.isEmpty() || !updateIds.isEmpty()) {
            log.debug("通用 Excel 处理预留参数暂不参与计算：insert={}, update={}", insertIds.size(), updateIds.size());
        }

        List<ExcelProcessRowVO> rows = EasyExcel.read(inputStream)
                .head(ExcelProcessRowVO.class)
                .autoCloseStream(false)
                .sheet()
                .doReadSync();

        List<ExcelProcessRowVO> result = doBusiness(rows);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        EasyExcel.write(output)
                .head(ExcelProcessRowVO.class)
                .sheet("处理结果")
                .doWrite(result == null ? Collections.<ExcelProcessRowVO>emptyList() : result);
        return output.toByteArray();
    }

    /** 业务处理：回填 fentryid、fdetailid 与分组递增的 fseq。 */
    private List<ExcelProcessRowVO> doBusiness(List<ExcelProcessRowVO> rows) {
        if (rows == null || rows.isEmpty()) {
            return rows;
        }

        Map<String, Integer> seqByFentryId = new HashMap<>();
        Map<String, ReceivingBoxEntry> entityByCode = new HashMap<>();

        for (int i = 0; i < rows.size(); i++) {
            ExcelProcessRowVO row = rows.get(i);
            if (row == null) {
                continue;
            }
            String code = trim(row.getCode());
            if (code == null) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 行 K 列（updateSql）缺少查询编码");
            }

            ReceivingBoxEntry entity = entityByCode.get(code);
            if (entity == null && !entityByCode.containsKey(code)) {
                entity = mapper.selectByCode(code);
                entityByCode.put(code, entity);
            }
            if (entity == null || entity.getFentryid() == null) {
                throw new IllegalArgumentException("第 " + (i + 1) + " 行编码 " + code + " 未匹配到箱号信息");
            }

            row.setFdetailid(String.valueOf(SnowflakeIdUtils.nextId()));
            String fentryId = String.valueOf(entity.getFentryid());
            row.setFentryid(fentryId);

            int nextSeq = seqByFentryId.getOrDefault(fentryId, 0) + 1;
            row.setFseq(nextSeq);
            seqByFentryId.put(fentryId, nextSeq);
        }
        return rows;
    }

    /** 拆分预留 fentryid 输入，支持中英文逗号、空白与换行。 */
    private Set<String> parseFentryIds(String raw) {
        Set<String> ids = new LinkedHashSet<>();
        if (raw == null || raw.trim().isEmpty()) {
            return ids;
        }
        for (String part : raw.split("[,，\\s]+")) {
            String id = normalizeFentryId(part);
            if (id != null && !id.isEmpty()) {
                ids.add(id);
            }
        }
        return ids;
    }

    /** 归一化 Excel 数值单元格读出的 "1001.0" 为 "1001"。 */
    private String normalizeFentryId(String value) {
        String id = trim(value);
        if (id != null && id.matches("-?\\d+\\.0+")) {
            id = id.substring(0, id.indexOf('.'));
        }
        return id;
    }

    private String trim(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
