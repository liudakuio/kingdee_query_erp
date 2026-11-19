import { COMMON_FIELD_LABELS, createSchemaMeta } from './shared'

/**
 * 采购车辆计划前端 Schema。
 * 维护方式：新增字段直接往 COLUMN_LABELS 追加 key: '中文标签'；未配置的列会自动原样展示字段名。
 */

export const VEHICLE_PLAN_TABLE = {
  PLAN: 'tk_usp_vehicleplan',
  PLAN_TC: 'tk_usp_vehicleplan_tc',
  CONTAINER_ENTRY: 'tk_usp_container_entry',
  SUB_ENTRY: 'tk_usp_subentryentity',
  VEHICLE_ENTRY: 'tk_usp_vehicleentry'
}

const TABLES = [
  { name: VEHICLE_PLAN_TABLE.PLAN, label: '采购车辆计划-主表', joinKey: 'fid', scope: 'bill' },
  { name: VEHICLE_PLAN_TABLE.PLAN_TC, label: '采购车辆计划-关联追踪表', joinKey: 'fid', scope: 'bill' },
  { name: VEHICLE_PLAN_TABLE.VEHICLE_ENTRY, label: '车辆明细-子表', joinKey: 'fid', scope: 'bill' },
  { name: VEHICLE_PLAN_TABLE.CONTAINER_ENTRY, label: '物料信息-子表', joinKey: 'fid', scope: 'entry' },
  { name: VEHICLE_PLAN_TABLE.SUB_ENTRY, label: '箱件信息-子表', joinKey: 'fentryid', scope: 'custom' }
]

const RELATIONS = [
  { from: VEHICLE_PLAN_TABLE.PLAN, fromKey: 'fid', to: VEHICLE_PLAN_TABLE.PLAN_TC, toKey: 'fid', cardinality: 'ZeroOne', note: '主表 → 关联追踪表' },
  { from: VEHICLE_PLAN_TABLE.PLAN, fromKey: 'fid', to: VEHICLE_PLAN_TABLE.CONTAINER_ENTRY, toKey: 'fid', cardinality: 'OneN', note: '主表 → 物料信息（核心链路）' },
  { from: VEHICLE_PLAN_TABLE.CONTAINER_ENTRY, fromKey: 'fentryid', to: VEHICLE_PLAN_TABLE.SUB_ENTRY, toKey: 'fentryid', cardinality: 'OneN', note: '物料信息 → 箱件信息（一对多）' },
  { from: VEHICLE_PLAN_TABLE.PLAN, fromKey: 'fid', to: VEHICLE_PLAN_TABLE.VEHICLE_ENTRY, toKey: 'fid', cardinality: 'OneN', note: '主表 → 车辆明细（一对多）' }
]

export const VEHICLE_PLAN_COLUMN_LABELS = {
  ...COMMON_FIELD_LABELS,

  // 通用关联键（含义与公共字典不同的字段在这里覆盖）
  fdetailid: '箱件内码 fdetailid',

  // 采购车辆计划-主表
  fk_usp_businesstype: '业务类型',
  fk_usp_billtype: '单据类型',
  fk_usp_source: '来源',
  fk_usp_destination: '目的地',
  fk_usp_origin: '始发地',
  fk_usp_starting: '启运地',
  fk_usp_transportnumber: '运输单号',
  fk_usp_delivernoticeno: '发货通知单号',
  fk_usp_receivedept: '收货部门',
  fk_usp_kilometer: '公里数',
  fk_usp_kilometers: '千米数',
  fk_usp_combofield: '组合字段',
  fk_usp_radiooptgroupfield: '单选字段',
  fk_usp_resubmit: '重新提交',
  fk_usp_textfield2: '文本字段2',
  fk_usp_textfield4: '文本字段4',

  // 物料信息-子表
  fk_usp_mainbillentity: '核心单据实体',
  fk_usp_mainbillnumber: '核心单据编号',
  fk_usp_mainbillid: '核心单据ID',
  fk_usp_mainbillentryid: '核心单据分录ID',
  fk_usp_mainbillentryseq: '核心单据分录序号',
  fk_usp_managedepartment: '管理部门',
  fk_usp_demandsector: '需求部门',
  fk_usp_itemnum: '物料编码',
  fk_usp_itemnumentryid: '物料分录ID',
  fk_usp_material: '物料',
  fk_usp_materialid03: '物料字段03',
  fk_usp_materielfield: '物料字段',
  fk_usp_devicename: '设备名称',
  fk_usp_devicetag: '设备位号',
  fk_usp_equipspecification: '设备规格',
  fk_usp_supplier: '供应商',
  fk_usp_vendorno: '供应商编号',
  fk_usp_deliverynoticeno: '发货通知单号',
  fk_usp_deliverynoticeid: '发货通知单ID',
  fk_usp_repnoticenumber: '收货通知单号',
  fk_usp_repnoticeid: '收货通知单ID',
  fk_usp_repnoticeentryid: '收货通知单分录ID',
  fk_usp_vehicleplan: '车辆计划',
  fk_usp_vehicleplanqty: '车辆计划数量',
  fk_usp_baseqty: '基本数量',
  fk_usp_qty: '数量',
  fk_usp_qtyfield: '数量字段',
  fk_usp_receiveqty: '收货数量',
  fk_usp_joinqty: '关联数量',
  fk_usp_joinbaseqty: '关联基本数量',
  fk_usp_cargo_value: '货值',
  fk_usp_lot: '批号主档',
  fk_usp_lotnumber: '批号',
  fk_usp_warehouse: '仓库',
  fk_usp_unit: '计量单位',
  fk_usp_baseunitid: '基本单位',
  fk_usp_logisticsstatus: '物流状态',
  fk_usp_do: '是否下推',
  fk_usp_dovehicleplan: '是否下推车辆计划',
  fk_usp_is_selection: '是否选中',
  fk_usp_sync_status: '同步状态',
  fk_usp_so_no: '销售订单号',
  fk_usp_so_out_no: '销售订单出库单号',
  fk_usp_srcbillnumber: '来源单据编号',
  fk_usp_srcbillentity: '来源单据实体',
  fk_usp_srcbillid: '来源单据ID',
  fk_usp_srcbillentryid: '来源单据分录ID',
  fk_usp_srcbillentryseq: '来源单据分录序号',
  fk_usp_project: '项目',
  fk_usp_financedimension: '财务维度',
  fk_usp_slotnumber: '仓位',
  fk_usp_location: '库位',
  fk_usp_container_num: '集装箱数量',
  fk_usp_bagqty: '袋数',
  fk_usp_suplot: '供应商批号',

  // 箱件信息-子表
  fk_usp_sendboxno: '发货箱号',
  fk_usp_wxnos: '集装箱号',
  fk_usp_volume: '体积',
  fk_usp_gross_weight: '毛重',
  fk_usp_net_weight: '净重',
  fk_usp_height: '高度',
  fk_usp_width: '宽度',
  fk_usp_length: '长度',
  fk_usp_detailnum: '箱内数量',
  fk_usp_associatedqty: '关联数量',
  fk_usp_issh: '是否共享',
  fk_usp_srcsubentryid: '来源箱件内码',

  // 车辆明细-子表
  fk_usp_carrier: '承运商',
  fk_usp_transportsupplier: '运输供应商',
  fk_usp_telephonefield: '联系电话',
  fk_usp_driverpicturer: '司机照片',
  fk_usp_outgostate: '出库状态',
  fk_usp_outgotime: '出库时间',
  fk_usp_incomestate: '入库状态',
  fk_usp_incometime: '入库时间',
  fk_usp_arrivedate: '到达日期',
  fk_usp_modifierfield: '修改人字段',
  fk_usp_textfield: '文本字段',
  fk_usp_textfield1: '文本字段1',
  fk_usp_textfield3: '文本字段3'
}

export function createVehiclePlanSchema() {
  return createSchemaMeta({
    tables: TABLES,
    relations: RELATIONS,
    columnLabels: VEHICLE_PLAN_COLUMN_LABELS
  })
}
