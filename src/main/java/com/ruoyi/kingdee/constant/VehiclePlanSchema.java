package com.ruoyi.kingdee.constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 《采购车辆计划 ER 图》中的 5 张表 / 4 条关系 / 字段中文标签。
 * scope 约定与收货单一致：bill=主表及其扩展；entry=主表明细；custom=明细的子表。
 */
public final class VehiclePlanSchema {

    /** 采购车辆计划-主表 */
    public static final String PLAN = "tk_usp_vehicleplan";
    /** 采购车辆计划-关联追踪表 */
    public static final String PLAN_TC = "tk_usp_vehicleplan_tc";
    /** 物料信息-子表 */
    public static final String CONTAINER_ENTRY = "tk_usp_container_entry";
    /** 箱件信息-子表 */
    public static final String SUB_ENTRY = "tk_usp_subentryentity";
    /** 车辆明细-子表 */
    public static final String VEHICLE_ENTRY = "tk_usp_vehicleentry";

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
        list.add(new TableDef(PLAN, "采购车辆计划-主表", "fid", "bill"));
        list.add(new TableDef(PLAN_TC, "采购车辆计划-关联追踪表", "fid", "bill"));
        list.add(new TableDef(VEHICLE_ENTRY, "车辆明细-子表", "fid", "bill"));
        list.add(new TableDef(CONTAINER_ENTRY, "物料信息-子表", "fid", "entry"));
        list.add(new TableDef(SUB_ENTRY, "箱件信息-子表", "fentryid", "custom"));
        TABLE_DEFS = Collections.unmodifiableList(list);
    }

    private VehiclePlanSchema() {
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

    /** ER 图中的 4 条关联关系。 */
    public static List<Map<String, Object>> relations() {
        List<Map<String, Object>> relations = new ArrayList<>();
        addRelation(relations, PLAN, "fid", PLAN_TC, "fid", "ZeroOne", "主表 → 关联追踪表");
        addRelation(relations, PLAN, "fid", CONTAINER_ENTRY, "fid", "OneN", "主表 → 物料信息（核心链路）");
        addRelation(relations, CONTAINER_ENTRY, "fentryid", SUB_ENTRY, "fentryid", "OneN", "物料信息 → 箱件信息（一对多）");
        addRelation(relations, PLAN, "fid", VEHICLE_ENTRY, "fid", "OneN", "主表 → 车辆明细（一对多）");
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

    /**
     * 字段中文标签：ER 图未提供中文说明，按字段语义整理常用字段，
     * 未收录的列由前端原样展示列名。
     */
    public static Map<String, String> columnLabels() {
        Map<String, String> labels = new LinkedHashMap<>();

        // 通用关联键
        labels.put("fid", "主表内码 fid");
        labels.put("fentryid", "明细行内码 fentryid");
        labels.put("fdetailid", "箱件内码 fdetailid");
        labels.put("fseq", "分录行号");
        labels.put("fpkid", "主键 fpkid");

        // 采购车辆计划-主表
        labels.put("fbillno", "单据编号");
        labels.put("fbillstatus", "单据状态");
        labels.put("fk_usp_businesstype", "业务类型");
        labels.put("fk_usp_billtype", "单据类型");
        labels.put("fk_usp_source", "来源");
        labels.put("fk_usp_destination", "目的地");
        labels.put("fk_usp_origin", "始发地");
        labels.put("fk_usp_starting", "启运地");
        labels.put("fk_usp_transportnumber", "运输单号");
        labels.put("fk_usp_delivernoticeno", "发货通知单号");
        labels.put("fk_usp_receivedept", "收货部门");
        labels.put("fk_usp_kilometer", "公里数");
        labels.put("fk_usp_kilometers", "千米数");
        labels.put("fk_usp_combofield", "组合字段");
        labels.put("fk_usp_radiooptgroupfield", "单选字段");
        labels.put("fk_usp_resubmit", "重新提交");
        labels.put("fk_usp_textfield2", "文本字段2");
        labels.put("fk_usp_textfield4", "文本字段4");
        labels.put("fcreatorid", "创建人");
        labels.put("fcreatetime", "创建时间");
        labels.put("fmodifierid", "修改人");
        labels.put("fmodifytime", "修改时间");
        labels.put("fauditorid", "审核人");
        labels.put("fauditdate", "审核时间");

        // 物料信息-子表
        labels.put("fk_usp_mainbillentity", "核心单据实体");
        labels.put("fk_usp_mainbillnumber", "核心单据编号");
        labels.put("fk_usp_mainbillid", "核心单据ID");
        labels.put("fk_usp_mainbillentryid", "核心单据分录ID");
        labels.put("fk_usp_mainbillentryseq", "核心单据分录序号");
        labels.put("fk_usp_managedepartment", "管理部门");
        labels.put("fk_usp_demandsector", "需求部门");
        labels.put("fk_usp_itemnum", "物料编码");
        labels.put("fk_usp_itemnumentryid", "物料分录ID");
        labels.put("fk_usp_material", "物料");
        labels.put("fk_usp_materialid03", "物料字段03");
        labels.put("fk_usp_materielfield", "物料字段");
        labels.put("fk_usp_devicename", "设备名称");
        labels.put("fk_usp_devicetag", "设备位号");
        labels.put("fk_usp_equipspecification", "设备规格");
        labels.put("fk_usp_supplier", "供应商");
        labels.put("fk_usp_vendorno", "供应商编号");
        labels.put("fk_usp_deliverynoticeno", "发货通知单号");
        labels.put("fk_usp_deliverynoticeid", "发货通知单ID");
        labels.put("fk_usp_repnoticenumber", "收货通知单号");
        labels.put("fk_usp_repnoticeid", "收货通知单ID");
        labels.put("fk_usp_repnoticeentryid", "收货通知单分录ID");
        labels.put("fk_usp_vehicleplan", "车辆计划");
        labels.put("fk_usp_vehicleplanqty", "车辆计划数量");
        labels.put("fk_usp_baseqty", "基本数量");
        labels.put("fk_usp_qty", "数量");
        labels.put("fk_usp_qtyfield", "数量字段");
        labels.put("fk_usp_receiveqty", "收货数量");
        labels.put("fk_usp_joinqty", "关联数量");
        labels.put("fk_usp_joinbaseqty", "关联基本数量");
        labels.put("fk_usp_cargo_value", "货值");
        labels.put("fk_usp_lot", "批号主档");
        labels.put("fk_usp_lotnumber", "批号");
        labels.put("fk_usp_warehouse", "仓库");
        labels.put("fk_usp_unit", "计量单位");
        labels.put("fk_usp_baseunitid", "基本单位");
        labels.put("fk_usp_logisticsstatus", "物流状态");
        labels.put("fk_usp_do", "是否下推");
        labels.put("fk_usp_dovehicleplan", "是否下推车辆计划");
        labels.put("fk_usp_is_selection", "是否选中");
        labels.put("fk_usp_sync_status", "同步状态");
        labels.put("fk_usp_so_no", "销售订单号");
        labels.put("fk_usp_so_out_no", "销售订单出库单号");
        labels.put("fk_usp_srcbillnumber", "来源单据编号");
        labels.put("fk_usp_srcbillentity", "来源单据实体");
        labels.put("fk_usp_srcbillid", "来源单据ID");
        labels.put("fk_usp_srcbillentryid", "来源单据分录ID");
        labels.put("fk_usp_srcbillentryseq", "来源单据分录序号");
        labels.put("fk_usp_project", "项目");
        labels.put("fk_usp_financedimension", "财务维度");
        labels.put("fk_usp_slotnumber", "仓位");
        labels.put("fk_usp_location", "库位");
        labels.put("fk_usp_container_num", "集装箱数量");
        labels.put("fk_usp_bagqty", "袋数");
        labels.put("fk_usp_suplot", "供应商批号");

        // 箱件信息-子表
        labels.put("fk_usp_sendboxno", "发货箱号");
        labels.put("fk_usp_wxno", "集装箱号");
        labels.put("fk_usp_wxnos", "集装箱号");
        labels.put("fk_usp_volume", "体积");
        labels.put("fk_usp_gross_weight", "毛重");
        labels.put("fk_usp_net_weight", "净重");
        labels.put("fk_usp_height", "高度");
        labels.put("fk_usp_width", "宽度");
        labels.put("fk_usp_length", "长度");
        labels.put("fk_usp_detailnum", "箱内数量");
        labels.put("fk_usp_detailqty", "应收数量");
        labels.put("fk_usp_associatedqty", "关联数量");
        labels.put("fk_usp_arrived", "是否到达");
        labels.put("fk_usp_issh", "是否共享");
        labels.put("fk_usp_srcsubentryid", "来源箱件内码");

        // 车辆明细-子表
        labels.put("fk_usp_carrier", "承运商");
        labels.put("fk_usp_transportsupplier", "运输供应商");
        labels.put("fk_usp_telephonefield", "联系电话");
        labels.put("fk_usp_driverpicturer", "司机照片");
        labels.put("fk_usp_outgostate", "出库状态");
        labels.put("fk_usp_outgotime", "出库时间");
        labels.put("fk_usp_incomestate", "入库状态");
        labels.put("fk_usp_incometime", "入库时间");
        labels.put("fk_usp_arrivedate", "到达日期");
        labels.put("fk_usp_modifierfield", "修改人字段");
        labels.put("fk_usp_textfield", "文本字段");
        labels.put("fk_usp_textfield1", "文本字段1");
        labels.put("fk_usp_textfield3", "文本字段3");
        return labels;
    }
}
