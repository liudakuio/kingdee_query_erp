import request from '@/utils/request'

// 主查询：按单据编号（或主表内码 fid）返回 4 张表的关联数据
export function queryExpress(params) {
  return request({
    url: '/kingdee/express/query',
    method: 'get',
    params
  })
}

// 批量查询：billNos 以中英文逗号分隔，返回多个单据的结果与数量汇总
export function queryExpressBatch(params) {
  return request({
    url: '/kingdee/express/queryBatch',
    method: 'get',
    params
  })
}

// 导出 Excel：table 传 all 导出全部表，传表名只导出该表
export function exportExpressExcel(params) {
  return request({
    url: '/kingdee/express/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 批量导出 Excel：每个单据的数据追加到同一张表，末尾标注来源单据编号
export function exportExpressBatchExcel(params) {
  return request({
    url: '/kingdee/express/exportBatch',
    method: 'get',
    params,
    responseType: 'blob'
  })
}