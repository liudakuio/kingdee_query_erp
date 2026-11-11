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
