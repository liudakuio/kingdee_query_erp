<template>
  <div class="app-container receive-page">
    <div class="page-head">
      <div>
        <h2>采购车辆计划关联查询</h2>
        <p>以单据编号为入口，按 <code>fid → fentryid</code> 两层关联键穿透 {{ metaInfo.tableCount || 5 }} 张业务表</p>
      </div>
      <el-tag :type="modeType" effect="dark" size="small">数据源：{{ modeText }}</el-tag>
    </div>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" label-width="110px" @submit.native.prevent>
        <el-form-item label="车辆计划单号">
          <el-input
            v-model="queryParams.billNo"
            clearable
            placeholder="请输入单据编号，多个单号用逗号分隔"
            style="width: 320px"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          <el-button icon="el-icon-download" :disabled="!canExport" @click="exportJson">导出结果</el-button>
        </el-form-item>
        <el-form-item label="导出 Excel">
          <el-select v-model="exportTable" size="small" style="width: 260px" placeholder="选择导出范围">
            <el-option label="全部表（5 张，每张一个工作表）" value="all" />
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
      description="输入采购车辆计划单号（多个用逗号分隔），查询主表、物料信息、箱件信息与车辆明细"
      class="empty-block"
    />

    <!-- 批量查询：所有单据合并成一份结果，各表首列标识来源单据 -->
    <template v-if="batchResults.length">
      <vehicle-plan-panel
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
        :title="'未找到采购车辆计划：' + (item.billNo || '') + '　' + (item.message || '')"
        type="warning"
        show-icon
        class="block-gap"
      />
    </template>

    <!-- 单号查询 -->
    <vehicle-plan-panel
      v-if="!batchResults.length && singleDossier && singleDossier.found"
      :dossier="singleDossier"
      :table-defs="tableDefs"
      :column-labels="columnLabels"
      :relations="metaInfo.relations || []"
    />
  </div>
</template><script>
import {
  getVehiclePlanMeta,
  queryVehiclePlan,
  queryVehiclePlanBatch,
  exportVehiclePlanExcel,
  exportVehiclePlanBatchExcel
} from '@/api/kingdee/vehiclePlan'
import VehiclePlanPanel from './VehiclePlanPanel.vue'
import { mergeDossiers, SOURCE_KEY, SOURCE_LABEL } from '../receive/mergeDossier'

export default {
  name: 'VehiclePlanIndex',
  components: { VehiclePlanPanel },
  data() {
    return {
      metaInfo: { tables: [], relations: [], columnLabels: {}, tableCount: 5 },
      queryParams: { billNo: '', fid: null },
      exportTable: 'all',
      exporting: false,
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
    missingBatchItems() {
      return this.batchResults.filter(item => !item.found)
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
      getVehiclePlanMeta().then(res => {
        if (res && res.data) this.metaInfo = res.data
      }).catch(() => {})
    },
    handleQuery() {
      const billNo = (this.queryParams.billNo || '').trim()
      if (!billNo) {
        this.$message.warning('请输入采购车辆计划单号')
        return
      }
      this.loading = true
      this.errorMsg = ''
      if (/[,，]/.test(billNo)) {
        queryVehiclePlanBatch({ billNos: billNo }).then(res => {
          const data = (res && res.data) || {}
          // 兼容汇总对象（新版）与裸数组（旧版后端）
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
      queryVehiclePlan({ billNo, fid: this.queryParams.fid || undefined }).then(res => {
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
    resetQuery() {
      this.queryParams = { billNo: '', fid: null }
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
      const request = isBatch ? exportVehiclePlanBatchExcel(params) : exportVehiclePlanExcel(params)
      const subject = isBatch
        ? '批量' + this.batchResults.length + '单'
        : ((this.singleDossier && this.singleDossier.billNo) || 'query')
      const name = '采购车辆计划_' + subject + '_' +
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
      link.download = '采购车辆计划_' + subject + '.json'
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
.empty-block { margin-top: 18px; background: #fff; border-radius: 8px; padding: 40px 0; }
</style>