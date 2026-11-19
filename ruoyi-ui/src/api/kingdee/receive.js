import request from '@/utils/request'

// 表字典、字段字典、12 条关联关系
export function getReceiveMeta() {
  return request({
    url: '/kingdee/receive/meta',
    method: 'get'
  })
}

// 单号联想 / 模糊匹配候选
export function listReceiveBills(billNo) {
  return request({
    url: '/kingdee/receive/list',
    method: 'get',
    params: { billNo }
  })
}

// 主查询：按单据编号（或主表内码 fid）返回 13 张表的关联数据
export function queryReceive(params) {
  return request({
    url: '/kingdee/receive/query',
    method: 'get',
    params
  })
}

// 导出 Excel：table 传 all 导出全部表，传表名只导出该表
export function exportReceiveExcel(params) {
  return request({
    url: '/kingdee/receive/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 批量查询：billNos 以逗号分隔，返回多个单据的结果列表
export function queryBatchReceive(params) {
  return request({
    url: '/kingdee/receive/queryBatch',
    method: 'get',
    params
  })
}

// 通用 Excel 处理：上传 Excel，后端按固定 11 列处理并返回结果文件
export function fixBoxNo(formData) {
  return request({
    url: '/kingdee/tool/excel/process',
    method: 'post',
    data: formData,
    responseType: 'blob',
    timeout: 120000
  })
}

// 批量导出 Excel：每张表一个工作表，多单据数据顺序追加并标注来源单据
export function exportBatchReceive(params) {
  return request({
    url: '/kingdee/receive/exportBatch',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
