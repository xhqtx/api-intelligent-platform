import axios, { AxiosInstance } from 'axios'
import { ElMessage } from 'element-plus'
import AuthService from './auth'

const request: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080', // 网关基础地址
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = AuthService.getToken()
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          AuthService.logout()
          break
        case 403:
          ElMessage.error('拒绝访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器端错误')
          break
        default:
          ElMessage.error(`连接错误 ${error.response.status}`)
      }
    } else {
      ElMessage.error('网络连接异常,请稍后再试!')
    }
    return Promise.reject(error)
  }
)

export default request