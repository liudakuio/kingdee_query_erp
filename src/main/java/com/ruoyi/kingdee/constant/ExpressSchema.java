package com.ruoyi.kingdee.constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 《采购物流单 ER 图》中的 4 张表 / 3 条关系 / 字段中文标签。
 * 物料明细与箱件信息表与采购车辆计划为同一张表，直接引用 VehiclePlanSchema 的常量与字段标签，避免重复维护。
 */
public final class ExpressSchema {

    /** 采购物流单-主表 */
    public static final String EXPRESS = "tk_usp_express";
    /** 采购物流单-关联追踪表 */
    public static final String EXPRESS_TC = "tk_usp_express_tc";
    /** 物料明细-子表（与车辆计划同表） */
    public static final String CONTAINER_ENTRY = VehiclePlanSchema.CONTAINER_ENTRY;
    /** 箱件信息-子表（与车辆计划同表） */
    public static final String SUB_ENTRY = VehiclePlanSchema.SUB_ENTRY;

    /** 表定义：名称、中文名、关联键、归属分组 */
    public static final class TableDef {
        private final String name;
        private final String label;
        private final String joinKey;
        private final String scope;

        public TableDef(String name, String label, String joinKey, String scope) {
            this.name = name;
            this.label = label;
            this.joinKey = joinKey;
            this.scope = scope;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public String getJoinKey() {
            return joinKey;
        }

        public String getScope() {
            return scope;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", name);
            map.put("label", label);
            map.put("joinKey", joinKey);
            map.put("scope", scope);
            return map;
        }
    }

    private static final List<TableDef> TABLE_DEFS;

    static {
        List<TableDef> list = new ArrayList<>();
        list.add(new TableDef(EXPRESS, "采购物流单-主表", "fid", "bill"));
        list.add(new TableDef(EXPRESS_TC, "采购物流单-关联追踪表", "fid", "bill"));
        list.add(new TableDef(CONTAINER_ENTRY, "物料明细-子表", "fid", "entry"));
        list.add(new TableDef(SUB_ENTRY, "箱件信息-子表", "fentryid", "custom"));
        TABLE_DEFS = Collections.unmodifiableList(list);
    }

    private ExpressSchema() {
    }

    public static List<TableDef> tableDefs() {
        return TABLE_DEFS;
    }

    /** 按表名取表定义，不存在时抛出可读提示。 */
    public static TableDef tableDef(String name) {
        for (TableDef def : TABLE_DEFS) {
            if (def.getName().equals(name)) {
                return def;
            }
        }
        throw new IllegalArgumentException("未定义的表：" + name);
    }

    /** ER 图中的 3 条关联关系。 */
    public static List<Map<String, Object>> relations() {
        List<Map<String, Object>> relations = new ArrayList<>();
        addRelation(relations, EXPRESS, "fid", EXPRESS_TC, "fid", "ZeroOne", "主表 → 关联追踪表");
        addRelation(relations, EXPRESS, "fid", CONTAINER_ENTRY, "fid", "OneN", "主表 → 物料明细（核心链路）");
        addRelation(relations, CONTAINER_ENTRY, "fentryid", SUB_ENTRY, "fentryid", "OneN", "物料明细 → 箱件信息（一对多）");
        return relations;
    }

    private static void addRelation(List<Map<String, Object>> relations, String from, String fromKey,
                                    String to, String toKey, String cardinality, String note) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("from", from);
        item.put("fromKey", fromKey);
        item.put("to", to);
        item.put("toKey", toKey);
        item.put("cardinality", cardinality);
        item.put("note", note);
        relations.add(item);
    }

    /** 字段中文标签：在车辆计划字典基础上补充采购物流单主表字段。 */
    public static Map<String, String> columnLabels() {
        Map<String, String> labels = new LinkedHashMap<>(VehiclePlanSchema.columnLabels());
        labels.put("fk_usp_carrier_name", "承运商名称");
        labels.put("fk_usp_carrierperson", "承运商联系人");
        labels.put("fk_usp_driverphone", "司机电话");
        labels.put("fk_usp_telephonefield", "联系电话");
        labels.put("fk_usp_radiooptgroupfield", "运输方式");
        labels.put("fk_usp_starting", "出发地");
        labels.put("fk_usp_destination", "目的地");
        labels.put("fk_usp_combofield2", "交货地点");
        labels.put("fk_usp_textfield", "交货港口");
        labels.put("fk_usp_ladingno", "提货单号");
        labels.put("fk_usp_transportnumber", "运输工具编号");
        labels.put("fk_usp_kilometer", "公里数");
        labels.put("fk_usp_basedatafield", "供应商");
        labels.put("fk_usp_billtypefield", "单据类型");
        labels.put("fk_usp_delivery_date", "交货日期");
        labels.put("fk_usp_etd_date", "预计离港日期");
        labels.put("fk_usp_port_collect_date", "预计港口收集日期");
        labels.put("fk_usp_tempfile_url", "临时文件路径");
        labels.put("fk_usp_declano", "报关单号");
        labels.put("fk_usp_cusdeclar", "自定义报关字段");
        labels.put("fk_usp_basedatafield1", "基础资料字段1");
        return labels;
    }
}