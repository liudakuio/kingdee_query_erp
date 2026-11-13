import { request } from '@/utils/request'

// 车辆计划元数据：表字典、关联关系、字段中文标签
export function getVehiclePlanMeta() {
  return request({
    url: '/kingdee/vehicleplan/meta',
    method: 'get'
  })
}

// 主查询：按单据编号（或主表内码 fid）返回 5 张表的关联数据
export function queryVehiclePlan(params) {
  return request({
    url: '/kingdee/vehicleplan/query',
    method: 'get',
    params
  })
}

// 批量查询：billNos 以中英文逗号分隔，返回多个单据的结果与数量汇总
export function queryVehiclePlanBatch(params) {
  return request({
    url: '/kingdee/vehicleplan/queryBatch',
    method: 'get',
    params
  })
}

// 导出 Excel：table 传 all 导出全部表，传表名只导出该表
export function exportVehiclePlanExcel(params) {
  return request({
    url: '/kingdee/vehicleplan/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 批量导出 Excel：每个单据的数据追加到同一张表，末尾标注来源单据编号
export function exportVehiclePlanBatchExcel(params) {
  return request({
    url: '/kingdee/vehicleplan/exportBatch',
    method: 'get',
    params,
    responseType: 'blob'
  })
}