import { createSchemaMeta } from './shared'
import { VEHICLE_PLAN_COLUMN_LABELS, VEHICLE_PLAN_TABLE } from './vehiclePlanSchema'

/**
 * 采购物流单前端 Schema。
 * 物料明细与箱件信息与车辆计划共用表，因此字段字典直接继承车辆计划字典，只维护本模块差异字段。
 */

const EXPRESS = 'tk_usp_express'
const EXPRESS_TC = 'tk_usp_express_tc'

const TABLES = [
  { name: EXPRESS, label: '采购物流单-主表', joinKey: 'fid', scope: 'bill' },
  { name: EXPRESS_TC, label: '采购物流单-关联追踪表', joinKey: 'fid', scope: 'bill' },
  { name: VEHICLE_PLAN_TABLE.CONTAINER_ENTRY, label: '物料明细-子表', joinKey: 'fid', scope: 'entry' },
  { name: VEHICLE_PLAN_TABLE.SUB_ENTRY, label: '箱件信息-子表', joinKey: 'fentryid', scope: 'custom' }
]

const RELATIONS = [
  { from: EXPRESS, fromKey: 'fid', to: EXPRESS_TC, toKey: 'fid', cardinality: 'ZeroOne', note: '主表 → 关联追踪表' },
  { from: EXPRESS, fromKey: 'fid', to: VEHICLE_PLAN_TABLE.CONTAINER_ENTRY, toKey: 'fid', cardinality: 'OneN', note: '主表 → 物料明细（核心链路）' },
  { from: VEHICLE_PLAN_TABLE.CONTAINER_ENTRY, fromKey: 'fentryid', to: VEHICLE_PLAN_TABLE.SUB_ENTRY, toKey: 'fentryid', cardinality: 'OneN', note: '物料明细 → 箱件信息（一对多）' }
]

const COLUMN_LABELS = {
  ...VEHICLE_PLAN_COLUMN_LABELS,

  // 采购物流单主表差异字段
  fk_usp_carrier_name: '承运商名称',
  fk_usp_carrierperson: '承运商联系人',
  fk_usp_driverphone: '司机电话',
  fk_usp_telephonefield: '联系电话',
  fk_usp_radiooptgroupfield: '运输方式',
  fk_usp_starting: '出发地',
  fk_usp_destination: '目的地',
  fk_usp_combofield2: '交货地点',
  fk_usp_textfield: '交货港口',
  fk_usp_ladingno: '提货单号',
  fk_usp_transportnumber: '运输工具编号',
  fk_usp_kilometer: '公里数',
  fk_usp_basedatafield: '供应商',
  fk_usp_billtypefield: '单据类型',
  fk_usp_delivery_date: '交货日期',
  fk_usp_etd_date: '预计离港日期',
  fk_usp_port_collect_date: '预计港口收集日期',
  fk_usp_tempfile_url: '临时文件路径',
  fk_usp_declano: '报关单号',
  fk_usp_cusdeclar: '自定义报关字段',
  fk_usp_basedatafield1: '基础资料字段1'
}

export function createExpressSchema() {
  return createSchemaMeta({
    tables: TABLES,
    relations: RELATIONS,
    columnLabels: COLUMN_LABELS
  })
}
