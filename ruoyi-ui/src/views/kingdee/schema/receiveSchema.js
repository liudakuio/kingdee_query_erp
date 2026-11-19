import { COMMON_FIELD_LABELS, createSchemaMeta } from './shared'

/**
 * 采购收货单前端 Schema。
 * 维护方式：新增字段直接往 COLUMN_LABELS 追加 key: '中文标签'；未配置的列会自动原样展示字段名。
 */

const BILL = 't_im_purrecbill'
const BILL_L = 't_im_purrecbill_l'
const BILL_LK = 't_im_purrecbill_lk'
const BILL_TC = 't_im_purrecbill_tc'
const BILL_WB = 't_im_purrecbill_wb'
const BILL_F = 't_im_purrecbill_f'
const ENTRY = 't_im_purrecbillentry'
const ENTRY_L = 't_im_purrecbillentry_l'
const ENTRY_R = 't_im_purrecbillentry_r'
const ENTRY_LK = 't_im_purrecbillentry_lk'
const BOX = 'tk_usp_receivingboxentry'
const ATTACH = 'tk_usp_cgshentryattach'
const ATTACH_FILE = 'tk_usp_cgentryattachment'

const TABLES = [
  { name: BILL, label: '采购收货单主表', joinKey: 'fid', scope: 'bill' },
  { name: BILL_L, label: '采购收货单多语言表', joinKey: 'fid', scope: 'bill' },
  { name: BILL_LK, label: '采购收货单关联子实体表', joinKey: 'fid', scope: 'bill' },
  { name: BILL_TC, label: '采购收货单关联追踪表', joinKey: 'fid', scope: 'bill' },
  { name: BILL_WB, label: '采购收货单反写记录表', joinKey: 'fid', scope: 'bill' },
  { name: BILL_F, label: '采购收货单分表', joinKey: 'fid', scope: 'bill' },
  { name: ENTRY, label: '采购收货单物料明细表', joinKey: 'fid', scope: 'entry' },
  { name: ENTRY_L, label: '物料明细多语言表', joinKey: 'fentryid', scope: 'entry' },
  { name: ENTRY_R, label: '物料明细分表', joinKey: 'fentryid', scope: 'entry' },
  { name: ENTRY_LK, label: '物料明细关联子实体表', joinKey: 'fentryid', scope: 'entry' },
  { name: BOX, label: '收货箱明细子单据体', joinKey: 'fentryid', scope: 'custom' },
  { name: ATTACH, label: '收货明细附件清单子单据体', joinKey: 'fentryid', scope: 'custom' },
  { name: ATTACH_FILE, label: '收货明细附件表', joinKey: 'fdetailid', scope: 'custom' }
]

const RELATIONS = [
  { from: BILL, fromKey: 'fid', to: BILL_L, toKey: 'fid', cardinality: 'One', note: '主表 → 多语言表' },
  { from: BILL, fromKey: 'fid', to: BILL_LK, toKey: 'fid', cardinality: 'ZeroOne', note: '主表 → 关联子实体表' },
  { from: BILL, fromKey: 'fid', to: BILL_TC, toKey: 'fid', cardinality: 'ZeroOne', note: '主表 → 关联追踪表' },
  { from: BILL, fromKey: 'fid', to: BILL_WB, toKey: 'fid', cardinality: 'ZeroOne', note: '主表 → 反写记录表' },
  { from: BILL, fromKey: 'fid', to: BILL_F, toKey: 'fid', cardinality: 'ZeroOne', note: '主表 → 分表' },
  { from: BILL, fromKey: 'fid', to: ENTRY, toKey: 'fid', cardinality: 'OneN', note: '主表 → 物料明细（核心链路）' },
  { from: ENTRY, fromKey: 'fentryid', to: ENTRY_L, toKey: 'fentryid', cardinality: 'One', note: '明细 → 明细多语言表' },
  { from: ENTRY, fromKey: 'fentryid', to: ENTRY_R, toKey: 'fentryid', cardinality: 'ZeroOne', note: '明细 → 明细分表' },
  { from: ENTRY, fromKey: 'fentryid', to: ENTRY_LK, toKey: 'fentryid', cardinality: 'ZeroOne', note: '明细 → 明细关联子实体表' },
  { from: ENTRY, fromKey: 'fentryid', to: BOX, toKey: 'fentryid', cardinality: 'OneN', note: '明细 → 收货箱明细（一对多）' },
  { from: ENTRY, fromKey: 'fentryid', to: ATTACH, toKey: 'fentryid', cardinality: 'OneN', note: '明细 → 附件清单（一对多）' },
  { from: ATTACH, fromKey: 'fdetailid', to: ATTACH_FILE, toKey: 'fdetailid', cardinality: 'ZeroOne', note: '附件清单 → 附件表' }
]

const COLUMN_LABELS = {
  ...COMMON_FIELD_LABELS,

  // 通用关联键（含义与公共字典不同的字段在这里覆盖）
  fdetailid: '附件明细内码 fdetailid',
  fsbillid: '源单内码',
  fstableid: '源单主实体编码',
  fsid: '源单主实体内码',
  fcomment: '备注',
  flocaleid: '语言',

  // 主表 t_im_purrecbill（精简白名单）
  fbiztime: '业务日期',
  fbilltypeid: '单据类型',
  fsupplierid: '供货供应商',
  forgid: '收货组织',
  facceptancestatus: '验收状态',

  // 明细表 t_im_purrecbillentry（精简白名单）
  fmaterialid: '物料编码',
  fmaterialname: '物料名称(封存)',
  fqty: '数量',
  fbaseqty: '基本数量',
  fprice: '单价',
  fpriceandtax: '含税单价',
  famount: '金额',
  famountandtax: '价税合计',
  fcuramount: '金额(本位币)',
  ftaxamount: '税额',
  ftaxrate: '税率%',
  fwarehouseid: '仓库',
  flotnumber: '批号',
  fk_usp_textfield1: '供应商发货箱号',
  fk_usp_deliverybill: '收货通知单号',
  fk_usp_inspect_doc_no: '检验单据编号',
  fentrycomment: '明细备注',

  // 主表扩展表
  fprovideraddress: '供货地址',
  foperate: '反写操作',
  fruleverid: '反写规则版本',
  fruleitemid: '反写规则项',
  fwritevalue: '反写值',
  ftbillid: '目标单内码',
  fttableid: '目标单实体编码',
  ftid: '目标单分录内码',
  fexchangerate: '汇率',
  fistax: '含税',
  fpaymode: '付款方式',
  fexratetable: '汇率表',
  finvoicebiztypeid: '发票类别',
  fpayconditionid: '付款条件',
  fexratedate: '汇率日期',
  fcurrencyid: '本位币',
  fsettlecurrencyid: '结算币',
  fsettletypeid: '结算方式',

  // 明细关联子实体表
  fbaseqty_old: '基本数量_原始携带值',
  fk_usp_qtyfield2: '不合格数（加）',
  fk_usp_qtyfield2_old: '不合格数（加）_原始值',
  fk_usp_baseunitqtyfield2: '基本数量扩展字段2',
  fk_usp_baseunitqtyfield2_old: '基本数量扩展字段2_原始值',

  // 明细分表 t_im_purrecbillentry_r
  fconcessionqty: '让步接收数量',
  fsrcsystem: '来源系统',
  flogisticsbill: '物流单据',
  finvbaseqty: '已入库基本数量',
  fmainbillentity: '核心单据实体',
  funqualifiedunit3rd: '不合格数量(第三单位)',
  funverifybaseqty: '未核销基本数量',
  ftotalreturnqty: '合格已退货数量',
  fqualifiedunit2nd: '合格数量(主辅单位)',
  fremaininvqty: '未入库数量',
  fsrcbillentryseq: '来源单据分录序号',
  fconbillrownum: '合同行号',
  fjoinpriceqty: '应付数量',
  fremainpurqty: '未转固数量',
  fconbillnumber: '合同编号',
  fmainbillid: '核心单据ID',
  facceptanceentrystatus: '验收状态',
  fsrcsysbillno: '来源系统单据编号',
  fsrcbillnumber: '来源单据编号',
  funqualifiedqty: '不合格数量',
  fsrcbillid: '来源单据ID',
  finvqty: '已入库数量',
  fmainbillnumber: '核心单据编号',
  fmversion: '物料版本',
  fverifybaseqty: '已核销基本数量',
  fsrcsysbillentryid: '来源系统单据分录ID',
  fremainjoinpricebaseqty: '剩余应付基本数量',
  fremainjoinpriceqty: '剩余应付数量',
  funverifyqty: '未核销数量',
  fpurbillentryid: '采购订单行号',
  fconbillid: '合同ID',
  fmainbillentryseq: '核心单据分录序号',
  funqualifiedunit2nd: '不合格数量(主辅单位)',
  fconbillentity: '合同实体',
  fconbillentryid: '合同行ID',
  fjoinpricebaseqty: '应付基本数量',
  fmftorderid: '委外工单ID',
  fconcessionbaseqty: '让步接收基本数量',
  ftotalreturnbaseqty: '合格已退货基本数量',
  fqualifiedunit3rd: '合格数量(第三单位)',
  fremainreturnbaseqty: '合格未退货基本数量',
  fpurbillnumber: '采购订单号',
  fverifyqty: '已核销数量',
  fqualifiedqty: '合格数量',
  fmftorderentryseq: '委外工单分录序号',
  fsrcbillentity: '来源单据实体',
  fpurchasedqty: '已转固数量',
  fqualifiedbaseqty: '合格基本数量',
  fmftorderentryid: '委外工单行ID',
  funqualifiedbaseqty: '不合格基本数量',
  fremaininvbaseqty: '未入库基本数量',
  fsrcbillentryid: '来源单据行ID',
  fpurorderbillnumber: '采购订单号',
  fmainbillentryid: '核心单据行ID',
  facceptancedate: '验收日期',
  fpurchasedamount: '已转固价税合计(本位币)',
  fremainpuramount: '未转固价税合计(本位币)',
  fremainreturnqty: '合格未退货数量',
  fsrcsysbillid: '来源系统单据ID',
  fmftordernumber: '委外工单编号',

  // 自定义子单据体 / 附件
  fk_usp_sendboxno: '发货箱件/袋号',
  fk_usp_srcsubentryid: '源单ID',
  fk_usp_realqty: '实收数量',
  fk_usp_is_special: '放行',
  fk_usp_fileid: '文件/文件夹ID',
  fk_usp_download_link: '下载链接',
  fk_usp_checkboxfield: '是否必传',
  fk_usp_isfolder: '是否文件夹',
  fk_usp_isupload: '是否上传',
  fk_usp_attachname: '附件名称',
  fk_usp_pre_link: '预览链接',
  fbasedataid: '基础资料ID'
}

export function createReceiveSchema() {
  return createSchemaMeta({
    tables: TABLES,
    relations: RELATIONS,
    columnLabels: COLUMN_LABELS
  })
}
