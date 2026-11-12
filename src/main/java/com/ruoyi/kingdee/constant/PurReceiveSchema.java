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

    /**
     * 字段中文标签，取自《金蝶采购收货单数据库 ER 图》的字段说明。
     * 未收录的列名由前端原样展示，不影响列的全量显示。
     */
    public static Map<String, String> columnLabels() {
        Map<String, String> labels = new LinkedHashMap<>();

        // 通用关联键
        labels.put("fid", "主表内码 fid");
        labels.put("fentryid", "明细行内码 fentryid");
        labels.put("fdetailid", "附件明细内码 fdetailid");
        labels.put("fpkid", "主键 fpkid");
        labels.put("fseq", "分录行号");
        labels.put("fsbillid", "源单内码");
        labels.put("fstableid", "源单主实体编码");
        labels.put("fsid", "源单主实体内码");
        labels.put("fcomment", "备注");
        labels.put("flocaleid", "语言");

        // 主表 t_im_purrecbill（精简白名单）
        labels.put("fbillno", "单据编号");
        labels.put("fbillstatus", "单据状态");
        labels.put("fbiztime", "业务日期");
        labels.put("fcreatorid", "创建人");
        labels.put("fcreatetime", "创建时间");
        labels.put("fmodifierid", "修改人");
        labels.put("fmodifytime", "修改时间");
        labels.put("fauditorid", "审核人");
        labels.put("fauditdate", "审核时间");
        labels.put("fbilltypeid", "单据类型");
        labels.put("fsupplierid", "供货供应商");
        labels.put("forgid", "收货组织");
        labels.put("facceptancestatus", "验收状态");

        // 明细表 t_im_purrecbillentry（精简白名单）
        labels.put("fmaterialid", "物料编码");
        labels.put("fmaterialname", "物料名称(封存)");
        labels.put("fqty", "数量");
        labels.put("fbaseqty", "基本数量");
        labels.put("fprice", "单价");
        labels.put("fpriceandtax", "含税单价");
        labels.put("famount", "金额");
        labels.put("famountandtax", "价税合计");
        labels.put("fcuramount", "金额(本位币)");
        labels.put("ftaxamount", "税额");
        labels.put("ftaxrate", "税率%");
        labels.put("fwarehouseid", "仓库");
        labels.put("flotnumber", "批号");
        labels.put("fk_usp_textfield1", "供应商发货箱号");
        labels.put("fk_usp_deliverybill", "收货通知单号");
        labels.put("fk_usp_inspect_doc_no", "检验单据编号");
        labels.put("fentrycomment", "明细备注");

        // 主表扩展表
        labels.put("fprovideraddress", "供货地址");
        labels.put("foperate", "反写操作");
        labels.put("fruleverid", "反写规则版本");
        labels.put("fruleitemid", "反写规则项");
        labels.put("fwritevalue", "反写值");
        labels.put("ftbillid", "目标单内码");
        labels.put("fttableid", "目标单实体编码");
        labels.put("ftid", "目标单分录内码");
        labels.put("fexchangerate", "汇率");
        labels.put("fistax", "含税");
        labels.put("fpaymode", "付款方式");
        labels.put("fexratetable", "汇率表");
        labels.put("finvoicebiztypeid", "发票类别");
        labels.put("fpayconditionid", "付款条件");
        labels.put("fexratedate", "汇率日期");
        labels.put("fcurrencyid", "本位币");
        labels.put("fsettlecurrencyid", "结算币");
        labels.put("fsettletypeid", "结算方式");

        // 明细关联子实体表
        labels.put("fbaseqty_old", "基本数量_原始携带值");
        labels.put("fk_usp_qtyfield2", "不合格数（加）");
        labels.put("fk_usp_qtyfield2_old", "不合格数（加）_原始值");
        labels.put("fk_usp_baseunitqtyfield2", "基本数量扩展字段2");
        labels.put("fk_usp_baseunitqtyfield2_old", "基本数量扩展字段2_原始值");

        // 明细分表 t_im_purrecbillentry_r
        labels.put("fconcessionqty", "让步接收数量");
        labels.put("fsrcsystem", "来源系统");
        labels.put("flogisticsbill", "物流单据");
        labels.put("finvbaseqty", "已入库基本数量");
        labels.put("fmainbillentity", "核心单据实体");
        labels.put("funqualifiedunit3rd", "不合格数量(第三单位)");
        labels.put("funverifybaseqty", "未核销基本数量");
        labels.put("ftotalreturnqty", "合格已退货数量");
        labels.put("fqualifiedunit2nd", "合格数量(主辅单位)");
        labels.put("fremaininvqty", "未入库数量");
        labels.put("fsrcbillentryseq", "来源单据分录序号");
        labels.put("fconbillrownum", "合同行号");
        labels.put("fjoinpriceqty", "应付数量");
        labels.put("fremainpurqty", "未转固数量");
        labels.put("fconbillnumber", "合同编号");
        labels.put("fmainbillid", "核心单据ID");
        labels.put("facceptanceentrystatus", "验收状态");
        labels.put("fsrcsysbillno", "来源系统单据编号");
        labels.put("fsrcbillnumber", "来源单据编号");
        labels.put("funqualifiedqty", "不合格数量");
        labels.put("fsrcbillid", "来源单据ID");
        labels.put("finvqty", "已入库数量");
        labels.put("fmainbillnumber", "核心单据编号");
        labels.put("fmversion", "物料版本");
        labels.put("fverifybaseqty", "已核销基本数量");
        labels.put("fsrcsysbillentryid", "来源系统单据分录ID");
        labels.put("fremainjoinpricebaseqty", "剩余应付基本数量");
        labels.put("fremainjoinpriceqty", "剩余应付数量");
        labels.put("funverifyqty", "未核销数量");
        labels.put("fpurbillentryid", "采购订单行号");
        labels.put("fconbillid", "合同ID");
        labels.put("fmainbillentryseq", "核心单据分录序号");
        labels.put("funqualifiedunit2nd", "不合格数量(主辅单位)");
        labels.put("fconbillentity", "合同实体");
        labels.put("fconbillentryid", "合同行ID");
        labels.put("fjoinpricebaseqty", "应付基本数量");
        labels.put("fmftorderid", "委外工单ID");
        labels.put("fconcessionbaseqty", "让步接收基本数量");
        labels.put("ftotalreturnbaseqty", "合格已退货基本数量");
        labels.put("fqualifiedunit3rd", "合格数量(第三单位)");
        labels.put("fremainreturnbaseqty", "合格未退货基本数量");
        labels.put("fpurbillnumber", "采购订单号");
        labels.put("fverifyqty", "已核销数量");
        labels.put("fqualifiedqty", "合格数量");
        labels.put("fmftorderentryseq", "委外工单分录序号");
        labels.put("fsrcbillentity", "来源单据实体");
        labels.put("fpurchasedqty", "已转固数量");
        labels.put("fqualifiedbaseqty", "合格基本数量");
        labels.put("fmftorderentryid", "委外工单行ID");
        labels.put("funqualifiedbaseqty", "不合格基本数量");
        labels.put("fremaininvbaseqty", "未入库基本数量");
        labels.put("fsrcbillentryid", "来源单据行ID");
        labels.put("fpurorderbillnumber", "采购订单号");
        labels.put("fmainbillentryid", "核心单据行ID");
        labels.put("facceptancedate", "验收日期");
        labels.put("fpurchasedamount", "已转固价税合计(本位币)");
        labels.put("fremainpuramount", "未转固价税合计(本位币)");
        labels.put("fremainreturnqty", "合格未退货数量");
        labels.put("fsrcsysbillid", "来源系统单据ID");
        labels.put("fmftordernumber", "委外工单编号");

        // 自定义子单据体 / 附件
        labels.put("fk_usp_sendboxno", "发货箱件/袋号");
        labels.put("fk_usp_wxno", "集装箱号");
        labels.put("fk_usp_srcsubentryid", "源单ID");
        labels.put("fk_usp_arrived", "是否到达");
        labels.put("fk_usp_realqty", "实收数量");
        labels.put("fk_usp_detailqty", "应收数量");
        labels.put("fk_usp_is_special", "放行");
        labels.put("fk_usp_fileid", "文件/文件夹ID");
        labels.put("fk_usp_download_link", "下载链接");
        labels.put("fk_usp_checkboxfield", "是否必传");
        labels.put("fk_usp_isfolder", "是否文件夹");
        labels.put("fk_usp_isupload", "是否上传");
        labels.put("fk_usp_attachname", "附件名称");
        labels.put("fk_usp_pre_link", "预览链接");
        labels.put("fbasedataid", "基础资料ID");
        return labels;
    }
}
