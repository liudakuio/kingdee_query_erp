package com.ruoyi.kingdee.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * 可空整数转换器：空单元格、空字符串返回 null。
 *
 * <p>避免 EasyExcel 默认 Integer 转换器在 fseq 为空时抛 NumberFormatException。</p>
 */
public class NullableIntegerConverter implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
                                     GlobalConfiguration globalConfiguration) {
        if (cellData == null || cellData.getType() == null || cellData.getType() == CellDataTypeEnum.EMPTY) {
            return null;
        }
        if (cellData.getType() == CellDataTypeEnum.NUMBER) {
            return cellData.getNumberValue() == null ? null : cellData.getNumberValue().intValue();
        }
        String value = cellData.getStringValue();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return Integer.valueOf(value.trim());
    }

    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value == null ? "" : String.valueOf(value));
    }
}
