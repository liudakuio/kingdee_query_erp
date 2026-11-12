<template>
  <div class="dynamic-table">
    <el-table
      v-if="rows && rows.length"
      :data="rows"
      border
      stripe
      size="mini"
      :max-height="maxHeight"
      style="width: 100%"
    >
      <el-table-column type="index" label="#" width="50" align="center" />
      <el-table-column
        v-for="col in tableColumns"
        :key="col"
        :prop="col"
        :label="labelOf(col)"
        :min-width="widthOf(col)"
        :render-header="renderHeader"
        show-overflow-tooltip
      >
        <template slot-scope="scope">
          <span :class="{ 'cell-empty': isEmpty(scope.row[col]) }">{{ display(scope.row[col]) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else description="该表无关联数据" :image-size="56" />
  </div>
</template>

<script>
// 金蝶表字段随版本/二开变化，列名不写死：由后端 SELECT * 返回的动态列生成表格
export default {
  name: 'DynamicTable',
  props: {
    rows: { type: Array, default: () => [] },
    columns: { type: Array, default: () => [] },
    labels: { type: Object, default: () => ({}) },
    maxHeight: { type: [Number, String], default: 380 }
  },
  computed: {
    tableColumns() {
      if (this.columns && this.columns.length) return this.columns
      const cols = []
      ;(this.rows || []).forEach(row => {
        Object.keys(row || {}).forEach(key => {
          if (cols.indexOf(key) === -1) cols.push(key)
        })
      })
      return cols
    }
  },
  methods: {
    labelOf(col) {
      return this.labels[col] || col
    },
    renderHeader(h, { column }) {
      return h('span', { attrs: { title: column.property } }, [column.label])
    },
    widthOf(col) {
      if (/url|remark|name|materialname|attachname|filename|supplier/i.test(col)) return 180
      if (/^(fseq|fqty|fbillstatus|fpkid|fk_usp_checkboxfield|ftaxrate)$/i.test(col)) return 105
      return 145
    },
    isEmpty(value) {
      return value === null || value === undefined || value === ''
    },
    display(value) {
      if (this.isEmpty(value)) return '—'
      if (typeof value === 'object') return JSON.stringify(value)
      return String(value)
    }
  }
}
</script>

<style scoped>
.dynamic-table { width: 100%; }
.dynamic-table /deep/ .el-table th { background: #f7f9fc; color: #4a5568; }
</style>
