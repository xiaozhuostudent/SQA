import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
  // 在开发环境中使用相对路径配合代理，在生产环境中使用绝对路径
  baseURL: import.meta.env.MODE === 'development' ? '/api' : '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    // 添加用户角色到请求头
    if (userStore.userInfo && userStore.userInfo.role) {
      config.headers['X-User-Role'] = userStore.userInfo.role
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    const hasCode = res && Object.prototype.hasOwnProperty.call(res, 'code')
    const hasSuccess = res && Object.prototype.hasOwnProperty.call(res, 'success')

    if (hasCode) {
      if (res.code !== 200) {
        if (res.code === 401) {
          ElMessage.error({
            message: '登录已过期，请重新登录',
            duration: 3000
          })
          const userStore = useUserStore()
          userStore.logout()
          window.location.href = '/login'
          return Promise.reject(new Error('未授权'))
        }
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      return res
    }

    if (hasSuccess) {
      if (res.success) {
        return res
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // 兜底返回，处理下载等没有统一结构的响应
    return res
  },
  error => {
    console.error('响应错误：', error)
    // 只有网络错误才在这里提示
    if (!error.response) {
      ElMessage.error({
        message: '网络连接失败，请检查网络设置',
        duration: 3000
      })
    } else if (error.code === 'ERR_NETWORK') {
      ElMessage.error({
        message: '无法连接到服务器，请检查后端服务是否启动',
        duration: 3000
      })
    }
    return Promise.reject(error)
  }
)

export default request