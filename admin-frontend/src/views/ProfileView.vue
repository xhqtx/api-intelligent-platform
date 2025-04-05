<template>
  <div class="profile-view">
    <div class="page-header">
      <h1>个人信息</h1>
    </div>
    <el-card class="profile-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      <div v-else class="profile-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag 
              v-for="role in currentUser.roles" 
              :key="role.id" 
              :type="getRoleType(role.name)"
              class="role-tag"
            >
              {{ formatRoleName(role.name) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="action-buttons">
          <el-button type="primary" :icon="Edit">修改信息</el-button>
          <el-button type="warning" :icon="Lock">修改密码</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue';
import { useStore } from 'vuex';

export default defineComponent({
  name: 'ProfileView',
  setup() {
    const store = useStore();
    
    // 从store获取用户信息
    const currentUser = computed(() => store.getters.currentUser);
    const loading = computed(() => currentUser.value.loading);
    
    // 确保用户信息已加载
    if (!currentUser.value.id && !loading.value) {
      store.dispatch('fetchCurrentUser');
    }
    
    // 角色格式化
    const formatRoleName = (roleName: string) => {
      if (roleName.startsWith('ROLE_')) {
        return roleName.replace('ROLE_', '');
      }
      return roleName;
    };

    // 根据角色返回不同的标签类型
    const getRoleType = (roleName: string) => {
      const roleLower = roleName.toLowerCase();
      if (roleLower.includes('admin')) {
        return 'danger';
      } else if (roleLower.includes('manager')) {
        return 'warning';
      } else if (roleLower.includes('user')) {
        return 'success';
      }
      return 'info';
    };
    
    return {
      currentUser,
      loading,
      formatRoleName,
      getRoleType
    };
  }
});
</script>

<style lang="scss" scoped>
.profile-view {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h1 {
      margin: 0;
      color: var(--text-color);
    }
  }

  .profile-card {
    max-width: 800px;
    margin: 0 auto;
    border-radius: 4px;
    box-shadow: var(--box-shadow-light);

    .loading-container {
      padding: 20px;
    }

    .profile-info {
      padding: 20px 0;

      .el-descriptions {
        margin-bottom: 20px;
      }
    }

    .action-buttons {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }

  .role-tag {
    margin-right: 5px;
  }
}

// 响应式布局
@media (max-width: 768px) {
  .profile-view {
    .profile-card {
      .action-buttons {
        flex-direction: column;
        align-items: stretch;
      }
    }
  }
}
</style>