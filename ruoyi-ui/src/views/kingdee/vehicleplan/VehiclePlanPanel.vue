<template>
  <div class="vehicle-plan-panel">
    <el-row :gutter="14" class="stat-row">
      <el-col v-for="stat in statCards" :key="stat.label" :span="6">
        <div class="stat-card" :style="{ borderTopColor: stat.color }">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-icon" :style="{ color: stat.color }"><i :class="stat.icon"></i></div>
        </div>
      </el-col>
    </el-row>

    <el-card v-if="!hideBillCard" shadow="never" class="block-gap">
      <div slot="header" class="card-header">
        <span><i class="el-icon-truck"></i> 车辆计划主表 · {{ dossier.billNo }}</span>
        <span class="card-meta">fid = {{ dossier.fid }} · 耗时 {{ dossier.meta.elapsedMs }} ms · {{ dossier.meta.queriedAt }}</span>
      </div>
      <el-descriptions :column="4" border size="small" class="bill-desc">
        <el-descriptions-item v-for="field in billFields" :key="field.key" :label="field.label">
          <span :class="{ 'cell-empty': !field.value }">{{ field.value || '—' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div class="chain-line">
        <span class="chain-node">车辆计划 <b>fid</b></span><span class="chain-arrow">→</span>
        <span class="chain-node">物料信息 <b>fentryid</b></span><span class="chain-arrow">→</span>
        <span class="chain-node">箱件信息</span>
        <span class="chain-arrow">｜</span>
        <span class="chain-node">车辆明细 <b>fid</b></span>
      </div>
    </el-card>

    <el-card shadow="never" class="block-gap">
      <el-tabs v-model="activeTab">
        <el-tab-pane :label="'物料信息 (' + entries.length + ')'" name="entries">
          <el-table :data="entries" border stripe size="small" row-key="fentryid">
            <el-table-column type="expand">
              <template slot-scope="scope">
                <div class="expand-wrap">
                  <section class="expand-section">
                    <h4>
                      <i class="el-icon-box"></i> 箱件信息
                      <el-tag size="mini">{{ (scope.row.ext[SUB_ENTRY] || []).length }}</el-tag>
                    </h4>
                    <dynamic-table
                      :rows="scope.row.ext[SUB_ENTRY] || []"
                      :columns="sectionOf(SUB_ENTRY).columns"
                      :labels="columnLabels"
                      :max-height="240"
                    />
                  </section>
                </div>
              </template>
            </el-table-column>
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column
              v-if="sourceColumn"
              :prop="sourceColumn.key"
              :label="sourceColumn.label"
              min-width="200"
              show-overflow-tooltip
            >
              <template slot-scope="scope">
                <span :class="{ 'cell-empty': isEmpty(scope.row[sourceColumn.key]) }">{{ display(scope.row[sourceColumn.key]) }}</span>
              </template>
            </el-table-column>
            <el-table-column
              v-for="col in detailColumns"
              :key="col"
              :prop="col"
              :label="labelOf(col)"
              :min-width="widthOf(col)"
              show-overflow-tooltip
            >
              <template slot-scope="scope">
                <span :class="{ 'cell-empty': isEmpty(scope.row.row[col]) }">{{ display(scope.row.row[col]) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="箱件数" width="100" fixed="right" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" effect="plain">{{ scope.row.stats.boxes }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>        <el-tab-pane :label="'主表扩展 (' + billExtSections.length + ')'" name="billExt">
          <el-alert title="关联键：tk_usp_vehicleplan.fid = tk_usp_vehicleplan_tc.fid" type="info" :closable="false" class="inner-tip" />
          <el-collapse v-model="billExtExpanded">
            <el-collapse-item v-for="s in billExtSections" :key="s.name" :name="s.name">
              <template slot="title">
                <span class="collapse-title">{{ s.label }} <code>{{ s.name }}</code></span>
                <el-tag size="mini" effect="plain">{{ s.count }} 行 · {{ s.joinKey }}</el-tag>
              </template>
              <dynamic-table :rows="s.rows" :columns="s.columns" :labels="columnLabels" :max-height="300" />
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>

        <el-tab-pane :label="'车辆明细 (' + sectionOf(VEHICLE_ENTRY).count + ')'" name="vehicles">
          <el-alert title="关联键：tk_usp_vehicleplan.fid = tk_usp_vehicleentry.fid（一对多）" type="info" :closable="false" class="inner-tip" />
          <dynamic-table
            :rows="sectionOf(VEHICLE_ENTRY).rows"
            :columns="sectionOf(VEHICLE_ENTRY).columns"
            :labels="columnLabels"
            :max-height="420"
          />
        </el-tab-pane>

        <el-tab-pane :label="'全部表 (' + rawSections.length + ')'" name="raw">
          <el-alert title="字段字典未覆盖的列名原样展示，不推测金蝶业务含义" type="info" :closable="false" class="inner-tip" />
          <el-collapse v-model="rawExpanded">
            <el-collapse-item v-for="s in rawSections" :key="s.name" :name="s.name">
              <template slot="title">
                <span class="collapse-title">{{ s.label }} <code>{{ s.name }}</code></span>
                <el-tag size="mini" :type="s.count ? 'success' : 'info'" effect="plain">{{ s.count }} 行 · {{ s.joinKey }}</el-tag>
              </template>
              <dynamic-table :rows="s.rows" :columns="s.columns" :labels="columnLabels" :max-height="360" />
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>

        <el-tab-pane label="关联关系" name="relations">
          <el-alert title="4 条关系来自《采购车辆计划 ER 图》；关系不代表数据库一定创建了物理外键" type="info" :closable="false" class="inner-tip" />
          <el-table :data="relations" border stripe size="small">
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="note" label="链路说明" min-width="230" />
            <el-table-column prop="from" label="主表" min-width="220">
              <template slot-scope="scope"><code>{{ scope.row.from }}</code></template>
            </el-table-column>
            <el-table-column prop="fromKey" label="主表字段" min-width="110">
              <template slot-scope="scope"><b>{{ scope.row.fromKey }}</b></template>
            </el-table-column>
            <el-table-column prop="to" label="从表" min-width="220">
              <template slot-scope="scope"><code>{{ scope.row.to }}</code></template>
            </el-table-column>
            <el-table-column prop="toKey" label="从表字段" min-width="110">
              <template slot-scope="scope"><b>{{ scope.row.toKey }}</b></template>
            </el-table-column>
            <el-table-column prop="cardinality" label="基数" width="100" align="center">
              <template slot-scope="scope"><el-tag size="mini">{{ relationLabel(scope.row.cardinality) }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-alert
      v-for="(w, i) in (dossier.warnings || [])"
      :key="i"
      :title="w"
      type="warning"
      show-icon
      class="block-gap"
    />
  </div>
</template><script>
import DynamicTable from '../receive/DynamicTable.vue'

const EMPTY_SECTION = { name: '', label: '', joinKey: '', count: 0, columns: [], rows: [] }
const PLAN = 'tk_usp_vehicleplan'
const PLAN_TC = 'tk_usp_vehicleplan_tc'
const CONTAINER_ENTRY = 'tk_usp_container_entry'
const SUB_ENTRY = 'tk_usp_subentryentity'
const VEHICLE_ENTRY = 'tk_usp_vehicleentry'

/**
 * 采购车辆计划查询结果面板：单号查询与批量查询共用同一套展示与交互。
 * 每个实例持有独立的 Tab / 折叠状态，多个单据面板之间互不影响。
 */
export default {
  name: 'VehiclePlanPanel',
  components: { DynamicTable },
  props: {
    dossier: { type: Object, required: true },
    tableDefs: { type: Array, default: () => [] },
    columnLabels: { type: Object, default: () => ({}) },
    relations: { type: Array, default: () => [] },
    hideBillCard: { type: Boolean, default: false },
    sourceColumn: { type: Object, default: null }
  },
  data() {
    return {
      // 表名常量同时暴露给模板使用
      PLAN,
      PLAN_TC,
      CONTAINER_ENTRY,
      SUB_ENTRY,
      VEHICLE_ENTRY,
      activeTab: 'entries',
      billExtExpanded: [],
      rawExpanded: [PLAN]
    }
  },
  computed: {
    entries() { return (this.dossier && this.dossier.entries) || [] },
    summary() { return (this.dossier && this.dossier.summary) || {} },
    entryColumns() { return this.sectionOf(CONTAINER_ENTRY).columns },
    detailColumns() {
      if (!this.sourceColumn) return this.entryColumns
      return this.entryColumns.filter(col => col !== this.sourceColumn.key)
    },
    billExtSections() {
      return this.tableDefs
        .filter(t => t.scope === 'bill' && t.name !== PLAN && t.name !== VEHICLE_ENTRY)
        .map(t => this.sectionOf(t.name))
    },
    rawSections() { return this.tableDefs.map(t => this.sectionOf(t.name)) },
    billFields() {
      const bill = (this.dossier && this.dossier.bill) || {}
      return Object.keys(bill).map(key => ({ key, label: this.labelOf(key), value: this.display(bill[key]) }))
    },
    statCards() {
      return [
        { label: '物料信息行', value: this.summary.entryCount || 0, icon: 'el-icon-tickets', color: '#409eff' },
        { label: '箱件记录', value: this.summary.boxCount || 0, icon: 'el-icon-box', color: '#67c23a' },
        { label: '车辆记录', value: this.summary.vehicleCount || 0, icon: 'el-icon-truck', color: '#e6a23c' },
        { label: '有数据的表', value: this.summary.nonEmptyTableCount || 0, icon: 'el-icon-folder-opened', color: '#8b6cf0' }
      ]
    }
  },
  methods: {
    sectionOf(name) {
      return (this.dossier && this.dossier.tables && this.dossier.tables[name]) || EMPTY_SECTION
    },
    labelOf(col) { return this.columnLabels[col] || col },
    isEmpty(value) { return value === null || value === undefined || value === '' },
    display(value) {
      if (this.isEmpty(value)) return ''
      if (typeof value === 'object') return JSON.stringify(value)
      return String(value)
    },
    widthOf(col) {
      if (/url|remark|name|material|sendboxno|wxno|supplier|driver/i.test(col)) return 180
      if (/^(fseq|fqty|fbillstatus|fpkid|ftaxrate)$/i.test(col)) return 105
      return 145
    },
    relationLabel(value) {
      return { One: '1 : 1', ZeroOne: '0..1', OneN: '1 : N' }[value] || value
    }
  }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px; }
.card-meta { font-size: 12px; color: #909399; font-weight: normal; }
.bill-desc { margin-bottom: 12px; }
.chain-line { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; background: #f7f9fc; border: 1px solid #e8edf3; border-radius: 6px; padding: 10px 12px; font-size: 12px; }
.chain-node { background: #fff; border: 1px solid #d9e2ee; border-radius: 4px; padding: 4px 9px; color: #4a5568; }
.chain-node b { color: #409eff; }
.chain-arrow { color: #a8b0bf; }
.stat-row { margin-top: 14px; }
.stat-card { position: relative; background: #fff; border: 1px solid #e8edf3; border-top: 3px solid #409eff; border-radius: 8px; padding: 16px 18px; box-shadow: 0 1px 4px rgba(31,45,61,.04); }
.stat-value { font-size: 25px; font-weight: 600; color: #1f2d3d; line-height: 1.2; }
.stat-label { font-size: 12px; color: #909399; margin-top: 6px; }
.stat-icon { position: absolute; right: 16px; top: 18px; font-size: 26px; opacity: .25; }
.inner-tip { margin-bottom: 12px; }
.sub-title { font-size: 13px; color: #4a5568; font-weight: 600; margin: 12px 0 6px; }
.collapse-title { margin-right: 10px; color: #303133; }
.expand-wrap { padding: 12px 16px; background: #fafbfd; }
.expand-section { min-width: 0; }
.expand-section h4 { margin: 0 0 8px; font-size: 13px; color: #4a5568; }
.vehicle-plan-panel /deep/ .el-card { border-radius: 8px; }
.vehicle-plan-panel /deep/ .el-tabs__header { margin-bottom: 14px; }
</style>