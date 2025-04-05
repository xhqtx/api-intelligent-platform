import axios, { AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

// 扩展AxiosRequestConfig接口，添加metadata属性
declare module 'axios' {
  interface AxiosRequestConfig {
    metadata?: {
      startTime: number;
    };
  }
}

// 创建axios实例
const api = axios.create({
  baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 日志工具类
const Logger = {
  // 打印请求日志
  requestLog: (config: AxiosRequestConfig) => {
    const { method, url, params, data, headers, baseURL } = config;
    const fullUrl = `${baseURL}${url}`;
    
    console.group(`🚀 发起请求: ${method?.toUpperCase()} ${url}`);
    console.log('完整请求地址:', fullUrl);
    console.log('请求头:', headers);
    
    if (params) {
      console.log('URL参数:', params);
    }
    
    if (data) {
      console.log('请求体:', data);
    }
    
    console.groupEnd();
  },

  // 打印响应日志
  responseLog: (response: AxiosResponse) => {
    const { config, status, data: responseData, headers } = response;
    const { method, url } = config;
    const timestamp = new Date().toISOString();
    
    console.group(`✅ 请求成功: ${method?.toUpperCase()} ${url}`);
    console.log('时间:', timestamp);
    console.log('状态码:', status);
    console.log('响应头:', headers);
    
    console.group('响应数据详情:');
    if (typeof responseData === 'object') {
      if ('code' in responseData) {
        console.log('业务状态码:', responseData.code);
      }
      if ('message' in responseData) {
        console.log('响应消息:', responseData.message);
      }
      if ('data' in responseData) {
        console.log('业务数据:');
        console.dir(responseData.data, { depth: null, colors: true });
      } else {
        console.log('完整响应:');
        console.dir(responseData, { depth: null, colors: true });
      }
    } else {
      console.log('响应数据:', responseData);
    }
    console.groupEnd();
    
    // 记录响应时间
    const responseTime = Date.now() - (config.metadata?.startTime || Date.now());
    console.log('响应时间:', `${responseTime}ms`);
    
    console.groupEnd();
  },

  // 打印错误日志
  errorLog: (error: AxiosError) => {
    const { config, response } = error;
    
    console.group('❌ 请求失败');
    if (config) {
      const { method, url, params, data } = config;
      console.log('请求信息:', {
        method: method?.toUpperCase(),
        url,
        params,
        data
      });
    }
    
    if (response) {
      const { status, data: responseData } = response;
      console.log('错误状态码:', status);
      console.log('错误响应:', responseData);
    } else {
      console.log('网络错误:', error.message);
    }
    
    console.groupEnd();
  }
};

// 请求拦截器
api.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 从localStorage获取token
    const token = localStorage.getItem('admin_token');
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // 添加请求开始时间
    config.metadata = { startTime: Date.now() };
    
    // 打印请求日志
    Logger.requestLog(config);
    
    return config;
  },
  error => {
    Logger.errorLog(error);
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // 打印响应日志
    Logger.responseLog(response);
    return response;
  },
  error => {
    // 打印错误日志
    Logger.errorLog(error);
    
    // 处理401错误（未授权）
    if (error.response && error.response.status === 401) {
      // 清除token并跳转到登录页
      localStorage.removeItem('admin_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 环境判断，只在开发环境下打印日志
if (process.env.NODE_ENV === 'production') {
  /* eslint-disable @typescript-eslint/no-empty-function */
  console.group = () => {};
  console.groupEnd = () => {};
  console.log = () => {};
  /* eslint-enable @typescript-eslint/no-empty-function */
}

// 获取当前用户信息
export const getCurrentUser = async () => {
  try {
    const response = await api.get('/auth/api/auth/me');
    return response.data;
  } catch (error) {
    console.error('获取当前用户信息失败:', error);
    throw error;
  }
};

// 获取审计日志列表
export const getAuditLogs = async (params: any) => {
  try {
    const response = await api.get('/api/audit-logs', { params });
    return response;
  } catch (error) {
    console.error('获取审计日志失败:', error);
    throw error;
  }
};

// 导出审计日志
export const exportAuditLogs = async (params: any) => {
  try {
    const response = await api.get('/api/audit-logs/export', { 
      params,
      responseType: 'blob'
    });
    return response;
  } catch (error) {
    console.error('导出审计日志失败:', error);
    throw error;
  }
};

export default api;