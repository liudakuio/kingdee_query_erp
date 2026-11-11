import axios from 'axios'
import { Message } from 'element-ui'

// 与若依 ruoyi-ui/src/utils/request.js 的约定一致：拦截器直接返回 res.data，业务码 200 视为成功
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || '',
  timeout: 30000
})

service.interceptors.response.use(
  response => {
    const body = response.data
    if (!body || typeof body.code === 'undefined') {
      return body
    }
    if (body.code === 200) {
      return body
    }
    Message.error(body.msg || '请求失败')
    return Promise.reject(new Error(body.msg || '请求失败'))
  },
  error => {
    Message.error(error.message || '网络异常，请确认后端服务已启动')
    return Promise.reject(error)
  }
)

export default service
