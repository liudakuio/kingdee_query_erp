/**
 * 批量查询合并工具：把多个单据的查询结果合并成一份，用于批量查询时统一展示。
 * 合并后每张表、每条明细都带上来源单据编号（SOURCE_KEY），
 * 前端据此在表格首列标识数据来自哪个单据。
 */

/** 来源单据编号在行对象中的键 */
export const SOURCE_KEY = '__billNo'

/** 来源单据编号的列表头 */
export const SOURCE_LABEL = '单据编号'

/** 浅拷贝一行并追加来源单据编号 */
function withSource(row, billNo) {
  return Object.assign({}, row, { [SOURCE_KEY]: billNo })
}

/** 从各单据结果里收集表定义（按首次出现顺序去重） */
function collectDefs(items) {
  const defs = []
  const seen = {}
  for (const item of items) {
    const tables = (item && item.tables) || {}
    for (const name of Object.keys(tables)) {
      if (seen[name]) continue
      seen[name] = true
      const section = tables[name]
      defs.push({ name, label: section.label, joinKey: section.joinKey, scope: section.scope })
    }
  }
  return defs
}

/**
 * 合并多个单据的 dossier，输出结构与单据 dossier 保持一致：
 * - tables：同名表的 rows 顺序拼接，columns 首列插入来源单据列
 * - entries：各单据明细行拼接，同样带来源单据编号
 * - summary：所有单据的合计
 * - bill：留空（批量视图不展示单据主表模块）
 */
export function mergeDossiers(items, tableDefs) {
  const found = (items || []).filter(item => item && item.found)
  const defs = (tableDefs && tableDefs.length) ? tableDefs : collectDefs(found)

  const tables = {}
  for (const def of defs) {
    const name = def.name
    const rows = []
    let columns = []
    let label = def.label
    let joinKey = def.joinKey
    let scope = def.scope
    let truncated = false
    for (const item of found) {
      const section = ((item.tables || {})[name])
      if (!section) continue
      if (!columns.length) columns = (section.columns || []).slice()
      label = label || section.label
      joinKey = joinKey || section.joinKey
      scope = scope || section.scope
      truncated = truncated || !!section.truncated
      for (const row of section.rows || []) {
        rows.push(withSource(row, item.billNo))
      }
    }
    if (!columns.length) continue
    tables[name] = {
      name,
      label,
      joinKey,
      scope,
      count: rows.length,
      columns: [SOURCE_KEY].concat(columns),
      rows,
      truncated
    }
  }

  const entries = []
  const warnings = []
  const summary = {
    entryCount: 0,
    boxCount: 0,
    attachCount: 0,
    fileCount: 0,
    tableCount: defs.length,
    nonEmptyTableCount: 0
  }
  for (const item of found) {
    for (const entry of item.entries || []) {
      entries.push(withSource(entry, item.billNo))
    }
    const itemSummary = item.summary || {}
    summary.entryCount += itemSummary.entryCount || 0
    summary.boxCount += itemSummary.boxCount || 0
    summary.attachCount += itemSummary.attachCount || 0
    summary.fileCount += itemSummary.fileCount || 0
    for (const warning of item.warnings || []) {
      warnings.push((item.billNo || '') + '：' + warning)
    }
  }
  summary.nonEmptyTableCount = Object.keys(tables).filter(name => tables[name].count > 0).length

  return {
    found: found.length > 0,
    billNo: '',
    fid: null,
    bill: {},
    tables,
    entries,
    summary,
    warnings,
    meta: (found[0] && found[0].meta) || {}
  }
}
