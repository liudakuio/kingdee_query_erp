package com.ruoyi.kingdee.constant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 《金蝶采购收货单数据结构与关联关系》中的 13 张表 / 12 条关系 / 已确认字段字典。
 * 只描述关联键，不推测未知业务字段；已确认字段由 domain 下各 entity 承载。
 */
public final class PurReceiveSchema {

    public static final String BILL = "t_im_purrecbill";
    public static final String BILL_L = "t_im_purrecbill_l";
    public static final String BILL_LK = "t_im_purrecbill_lk";
    public static final String BILL_TC = "t_im_purrecbill_tc";
    public static final String BILL_WB = "t_im_purrecbill_wb";
    public static final String BILL_F = "t_im_purrecbill_f";
    public static final String ENTRY = "t_im_purrecbillentry";
    public static final String ENTRY_L = "t_im_purrecbillentry_l";
    public static final String ENTRY_R = "t_im_purrecbillentry_r";
    public static final String ENTRY_LK = "t_im_purrecbillentry_lk";
    public static final String BOX = "tk_usp_receivingboxentry";
    public static final String ATTACH = "tk_usp_cgshentryattach";
    public static final String ATTACH_FILE = "tk_usp_cgentryattachment";

    /** bill=主表及其扩展；entry=明细及其扩展；custom=自定义子单据体/附件表 */
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
        list.add(new TableDef(BILL, "采购收货单主表", "fid", "bill"));
        list.add(new TableDef(BILL_L, "采购收货单多语言表", "fid", "bill"));
        list.add(new TableDef(BILL_LK, "采购收货单关联子实体表", "fid", "bill"));
        list.add(new TableDef(BILL_TC, "采购收货单关联追踪表", "fid", "bill"));
        list.add(new TableDef(BILL_WB, "采购收货单反写记录表", "fid", "bill"));
        list.add(new TableDef(BILL_F, "采购收货单分表", "fid", "bill"));
        list.add(new TableDef(ENTRY, "采购收货单物料明细表", "fid", "entry"));
        list.add(new TableDef(ENTRY_L, "物料明细多语言表", "fentryid", "entry"));
        list.add(new TableDef(ENTRY_R, "物料明细分表", "fentryid", "entry"));
        list.add(new TableDef(ENTRY_LK, "物料明细关联子实体表", "fentryid", "entry"));
        list.add(new TableDef(BOX, "收货箱明细子单据体", "fentryid", "custom"));
        list.add(new TableDef(ATTACH, "收货明细附件清单子单据体", "fentryid", "custom"));
        list.add(new TableDef(ATTACH_FILE, "收货明细附件表", "fdetailid", "custom"));
        TABLE_DEFS = Collections.unmodifiableList(list);
    }

    private PurReceiveSchema() {
    }

    public static List<TableDef> tableDefs() {
        return TABLE_DEFS;
    }

    /** 按表名取表定义，供 Service 生成各表的渲染分区。 */
    public static TableDef tableDef(String name) {
        for (TableDef def : TABLE_DEFS) {
            if (def.getName().equals(name)) {
                return def;
            }
        }
        throw new IllegalArgumentException("未定义的表：" + name);
    }

    /** 文档第 3 节的 12 条关联关系。 */
    public static List<Map<String, Object>> relations() {
        List<Map<String, Object>> relations = new ArrayList<>();
        addRelation(relations, BILL, "fid", BILL_L, "fid", "One", "主表 → 多语言表");
        addRelation(relations, BILL, "fid", BILL_LK, "fid", "ZeroOne", "主表 → 关联子实体表");
        addRelation(relations, BILL, "fid", BILL_TC, "fid", "ZeroOne", "主表 → 关联追踪表");
        addRelation(relations, BILL, "fid", BILL_WB, "fid", "ZeroOne", "主表 → 反写记录表");
        addRelation(relations, BILL, "fid", BILL_F, "fid", "ZeroOne", "主表 → 分表");
        addRelation(relations, BILL, "fid", ENTRY, "fid", "OneN", "主表 → 物料明细（核心链路）");
        addRelation(relations, ENTRY, "fentryid", ENTRY_L, "fentryid", "One", "明细 → 明细多语言表");
        addRelation(relations, ENTRY, "fentryid", ENTRY_R, "fentryid", "ZeroOne", "明细 → 明细分表");
        addRelation(relations, ENTRY, "fentryid", ENTRY_LK, "fentryid", "ZeroOne", "明细 → 明细关联子实体表");
        addRelation(relations, ENTRY, "fentryid", BOX, "fentryid", "OneN", "明细 → 收货箱明细（一对多）");
        addRelation(relations, ENTRY, "fentryid", ATTACH, "fentryid", "OneN", "明细 → 附件清单（一对多）");
        addRelation(relations, ATTACH, "fdetailid", ATTACH_FILE, "fdetailid", "ZeroOne", "附件清单 → 附件表");
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

    /** 文档中已确认的字段，以及演示库补充字段；未列出的字段原样展示列名。 */
    public static Map<String, String> columnLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("fid", "主表内码 fid");
        labels.put("fbillno", "单据编号");
        labels.put("fentryid", "明细行内码");
        labels.put("fseq", "行序号");
        labels.put("fdetailid", "附件明细内码");
        labels.put("fpkid", "主键 fpkid");
        labels.put("fmaterialid", "物料编码/内码");
        labels.put("fmaterialname", "物料名称");
        labels.put("fqty", "数量");
        labels.put("fwarehouseid", "仓库内码");
        labels.put("fk_usp_deliverybill", "收货通知单号");
        labels.put("fk_usp_inspect_doc_no", "检验单据编号");
        labels.put("fbizdate", "业务日期");
        labels.put("fstatus", "单据状态");
        labels.put("fsuppliername", "供应商");
        labels.put("fcreatorname", "制单人");
        labels.put("fcreatetime", "创建时间");
        labels.put("fremark", "备注");
        labels.put("fsourcebillno", "来源单据编号");
        labels.put("fsourcebilltype", "来源单据类型");
        labels.put("ftrackno", "追踪号");
        labels.put("ftrackstatus", "追踪状态");
        labels.put("fsrcbillno", "反写来源单号");
        labels.put("fwritebackqty", "反写数量");
        labels.put("fwritebacktime", "反写时间");
        labels.put("fbaseqty", "基本数量");
        labels.put("fprice", "单价");
        labels.put("famount", "金额");
        labels.put("ftaxrate", "税率");
        labels.put("fsourceentryid", "来源明细内码");
        labels.put("flinkqty", "关联数量");
        labels.put("fboxno", "箱号");
        labels.put("fboxqty", "箱内数量");
        labels.put("fbatchno", "批次号");
        labels.put("fboxstatus", "箱状态");
        labels.put("fattachname", "附件名称");
        labels.put("fattachtype", "附件类型");
        labels.put("frequired", "是否必传");
        labels.put("fcheckresult", "检查结果");
        labels.put("ffilename", "文件名");
        labels.put("ffileurl", "文件路径");
        labels.put("ffilesize", "文件大小(字节)");
        labels.put("fuploadtime", "上传时间");
        labels.put("flocale", "语言");
        labels.put("fname", "名称");
        return labels;
    }
}
