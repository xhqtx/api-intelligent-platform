import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import AuthService from '@/services/auth'
import { ElLoading } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置 NProgress
NProgress.configure({ 
  easing: 'ease', 
  speed: 500, 
  showSpinner: false,
  trickleSpeed: 200,
  minimum: 0.3
})

// 使用异步组件实现懒加载
const loadView = (view: string) => {
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '页面加载中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  return new Promise((resolve) => {
    // 模拟网络延迟，实际项目中可以删除此延迟
    const component = () => import(`../views/${view}.vue`)
    
    // 添加短暂延迟以确保骨架屏能够显示
    setTimeout(() => {
      loadingInstance.close()
      resolve(component())
    }, 300)
  })
}

const routes: Array<RouteRecordRaw> = [
  {
    path: '/dict/types',
    name: 'DictType',
    component: () => loadView('DictTypeView'),
    meta: { requiresAuth: true, requiresAdmin: true, keepAlive: true }
  },
  {
    path: '/dict/data/:typeCode',
    name: 'DictData',
    component: () => loadView('DictDataView'),
    meta: { requiresAuth: true, requiresAdmin: true, keepAlive: false }
  },
  {
    path: '/roles',
    name: 'roles',
    component: () => loadView('RoleManagementView'),
    meta: { requiresAuth: true, requiresAdmin: true, keepAlive: true }
  },
  {
    path: '/users',
    name: 'users',
    component: () => loadView('UserManagementView'),
    meta: { requiresAuth: true, requiresAdmin: true, keepAlive: true }
  },
  {
    path: '/login',
    name: 'login',
    component: () => loadView('LoginView'),
    meta: { requiresAuth: false, keepAlive: false }
  },
  {
    path: '/register',
    name: 'register',
    component: () => loadView('RegisterView'),
    meta: { requiresAuth: false, keepAlive: false }
  },
  {
    path: '/',
    name: 'home',
    component: () => loadView('HomeView'),
    meta: { keepAlive: true }
  },
  {
    path: '/about',
    name: 'about',
    component: () => loadView('AboutView'),
    meta: { keepAlive: true }
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => loadView('ProfileView'),
    meta: { requiresAuth: true, keepAlive: false }
  },
  {
    path: '/permissions',
    name: 'permissions',
    component: () => loadView('PermissionManagementView'),
    meta: { requiresAuth: true, requiresAdmin: true, keepAlive: true }
  },
  {
    path: '/permission-groups',
    name: 'permissionGroups',
    component: () => loadView('PermissionGroupManagementView'),
    meta: { requiresAuth: true, requiresAdmin: true, keepAlive: true }
  },
  {
    path: '/audit-logs',
    name: 'auditLogs',
    component: () => loadView('AuditLogView'),
    meta: { requiresAuth: true, requiresAdmin: true, keepAlive: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth !== false);
  
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin);
  const isAdmin = AuthService.hasRole('ADMIN');

  console.log('Route guard:', { to: to.path, requiresAuth, requiresAdmin, isAdmin, isAuthenticated: AuthService.isAuthenticated() });

  if (requiresAdmin && !isAdmin) {
    // 需要管理员权限但用户不是管理员
    console.log('Access denied: Admin role required');
    next('/');
  } else if (requiresAuth && !AuthService.isAuthenticated()) {
    // 需要认证但未登录，重定向到登录页
    console.log('Authentication required, redirecting to login');
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    });
  } else if (to.path === '/login' && AuthService.isAuthenticated()) {
    // 已登录用户访问登录页，重定向到首页
    console.log('Already authenticated, redirecting to home');
    next('/');
  } else {
    console.log('Access granted');
    next();
  }
});

export default router