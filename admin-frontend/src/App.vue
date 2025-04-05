<template>
  <el-config-provider :locale="locale" :size="size" :z-index="zIndex">
    <div id="app" :class="{ 'dark-mode': isDarkMode }">
      <el-container v-if="isAuthenticated">
        <el-aside :width="isCollapse ? '64px' : '200px'">
          <div class="sidebar">
            <div class="logo">
              <img src="./assets/logo.svg" alt="Logo" />
              <span v-if="!isCollapse">API管理平台</span>
            </div>
            <el-menu
              :router="true"
              class="nav-menu"
              :default-active="$route.path"
              :collapse="isCollapse"
              @select="handleSelect"
            >
              <el-menu-item index="/">
                <el-icon><House /></el-icon>
                <template #title>首页</template>
              </el-menu-item>
              <el-menu-item index="/users" v-if="isAdmin">
                <el-icon><User /></el-icon>
                <template #title>用户管理</template>
              </el-menu-item>
              <el-menu-item index="/roles" v-if="isAdmin">
                <el-icon><Setting /></el-icon>
                <template #title>角色管理</template>
              </el-menu-item>
              <el-menu-item index="/dict/types" v-if="isAdmin">
                <el-icon><Document /></el-icon>
                <template #title>字典管理</template>
              </el-menu-item>
              <el-menu-item index="/permissions" v-if="isAdmin">
                <el-icon><Key /></el-icon>
                <template #title>权限管理</template>
              </el-menu-item>
              <el-menu-item index="/permission-groups" v-if="isAdmin">
                <el-icon><List /></el-icon>
                <template #title>权限组管理</template>
              </el-menu-item>
              <el-menu-item index="/permission-logs" v-if="isAdmin">
                <el-icon><Document /></el-icon>
                <template #title>权限变更日志</template>
              </el-menu-item>
              <el-menu-item index="/about">
                <el-icon><InfoFilled /></el-icon>
                <template #title>关于</template>
              </el-menu-item>
            </el-menu>
          </div>
        </el-aside>
        <el-container>
          <el-header>
            <div class="header-content">
              <el-button :icon="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'" @click="toggleCollapse" />
              <div class="user-actions">
                <el-switch
                  v-model="isDarkMode"
                  active-text="深色"
                  inactive-text="浅色"
                  @change="toggleDarkMode"
                />
                <el-dropdown @command="handleCommand">
                  <span class="el-dropdown-link">
                    {{ currentUser.username || '管理员' }}
                    <el-icon class="el-icon--right"><arrow-down /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                      <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </el-header>
          <el-main>
            <router-view v-slot="{ Component }">
              <transition name="fade-transform" mode="out-in">
                <keep-alive>
                  <component :is="Component" />
                </keep-alive>
              </transition>
            </router-view>
          </el-main>
        </el-container>
      </el-container>
      <router-view v-else />
    </div>
  </el-config-provider>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElConfigProvider } from 'element-plus'
import { ArrowDown, House, User, InfoFilled, Setting, Document, Key, List } from '@element-plus/icons-vue'
import PageLoading from '@/components/PageLoading.vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import AuthService from '@/services/auth'

export default defineComponent({
  name: 'App',
  components: {
    ElConfigProvider,
    ArrowDown,
    House,
    User,
    InfoFilled,
    Setting,
    Document,
    Key,
    List,
    PageLoading,
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    const isCollapse = ref(false)
    const isDarkMode = ref(false)
    const isPageLoading = ref(false)

    const locale = ref(zhCn)
    const size = ref('default')
    const zIndex = ref(3000)
    const buttonConfig = ref({ autoInsertSpace: true })

    const isAuthenticated = computed(() => store.getters.isAuthenticated)
    const isAdmin = computed(() => store.getters.isAdmin)
    const currentUser = computed(() => store.getters.currentUser)

    onMounted(() => {
      if (AuthService.isAuthenticated()) {
        store.dispatch('fetchCurrentUser')
      }
      // 从本地存储中获取主题设置
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme) {
        isDarkMode.value = savedTheme === 'dark'
        applyTheme(isDarkMode.value)
      }
    })

    const toggleCollapse = () => {
      isCollapse.value = !isCollapse.value
    }

    const toggleDarkMode = (value: boolean) => {
      isDarkMode.value = value
      applyTheme(value)
      localStorage.setItem('theme', value ? 'dark' : 'light')
    }

    const applyTheme = (isDark: boolean) => {
      document.body.classList.toggle('dark-mode', isDark)
    }

    const handleSelect = (index: string) => {
      console.log('Selected menu item:', index)
    }

    const handleCommand = async (command: string) => {
      if (command === 'profile') {
        router.push('/profile')
      } else if (command === 'logout') {
        try {
          const response = await AuthService.logout()
          if (response.success) {
            await store.dispatch('logout')
            ElMessage.success('退出登录成功')
            router.push('/login')
          } else {
            ElMessage.error(response.message || '退出登录失败')
          }
        } catch (error) {
          ElMessage.error('退出登录失败')
        }
      }
    }

    return {
      router,
      store,
      isAuthenticated,
      isAdmin,
      currentUser,
      isCollapse,
      isDarkMode,
      locale,
      size,
      zIndex,
      buttonConfig,
      isPageLoading,
      toggleCollapse,
      toggleDarkMode,
      handleSelect,
      handleCommand,
    }
  },
})
</script>

<style lang="scss">
@import '@/styles/variables.scss';

:root {
  --primary-color: #{$primary-color};
  --success-color: #{$success-color};
  --warning-color: #{$warning-color};
  --danger-color: #{$danger-color};
  --info-color: #{$info-color};
  --background-color: #{$bg-color};
  --text-color: #{$text-color};
  --text-color-secondary: #{$text-color-secondary};
  --border-color: #{$border-color};
  --menu-bg-color: #304156;
  --menu-text-color: #bfcbd9;
  --menu-active-color: var(--primary-color);
}

.dark-mode {
  --primary-color: #{lighten($primary-color, 10%)};
  --success-color: #{lighten($success-color, 10%)};
  --warning-color: #{lighten($warning-color, 10%)};
  --danger-color: #{lighten($danger-color, 10%)};
  --info-color: #{lighten($info-color, 10%)};
  --background-color: #1f1f1f;
  --text-color: #ffffff;
  --text-color-secondary: #a0a0a0;
  --border-color: #4c4c4c;
  --menu-bg-color: #1f1f1f;
  --menu-text-color: #bfcbd9;
  --menu-active-color: var(--primary-color);
}

#app {
  font-family: $font-family;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100vh;
  color: var(--text-color);
  background-color: var(--background-color);
  transition: $transition-base;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: var(--menu-bg-color);
  color: var(--menu-text-color);
  transition: width $transition-base;
  box-shadow: $box-shadow-light;
}

.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  .logo {
    height: $header-height;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #1a1a1a; // 替换为固定颜色而不是使用darken函数
    padding: 0 16px;
    img {
      width: 32px;
      height: 32px;
      margin-right: 8px;
    }
    span {
      font-size: $font-size-large;
      font-weight: bold;
      color: var(--menu-text-color);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  .nav-menu {
    border-right: none;
    flex: 1;
    overflow-y: auto;
    &:not(.el-menu--collapse) {
      width: $sidebar-width;
    }
  }
}

.el-menu {
  background-color: var(--menu-bg-color);
  .el-menu-item, .el-submenu__title {
    height: 50px;
    line-height: 50px;
    &:hover, &:focus {
      background-color: rgba(255, 255, 255, 0.05);
    }
  }
  .el-menu-item.is-active {
    background-color: var(--primary-color);
    color: #fff;
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background-color: var(--menu-active-color);
    }
  }
}

.el-header {
  background-color: var(--background-color);
  border-bottom: 1px solid var(--border-color);
  padding: 0;
  box-shadow: $box-shadow-light;
}

.header-content {
  height: $header-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.breadcrumb {
  margin-left: 20px;
}

.user-actions {
  display: flex;
  align-items: center;
  > * {
    margin-left: 15px;
  }
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: var(--text-color);
}

.el-main {
  padding: 20px;
  overflow-x: hidden;
}

.app-container {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: $box-shadow-light;
  margin-bottom: 20px;
}

// 卡片式布局
.dashboard-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  .dashboard-card {
    background-color: #fff;
    border-radius: 4px;
    padding: 20px;
    box-shadow: $box-shadow-light;
  }
}

// 动画过渡效果
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.5s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

// 响应式设计
@media (max-width: 768px) {
  .el-aside {
    position: absolute;
    z-index: 999;
    height: 100%;
  }
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 10px;
  }
  .user-actions {
    margin-top: 10px;
  }
}
</style>