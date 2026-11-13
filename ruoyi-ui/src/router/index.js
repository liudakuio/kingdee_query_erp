import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

// 独立运行脚手架使用的静态路由；接入正式若依时由后端菜单动态生成，无需此文件
const routes = [
  { path: '/', redirect: '/kingdee/receive' },
  {
    path: '/kingdee/receive',
    name: 'PurReceive',
    component: () => import('@/views/kingdee/receive/index.vue'),
    meta: { title: '采购收货单查询', icon: 'el-icon-document' }
  },
  {
    path: '/kingdee/vehicleplan',
    name: 'VehiclePlan',
    component: () => import('@/views/kingdee/vehicleplan/index.vue'),
    meta: { title: '采购车辆计划查询', icon: 'el-icon-truck' }
  },
  { path: '*', redirect: '/kingdee/receive' }
]

export default new VueRouter({
  mode: 'hash',
  routes
})
