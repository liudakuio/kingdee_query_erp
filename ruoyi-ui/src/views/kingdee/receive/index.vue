<template>
  <div class="app-container receive-page">
    <div class="page-head">
      <div>
        <h2>采购收货单关联查询</h2>
        <p>以单据编号为入口，按 <code>fid → fentryid → fdetailid</code> 三层关联键穿透 {{ metaInfo.tableCount || 13 }} 张业务表</p>
      </div>
      <el-tag :type="modeType" effect="dark" size="small">数据源：{{ modeText }}</el-tag>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" label-width="110px" @submit.native.prevent>
        <el-form-item label="采购收货单号">
          <el-autocomplete
            v-model="queryParams.billNo"
            :fetch-suggestions="querySearch"
            :trigger-on-focus="false"
            clearable
            placeholder="请输入单据编号，支持单号联想"
            style="width: 320px"
            @select="onSuggestSelect"
            @input="onBillNoInput"
            @keyup.enter.native="handleQuery"
          >
            <template slot-scope="{ item }">
              <div class="suggest-row">
                <span>{{ item.value }}</span><span class="suggest-fid">{{ item.label }}</span>
              </div>
            </template>
          </el-autocomplete>
        </el-form-item>
        <el-form-item label="模糊查询">
          <el-switch v-model="queryParams.fuzzy" active-text="开" inactive-text="关" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          <el-button icon="el-icon-download" :disabled="!canExport" @click="exportJson">导出结果</el-button>
        </el-form-item>
        <el-form-item label="导出 Excel">
          <el-select v-model="exportTable" size="small" style="width: 260px" placeholder="选择导出范围">
            <el-option label="全部表（13 张，每张一个工作表）" value="all" />
            <el-option
              v-for="t in tableDefs"
              :key="t.name"
              :label="t.label + ' · ' + t.name"
              :value="t.name"
            />
          </el-select>
          <el-button
            type="success"
            icon="el-icon-document"
            :disabled="!canExport"
            :loading="exporting"
            style="margin-left: 8px"
            @click="exportExcel"
          >导出 Excel</el-button>
        </el-form-item>
      </el-form>
      <div class="demo-line">
        <span>演示单号：</span>
        <el-link
          v-for="no in demoBillNos"
          :key="no"
          type="primary"
          :underline="false"
          class="demo-link"
          @click="useDemoBill(no)"
        >{{ no }}</el-link>
        <span class="demo-hint">默认使用内置 H2 演示库；切换真实金蝶库见 README</span>
      </div>
    </el-card>

    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      closable
      class="block-gap"
      @close="errorMsg = ''"
    />

    <el-empty
      v-if="!hasSearched && !loading"
      description="输入采购收货单号，查询主表、明细、收货箱与附件全链路数据"
      class="empty-block"
    />

    <el-card v-if="dossier && dossier.candidates && dossier.candidates.length" shadow="never" class="block-gap">
      <div slot="header"><i class="el-icon-search"></i> {{ dossier.message }}</div>
      <el-table
        :data="dossier.candidates"
        border
        size="small"
        highlight-current-row
        class="clickable-table"
        @row-click="chooseCandidate"
      >
        <el-table-column prop="billNo" label="单据编号" min-width="220" />
        <el-table-column prop="fid" label="主表内码 fid" min-width="140" />
        <el-table-column prop="status" label="状态" min-width="100" />
        <el-table-column prop="bizDate" label="业务日期" min-width="130" />
        <el-table-column label="操作" width="110" align="center">
          <template slot-scope="scope">
            <el-button type="text" @click.stop="chooseCandidate(scope.row)">查看数据</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-alert
      v-if="dossier && !dossier.found && !(dossier.candidates && dossier.candidates.length)"
      :title="dossier.message || '未查询到数据'"
      type="warning"
      show-icon
      class="block-gap"
    />

    <div v-if="dossier && dossier.found" class="result-block">
      <el-row :gutter="14" class="stat-row">
        <el-col v-for="stat in statCards" :key="stat.label" :span="6">
          <div class="stat-card" :style="{ borderTopColor: stat.color }">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-icon" :style="{ color: stat.color }"><i :class="stat.icon"></i></div>
          </div>
        </el-col>
      </el-row>

      <el-card shadow="never" class="block-gap">
        <div slot="header" class="card-header">
          <span><i class="el-icon-document"></i> 单据主表 · {{ dossier.billNo }}</span>
          <span class="card-meta">fid = {{ dossier.fid }} · 耗时 {{ dossier.meta.elapsedMs }} ms · {{ dossier.meta.queriedAt }}</span>
        </div>
        <el-descriptions :column="4" border size="small" class="bill-desc">
          <el-descriptions-item v-for="field in billFields" :key="field.key" :label="field.label">
            <span :class="{ 'cell-empty': !field.value }">{{ field.value || '—' }}</span>
          </el-descriptions-item>
        </el-descriptions>
        <div class="chain-line">
          <span class="chain-node">主表 <b>fid</b></span><span class="chain-arrow">→</span>
          <span class="chain-node">物料明细 <b>fentryid</b></span><span class="chain-arrow">→</span>
          <span class="chain-node">收货箱 / 附件清单</span><span class="chain-arrow">→</span>
          <span class="chain-node">附件表 <b>fdetailid</b></span>
        </div>
      </el-card>

      <el-card shadow="never" class="block-gap">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="'物料明细 (' + entries.length + ')'" name="entries">
            <el-table :data="entries" border stripe size="small" row-key="fentryid">
              <el-table-column type="expand">
                <template slot-scope="scope">
                  <div class="expand-wrap">
                    <section class="expand-section">
                      <h4><i class="el-icon-box"></i> 收货箱明细 <el-tag size="mini">{{ scope.row.boxes.length }}</el-tag></h4>
                      <dynamic-table
                        :rows="scope.row.boxes"
                        :columns="sectionOf('tk_usp_receivingboxentry').columns"
                        :labels="columnLabels"
                        :max-height="240"
                      />
                    </section>
                    <section class="expand-section">
                      <h4><i class="el-icon-paperclip"></i> 附件清单 <el-tag size="mini">{{ scope.row.attachList.length }}</el-tag></h4>
                      <el-collapse v-if="scope.row.attachList.length">
                        <el-collapse-item v-for="(a, idx) in scope.row.attachList" :key="a.fdetailid || idx" :name="String(idx)">
                          <template slot="title">
                            <span class="attach-title">{{ a.row.fk_usp_attachname || a.row.fattachname || ('附件 ' + (idx + 1)) }}</span>
                            <el-tag size="mini" type="info" effect="plain">{{ a.files.length }} 个文件</el-tag>
                          </template>
                          <dynamic-table :rows="[a.row]" :labels="columnLabels" :max-height="180" />
                          <div class="sub-title">附件表记录（fdetailid = {{ a.fdetailid }}）</div>
                          <dynamic-table
                            :rows="a.files"
                            :columns="sectionOf('tk_usp_cgentryattachment').columns"
                            :labels="columnLabels"
                            :max-height="180"
                          />
                        </el-collapse-item>
                      </el-collapse>
                      <el-empty v-else description="该明细行无附件清单" :image-size="50" />
                    </section>
                    <section class="expand-section">
                      <h4><i class="el-icon-s-operation"></i> 明细扩展表（按 fentryid 关联）</h4>
                      <div v-for="t in entryExtTables" :key="t.name" class="ext-block">
                        <div class="sub-title">{{ t.label }} · <code>{{ t.name }}</code> · {{ (scope.row.ext[t.name] || []).length }} 行</div>
                        <dynamic-table :rows="scope.row.ext[t.name] || []" :labels="columnLabels" :max-height="180" />
                      </div>
                    </section>
                  </div>
                </template>
              </el-table-column>
              <el-table-column type="index" label="#" width="50" align="center" />
              <el-table-column
                v-for="col in entryColumns"
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
              <el-table-column label="关联统计" width="200" fixed="right" align="center">
                <template slot-scope="scope">
                  <el-tag size="mini" effect="plain">箱 {{ scope.row.stats.boxes }}</el-tag>
                  <el-tag size="mini" effect="plain" type="warning">附件 {{ scope.row.stats.attachments }}</el-tag>
                  <el-tag size="mini" effect="plain" type="success">文件 {{ scope.row.stats.files }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="'主表扩展 (' + billExtSections.length + ')'" name="billExt">
            <el-alert title="关联键：t_im_purrecbill.fid = 各扩展表.fid" type="info" :closable="false" class="inner-tip" />
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

          <el-tab-pane :label="'收货箱 (' + sectionOf('tk_usp_receivingboxentry').count + ')'" name="boxes">
            <el-alert title="关联键：t_im_purrecbillentry.fentryid = tk_usp_receivingboxentry.fentryid（一对多）" type="info" :closable="false" class="inner-tip" />
            <dynamic-table
              :rows="sectionOf('tk_usp_receivingboxentry').rows"
              :columns="sectionOf('tk_usp_receivingboxentry').columns"
              :labels="columnLabels"
              :max-height="460"
            />
          </el-tab-pane>

          <el-tab-pane :label="'附件清单与附件 (' + sectionOf('tk_usp_cgshentryattach').count + ')'" name="attach">
            <el-alert title="明细 fentryid → 附件清单；附件清单 fdetailid → 附件表" type="info" :closable="false" class="inner-tip" />
            <div class="sub-title">附件清单子单据体 <code>tk_usp_cgshentryattach</code></div>
            <dynamic-table
              :rows="sectionOf('tk_usp_cgshentryattach').rows"
              :columns="sectionOf('tk_usp_cgshentryattach').columns"
              :labels="columnLabels"
              :max-height="280"
            />
            <div class="sub-title">附件表 <code>tk_usp_cgentryattachment</code>（{{ sectionOf('tk_usp_cgentryattachment').count }} 行）</div>
            <dynamic-table
              :rows="sectionOf('tk_usp_cgentryattachment').rows"
              :columns="sectionOf('tk_usp_cgentryattachment').columns"
              :labels="columnLabels"
              :max-height="280"
            />
          </el-tab-pane>

          <el-tab-pane :label="'全部表 (' + rawSections.length + ')'" name="raw">
            <el-alert title="SELECT * 动态列：字段字典未覆盖的列名原样展示，不推测金蝶业务含义" type="info" :closable="false" class="inner-tip" />
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
            <el-alert title="12 条关系来自数据结构文档第 3 节；ERD 逻辑关系不代表数据库一定创建了物理外键" type="info" :closable="false" class="inner-tip" />
            <el-table :data="metaInfo.relations" border stripe size="small">
              <el-table-column type="index" label="#" width="50" align="center" />
              <el-table-column prop="note" label="链路说明" min-width="230" />
              <el-table-column prop="from" label="主表" min-width="220">
                <template slot-scope="scope"><code>{{ scope.row.from }}</code></template>
              </el-table-column>
              <el-table-column prop="fromKey" label="主表字段" min-width="110">
                <template slot-scope="scope"><b>{{ scope.row.fromKey }}</b></template>
              </el-table-column>
              <el-table-column prop="to" label="从表" min-width="230">
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
  </div>
</template>

<script>
import { getReceiveMeta, listReceiveBills, queryReceive, exportReceiveExcel } from '@/api/kingdee/receive'
import DynamicTable from './DynamicTable.vue'

const EMPTY_SECTION = { name: '', label: '', joinKey: '', count: 0, columns: [], rows: [] }
const MAIN_BILL = 't_im_purrecbill'
const MAIN_ENTRY = 't_im_purrecbillentry'
const BOX = 'tk_usp_receivingboxentry'
const ATTACH = 'tk_usp_cgshentryattach'
const ATTACH_FILE = 'tk_usp_cgentryattachment'

export default {
  name: 'PurReceiveIndex',
  components: { DynamicTable },
  data() {
    return {
      metaInfo: { tables: [], relations: [], columnLabels: {}, tableCount: 13 },
      queryParams: { billNo: '', fid: null, fuzzy: false },
      exportTable: 'all',
      exporting: false,
      demoBillNos: ['CGSH-20261111-0001', 'CGSH-20261111-0002', 'CGSH-20261110-0003'],
      dossier: null,
      loading: false,
      hasSearched: false,
      errorMsg: '',
      activeTab: 'entries',
      billExtExpanded: [],
      rawExpanded: [MAIN_BILL]
    }
  },
  computed: {
    columnLabels() { return this.metaInfo.columnLabels || {} },
    tableDefs() { return this.metaInfo.tables || [] },
    entries() { return (this.dossier && this.dossier.entries) || [] },
    summary() { return (this.dossier && this.dossier.summary) || {} },
    entryColumns() { return this.sectionOf(MAIN_ENTRY).columns },
    billExtSections() {
      return this.tableDefs.filter(t => t.scope === 'bill' && t.name !== MAIN_BILL).map(t => this.sectionOf(t.name))
    },
    entryExtTables() {
      return this.tableDefs.filter(t => t.scope === 'entry' && t.name !== MAIN_ENTRY)
    },
    rawSections() { return this.tableDefs.map(t => this.sectionOf(t.name)) },
    billFields() {
      const bill = (this.dossier && this.dossier.bill) || {}
      return Object.keys(bill).map(key => ({ key, label: this.labelOf(key), value: this.display(bill[key]) }))
    },
    statCards() {
      return [
        { label: '物料明细行', value: this.summary.entryCount || 0, icon: 'el-icon-tickets', color: '#409eff' },
        { label: '收货箱记录', value: this.summary.boxCount || 0, icon: 'el-icon-box', color: '#67c23a' },
        { label: '附件清单', value: this.summary.attachCount || 0, icon: 'el-icon-paperclip', color: '#e6a23c' },
        { label: '附件文件 / 有数据的表', value: (this.summary.fileCount || 0) + ' / ' + (this.summary.nonEmptyTableCount || 0), icon: 'el-icon-folder-opened', color: '#8b6cf0' }
      ]
    },
    canExport() { return !!(this.dossier && this.dossier.found) },
    modeType() { return this.isRealDb ? 'warning' : 'success' },
    modeText() { return this.isRealDb ? '真实金蝶库' : 'H2 演示库' },
    isRealDb() { return !!(this.dossier && this.dossier.meta && this.dossier.meta.mode === 'kingdee') }
  },
  created() {
    this.loadMeta()
  },
  methods: {
    loadMeta() {
      getReceiveMeta().then(res => {
        if (res && res.data) this.metaInfo = res.data
      }).catch(() => {})
    },
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
      if (/url|remark|name|materialname|attachname|filename|supplier/i.test(col)) return 180
      if (/^(fseq|fqty|fbillstatus|fpkid|fk_usp_checkboxfield|ftaxrate)$/i.test(col)) return 105
      return 145
    },
    relationLabel(value) {
      return { One: '1 : 1', ZeroOne: '0..1', OneN: '1 : N' }[value] || value
    },
    handleQuery() {
      const billNo = (this.queryParams.billNo || '').trim()
      if (!billNo && !this.queryParams.fid) {
        this.$message.warning('请输入采购收货单号')
        return
      }
      this.loading = true
      this.errorMsg = ''
      const params = {
        billNo: billNo || undefined,
        fid: this.queryParams.fid || undefined,
        fuzzy: this.queryParams.fuzzy
      }
      queryReceive(params).then(res => {
        this.dossier = (res && res.data) || null
        this.hasSearched = true
        this.loading = false
        if (this.dossier && this.dossier.found) {
          this.activeTab = 'entries'
          this.rawExpanded = [MAIN_BILL]
        }
      }).catch(error => {
        this.loading = false
        this.hasSearched = true
        this.dossier = null
        this.errorMsg = (error && error.message) || '查询失败'
      })
    },
    querySearch(keyword, callback) {
      if (!keyword || !keyword.trim()) { callback([]); return }
      listReceiveBills(keyword.trim()).then(res => {
        const rows = (res && res.data) || []
        callback(rows.map(row => ({ value: row.billNo, fid: row.fid, label: row.label })))
      }).catch(() => callback([]))
    },
    onSuggestSelect(item) {
      this.queryParams.billNo = item.value
      this.queryParams.fid = item.fid
      this.queryParams.fuzzy = false
      this.handleQuery()
    },
    onBillNoInput() {
      this.queryParams.fid = null
    },
    chooseCandidate(row) {
      this.queryParams.billNo = row.billNo
      this.queryParams.fid = row.fid
      this.queryParams.fuzzy = false
      this.handleQuery()
    },
    useDemoBill(no) {
      this.queryParams.billNo = no
      this.queryParams.fid = null
      this.queryParams.fuzzy = false
      this.handleQuery()
    },
    resetQuery() {
      this.queryParams = { billNo: '', fid: null, fuzzy: false }
      this.dossier = null
      this.hasSearched = false
      this.errorMsg = ''
      this.activeTab = 'entries'
    },
    exportExcel() {
      if (!this.canExport) return
      this.exporting = true
      const params = {
        billNo: (this.queryParams.billNo || '').trim() || undefined,
        fid: this.queryParams.fid || undefined,
        table: this.exportTable
      }
      exportReceiveExcel(params).then(res => {
        this.exporting = false
        // 后端校验失败时返回 JSON 而非文件，不能把错误提示当成 Excel 下载
        if (res && res.type && /json/i.test(res.type)) {
          const reader = new FileReader()
          reader.onload = () => {
            let msg = '导出失败'
            try { msg = (JSON.parse(reader.result) || {}).msg || msg } catch (e) { /* 保留默认提示 */ }
            this.$message.error(msg)
          }
          reader.readAsText(res)
          return
        }
        const blob = res instanceof Blob ? res : new Blob([res])
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = '收货单_' + (this.dossier.billNo || 'query') + '_' +
          (this.exportTable === 'all' ? '全部表' : this.exportTable) + '.xlsx'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        this.$message.success('已导出 Excel')
      }).catch(error => {
        this.exporting = false
        this.$message.error((error && error.message) || '导出失败')
      })
    },
    exportJson() {
      const content = JSON.stringify(this.dossier, null, 2)
      const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = '收货单_' + (this.dossier.billNo || 'query') + '.json'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      this.$message.success('已导出查询结果')
    }
  }
}
</script>

<style scoped>
.page-head { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 14px; }
.page-head h2 { margin: 0 0 6px; font-size: 20px; color: #1f2d3d; }
.page-head p { margin: 0; font-size: 13px; color: #7a869a; }
.search-card { border-radius: 8px; }
.search-card /deep/ .el-card__body { padding-bottom: 12px; }
.search-card /deep/ .el-form-item { margin-bottom: 10px; }
.demo-line { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; font-size: 12px; color: #7a869a; border-top: 1px dashed #e8edf3; padding-top: 10px; }
.demo-link { font-size: 12px; }
.demo-hint { color: #a8b0bf; }
.suggest-row { display: flex; justify-content: space-between; align-items: center; }
.suggest-fid { color: #a8b0bf; font-size: 11px; margin-left: 12px; }
.empty-block { margin-top: 18px; background: #fff; border-radius: 8px; padding: 40px 0; }
.clickable-table { cursor: pointer; }
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
.expand-wrap { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; padding: 12px 16px; background: #fafbfd; }
.expand-section { min-width: 0; }
.expand-section h4 { margin: 0 0 8px; font-size: 13px; color: #4a5568; }
.ext-block { margin-bottom: 8px; }
.attach-title { margin-right: 10px; }
.result-block /deep/ .el-card { border-radius: 8px; }
.result-block /deep/ .el-tabs__header { margin-bottom: 14px; }
</style>
