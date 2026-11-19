/**
 * 前端 Schema 公共定义。
 *
 * 只放三个业务模块含义完全一致的字段标签；含义不同的字段（如 fdetailid）
 * 由各模块字典单独定义，避免改一个模块影响另一个模块。
 */

export const COMMON_FIELD_LABELS = {
  fid: '主表内码 fid',
  fentryid: '明细行内码 fentryid',
  fpkid: '主键 fpkid',
  fseq: '分录行号',
  fbillno: '单据编号',
  fbillstatus: '单据状态',
  fcreatorid: '创建人',
  fcreatetime: '创建时间',
  fmodifierid: '修改人',
  fmodifytime: '修改时间',
  fauditorid: '审核人',
  fauditdate: '审核时间',
  fk_usp_wxno: '集装箱号',
  fk_usp_arrived: '是否到达',
  fk_usp_detailqty: '应收数量'
}

/**
 * 生成页面使用的 metaInfo，字段结构与页面渲染约定保持一致。
 * 这里做浅拷贝，避免多个页面实例共享同一份可变数据。
 */
export function createSchemaMeta({ tables, relations, columnLabels }) {
  return {
    tables: tables.map(table => ({ ...table })),
    relations: relations.map(relation => ({ ...relation })),
    columnLabels: { ...columnLabels },
    tableCount: tables.length
  }
}
