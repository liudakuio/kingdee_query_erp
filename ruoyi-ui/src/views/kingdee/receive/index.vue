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
            placeholder="请输入单据编号，多个单号用逗号分隔"
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
      description="输入采购收货单号（多个用逗号分隔），查询主表、明细、收货箱与附件全链路数据"
      class="empty-block"
    />

    <!-- 批量查询：所有单据合并成一份结果，各表首列标识来源单据 -->
    <template v-if="batchResults.length">
      <dossier-panel
        v-if="mergedDossier.found"
        :dossier="mergedDossier"
        :hide-bill-card="true"
        :source-column="sourceColumn"
        :table-defs="tableDefs"
        :column-labels="mergedColumnLabels"
        :relations="metaInfo.relations || []"
      />
      <el-alert
        v-for="(item, idx) in missingBatchItems"
        :key="'missing-' + idx"
        :title="'未找到采购收货单：' + (item.billNo || '') + '　' + (item.message || '')"
        type="warning"
        show-icon
        class="block-gap"
      />
    </template>

    <el-card
      v-if="!batchResults.length && singleDossier && singleDossier.candidates && singleDossier.candidates.length"
      shadow="never"
      class="block-gap"
    >
      <div slot="header"><i class="el-icon-search"></i> {{ singleDossier.message }}</div>
      <el-table
        :data="singleDossier.candidates"
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
      v-if="!batchResults.length && singleDossier && !singleDossier.found && !(singleDossier.candidates && singleDossier.candidates.length)"
      :title="singleDossier.message || '未查询到数据'"
      type="warning"
      show-icon
      class="block-gap"
    />

    <!-- 单号查询：与批量查询使用同一个结果面板组件 -->
    <dossier-panel
      v-if="!batchResults.length && singleDossier && singleDossier.found"
      :dossier="singleDossier"
      :table-defs="tableDefs"
      :column-labels="columnLabels"
      :relations="metaInfo.relations || []"
    />
  </div>
</template>

<script>
import { getReceiveMeta, listReceiveBills, queryReceive, exportReceiveExcel, queryBatchReceive, exportBatchReceive } from '@/api/kingdee/receive'
import DossierPanel from './DossierPanel.vue'
import { mergeDossiers, SOURCE_KEY, SOURCE_LABEL } from './mergeDossier'

export default {
  name: 'PurReceiveIndex',
  components: { DossierPanel },
  data() {
    return {
      metaInfo: { tables: [], relations: [], columnLabels: {}, tableCount: 13 },
      queryParams: { billNo: '', fid: null, fuzzy: false },
      exportTable: 'all',
      exporting: false,
      demoBillNos: ['CGSH-20261111-0001', 'CGSH-20261111-0002', 'CGSH-20261110-0003'],
      singleDossier: null,
      batchResults: [],
      batchSummary: { total: 0, foundCount: 0, missingCount: 0, totals: {} },
      loading: false,
      hasSearched: false,
      errorMsg: ''
    }
  },
  computed: {
    columnLabels() { return this.metaInfo.columnLabels || {} },
    tableDefs() { return this.metaInfo.tables || [] },
    foundBatchItems() {
      return this.batchResults.filter(item => item.found)
    },
    // 批量视图：所有单据合并成一份，表格首列标识来源单据
    mergedDossier() {
      return mergeDossiers(this.foundBatchItems, this.tableDefs)
    },
    mergedColumnLabels() {
      return Object.assign({}, this.columnLabels, { [SOURCE_KEY]: SOURCE_LABEL })
    },
    sourceColumn() {
      return { key: SOURCE_KEY, label: SOURCE_LABEL }
    },
    missingBatchItems() {
      return this.batchResults.filter(item => !item.found)
    },
    canExport() {
      return this.batchResults.length
        ? !!(this.batchSummary && this.batchSummary.foundCount > 0)
        : !!(this.singleDossier && this.singleDossier.found)
    },
    modeType() { return this.isRealDb ? 'warning' : 'success' },
    modeText() { return this.isRealDb ? '真实金蝶库' : 'H2 演示库' },
    isRealDb() {
      if (this.batchResults.length) return !!(this.batchSummary && this.batchSummary.mode === 'kingdee')
      return !!(this.singleDossier && this.singleDossier.meta && this.singleDossier.meta.mode === 'kingdee')
    }
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
    handleQuery() {
      const billNo = (this.queryParams.billNo || '').trim()
      if (!billNo && !this.queryParams.fid) {
        this.$message.warning('请输入采购收货单号')
        return
      }
      this.loading = true
      this.errorMsg = ''
      if (/[,，]/.test(billNo)) {
        queryBatchReceive({ billNos: billNo }).then(res => {
          const data = (res && res.data) || {}
          // 兼容汇总对象（新版）与裸数组（旧版后端），避免对象被当成数组导致整页空白
          const legacy = Array.isArray(data)
          this.batchResults = legacy ? data : (data.items || [])
          this.batchSummary = legacy
            ? {
                total: data.length,
                foundCount: data.filter(x => x.found).length,
                missingCount: data.filter(x => !x.found).length,
                totals: {},
                mode: (data[0] && data[0].meta && data[0].meta.mode) || 'demo'
              }
            : data
          this.singleDossier = null
          this.hasSearched = true
          this.loading = false
        }).catch(error => {
          this.loading = false
          this.hasSearched = true
          this.batchResults = []
          this.batchSummary = { total: 0, foundCount: 0, missingCount: 0, totals: {} }
          this.singleDossier = null
          this.errorMsg = (error && error.message) || '批量查询失败'
        })
        return
      }
      const params = {
        billNo: billNo || undefined,
        fid: this.queryParams.fid || undefined,
        fuzzy: this.queryParams.fuzzy
      }
      queryReceive(params).then(res => {
        this.batchResults = []
        this.batchSummary = { total: 0, foundCount: 0, missingCount: 0, totals: {} }
        this.singleDossier = (res && res.data) || null
        this.hasSearched = true
        this.loading = false
      }).catch(error => {
        this.loading = false
        this.hasSearched = true
        this.singleDossier = null
        this.errorMsg = (error && error.message) || '查询失败'
      })
    },
    querySearch(keyword, callback) {
      if (!keyword || !keyword.trim() || /[,，]/.test(keyword)) { callback([]); return }
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
      this.singleDossier = null
      this.batchResults = []
      this.batchSummary = { total: 0, foundCount: 0, missingCount: 0, totals: {} }
      this.hasSearched = false
      this.errorMsg = ''
    },
    exportExcel() {
      if (!this.canExport) return
      this.exporting = true
      const billNo = (this.queryParams.billNo || '').trim()
      const isBatch = this.batchResults.length > 0
      const params = isBatch
        ? { billNos: billNo, table: this.exportTable }
        : { billNo: billNo || undefined, fid: this.queryParams.fid || undefined, table: this.exportTable }
      const request = isBatch ? exportBatchReceive(params) : exportReceiveExcel(params)
      const subject = isBatch
        ? '批量' + this.batchResults.length + '单'
        : ((this.singleDossier && this.singleDossier.billNo) || 'query')
      const name = '收货单_' + subject + '_' +
        (this.exportTable === 'all' ? '全部表' : this.exportTable) + '.xlsx'
      request.then(res => {
        this.exporting = false
        this.saveExcelBlob(res, name)
      }).catch(error => {
        this.exporting = false
        this.$message.error((error && error.message) || '导出失败')
      })
    },
    saveExcelBlob(res, name) {
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
      link.download = name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      this.$message.success('已导出 Excel')
    },
    exportJson() {
      const isBatch = this.batchResults.length > 0
      const content = JSON.stringify(isBatch ? this.batchSummary : this.singleDossier, null, 2)
      const blob = new Blob([content], { type: 'application/json;charset=utf-8' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      const subject = isBatch
        ? '批量' + this.batchResults.length + '单'
        : ((this.singleDossier && this.singleDossier.billNo) || 'query')
      link.download = '收货单_' + subject + '.json'
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
</style>
