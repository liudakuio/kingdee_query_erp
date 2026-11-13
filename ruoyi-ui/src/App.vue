<template>
  <div id="app">
    <el-container class="layout">
      <el-aside width="216px" class="sidebar">
        <div class="brand">
          <div class="brand-logo">K</div>
          <div class="brand-text">金蝶 ERP 查询</div>
        </div>
        <el-menu
          router
          :default-active="$route.path"
          background-color="#1f2d3d"
          text-color="#bfcbd9"
          active-text-color="#ffffff"
        >
          <el-submenu index="kingdee">
            <template slot="title"><i class="el-icon-s-grid"></i><span>金蝶 ERP</span></template>
            <el-menu-item index="/kingdee/receive">
              <i class="el-icon-document"></i><span>采购收货单查询</span>
            </el-menu-item>
            <el-menu-item index="/kingdee/vehicleplan">
              <i class="el-icon-truck"></i><span>采购车辆计划查询</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header height="58px" class="navbar">
          <div class="crumb"><i class="el-icon-location-outline"></i> 金蝶 ERP / <b>{{ $route.meta.title || '查询' }}</b></div>
          <div class="nav-right">
            <el-tag size="mini" type="success" effect="plain">只读查询</el-tag>
            <span class="nav-user"><i class="el-icon-user-solid"></i> 管理员</span>
          </div>
        </el-header>
        <el-main class="main"><router-view /></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style scoped>
.layout { height: 100%; }
.sidebar { background: #1f2d3d; overflow-x: hidden; }
.sidebar .el-menu { border-right: none; }
.brand { height: 58px; display: flex; align-items: center; padding: 0 16px; color: #fff; border-bottom: 1px solid #2b3c50; }
.brand-logo { width: 30px; height: 30px; border-radius: 8px; background: #409eff; display: flex; align-items: center; justify-content: center; font-weight: 700; margin-right: 10px; }
.brand-text { font-size: 15px; font-weight: 600; letter-spacing: 1px; }
.navbar { background: #fff; border-bottom: 1px solid #e8edf3; display: flex; align-items: center; justify-content: space-between; }
.crumb { color: #606266; font-size: 14px; }
.crumb b { color: #303133; }
.nav-right { display: flex; align-items: center; gap: 16px; }
.nav-user { color: #606266; font-size: 13px; }
.main { background: #f4f6f9; padding: 18px; overflow-y: auto; }
</style>
