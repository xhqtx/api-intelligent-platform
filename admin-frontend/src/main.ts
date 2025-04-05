import { createApp } from 'vue'

// 在全局环境中定义特性标志
// 这会在打包时被webpack处理
if (typeof window !== 'undefined') {
  window.__VUE_PROD_HYDRATION_MISMATCH_DETAILS__ = true;
}
import * as PopperFix from './utils/popper-fix'
import { placements } from './utils/popper-fix'
import { setupResizeObserverErrorHandling } from './utils/resize-observer-fix'

// 设置 ResizeObserver 错误处理
setupResizeObserverErrorHandling()

// 替换全局的 @popperjs/core
window.Popper = PopperFix.default
window.createPopper = PopperFix.createPopper
window.placements = placements

import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import AuthService from '@/services/auth'

// 导入自定义样式
import '@/styles/index.scss'
import './styles/dark.scss'
import '@/styles/variables.scss'
import '@/styles/global.scss'

const app = createApp(App)

// 初始化应用
const initApp = async () => {
  if (AuthService.isAuthenticated()) {
    await store.dispatch('fetchCurrentUser')
  }
  
  // 注册所有图标
  for (const [key, component] of Object.entries(ElementPlusIcons)) {
    app.component(key, component)
  }

  // 全局错误处理
app.config.errorHandler = (err, vm, info) => {
  // 忽略 ResizeObserver 循环错误，这通常不会影响应用功能
  if (err instanceof Error && err.message.includes('ResizeObserver loop')) {
    console.debug('Ignored ResizeObserver error:', err.message)
    return
  }
  console.error('Global Error:', err)
  console.error('Error Info:', info)
}

// 处理未捕获的错误和 Promise rejection
window.addEventListener('error', (event) => {
  if (event.error && event.error.message && event.error.message.includes('ResizeObserver loop')) {
    event.preventDefault()
    event.stopPropagation()
    return true // 阻止错误继续传播
  }
}, true) // 使用捕获阶段处理事件

window.addEventListener('unhandledrejection', (event) => {
  if (event.reason && event.reason.message && event.reason.message.includes('ResizeObserver loop')) {
    event.preventDefault()
    event.stopPropagation()
    return true // 阻止错误继续传播
  }
}, true) // 使用捕获阶段处理事件

// 修复 webpack-dev-server 客户端的覆盖错误处理
// 这是一个临时解决方案，专门针对开发环境
if (process.env.NODE_ENV === 'development') {
  const originalConsoleError = console.error;
  console.error = (...args) => {
    if (args.length > 0 && 
        typeof args[0] === 'string' && 
        args[0].includes('ResizeObserver loop')) {
      console.debug('Ignored ResizeObserver error:', args[0]);
      return; // 忽略 ResizeObserver 相关错误
    }
    originalConsoleError.apply(console, args);
  };

  // 覆盖 webpack-dev-server 的错误处理
  const originalHandleError = window.onerror;
  window.onerror = (message, source, lineno, colno, error) => {
    if (message && typeof message === 'string' && message.includes('ResizeObserver loop')) {
      console.debug('Ignored ResizeObserver error:', message);
      return true; // 阻止错误继续传播
    }
    return originalHandleError ? originalHandleError(message, source, lineno, colno, error) : false;
  };
}

  // 全局属性
  app.config.globalProperties.$isDarkMode = false

  app.use(store)
     .use(router)
     .use(ElementPlus, {
       size: 'default',
       zIndex: 3000,
     })
     .mount('#app')
}

initApp()