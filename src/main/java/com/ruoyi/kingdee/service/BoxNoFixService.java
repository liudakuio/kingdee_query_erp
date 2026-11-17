package com.ruoyi.kingdee.service;

import com.ruoyi.kingdee.domain.ReceivingBoxEntry;
import com.ruoyi.kingdee.service.fix.BoxNoExcelSupport;
import com.ruoyi.kingdee.service.fix.BoxNoExcelVo;
import com.ruoyi.kingdee.service.fix.BoxNoFixHandler;
import com.ruoyi.kingdee.service.fix.BoxNoFixRow;
import com.ruoyi.kingdee.service.fix.UploadedBoxNoRow;
import com.ruoyi.kingdee.util.RowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 箱号信息修正的编排：解析上传表 → 调用处理逻辑扩展点 → 生成结果 Excel。
 */
@Service
public class BoxNoFixService {

    private final BoxNoFixHandler handler;

    public BoxNoFixService(BoxNoFixHandler handler) {
        this.handler = handler;
    }

    /**
     * @param file        上传的正确箱号信息 Excel
     * @param currentRows 当前查询到的箱号信息实体行
     * @return 处理完成的结果 Excel 字节流
     */
    public byte[] fix(MultipartFile file, List<ReceivingBoxEntry> currentRows) throws IOException {
        List<UploadedBoxNoRow> uploaded = BoxNoExcelSupport.read(file);
        if (uploaded.isEmpty()) {
            throw new IllegalArgumentException("上传的 Excel 没有可处理的数据行");
        }
        if (currentRows == null || currentRows.isEmpty()) {
            throw new IllegalArgumentException("当前没有可处理的箱号信息数据，请先查询后再上传");
        }
        List<BoxNoFixRow> result = handler.fix(uploaded, currentRows);
        List<BoxNoExcelVo> excelRows = new ArrayList<>();
        for (BoxNoFixRow row : result) {
            excelRows.add(BoxNoExcelVo.of(row));
        }
        return BoxNoExcelSupport.write(excelRows, RowUtils.columnsOf(ReceivingBoxEntry.class),
                "箱号信息修正结果");
    }
}