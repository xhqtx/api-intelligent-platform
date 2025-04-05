<template>
  <div class="permission-group-management page-container">
    <!-- 使用PageHeader组件 -->
    <PageHeader title="权限组管理">
      <template #actions>
        <el-button type="primary" @click="openGroupModal">
          <el-icon><Plus /></el-icon>
          <span>新增权限组</span>
        </el-button>
      </template>
    </PageHeader>

    <!-- 使用DataTable组件 -->
    <div class="main-card">
      <DataTable
        :data="permissionGroups"
        :loading="loading"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @update:page="handlePageChange"
        @update:page-size="handleSizeChange"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="权限组名称" width="180" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="权限数量" width="100">
          <template #default="{ row }">
            {{ row.permissions?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <!-- 操作列使用ActionButton组件 -->
        <template #actions="{ row }">
          <el-button-group>
            <el-tooltip content="编辑权限组" placement="top" :show-after="500">
              <ActionButton type="primary" icon="Edit" @click="openGroupModal(row)"></ActionButton>
            </el-tooltip>
            <el-tooltip content="管理权限" placement="top" :show-after="500">
              <ActionButton type="info" icon="Setting" @click="openPermissionsModal(row)"></ActionButton>
            </el-tooltip>
            <el-tooltip content="删除权限组" placement="top" :show-after="500">
              <ActionButton 
                type="danger" 
                icon="Delete"
                :confirm-text="`确认删除权限组'${row.name}'吗？此操作不可恢复。`"
                :confirm-type="'error'"
                @click="deleteGroup(row)"
              ></ActionButton>
            </el-tooltip>
          </el-button-group>
        </template>
      </DataTable>
    </div>

    <!-- 权限组编辑对话框 -->
    <el-dialog
      :title="editingGroup.id ? '编辑权限组' : '新增权限组'"
      v-model="groupDialogVisible"
      width="500px"
    >
      <el-form :model="editingGroup" :rules="rules" ref="groupFormRef" label-width="100px">
        <el-form-item label="权限组名称" prop="name">
          <el-input v-model="editingGroup.name" placeholder="请输入权限组名称"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editingGroup.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="groupDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveGroup">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限管理对话框 -->
    <el-dialog
      :title="`管理权限组 '${currentGroup?.name}' 的权限`"
      v-model="permissionsDialogVisible"
      @closed="closePermissionsModal"
      width="700px"
    >
      <div v-if="loadingPermissions" class="text-center py-4">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else>
        <el-input
          v-model="permissionSearchQuery"
          placeholder="搜索权限..."
          prefix-icon="el-icon-search"
          clearable
          class="mb-3"
        />
        
        <div class="permission-list">
          <el-checkbox-group v-model="selectedPermissionIds">
            <div v-for="permission in filteredPermissions" :key="permission.id">
              <el-checkbox :label="permission.id">
                {{ permission.name }} - {{ permission.description }}
              </el-checkbox>
            </div>
          </el-checkbox-group>
          <div v-if="loadingPermissions" class="text-center mt-4">
            <el-icon class="is-loading"><Loading /></el-icon> 加载中...
          </div>
        </div>
        
        <div v-if="filteredPermissions.length === 0" class="el-alert el-alert--info">
          没有找到匹配的权限
        </div>
        
        <div class="mt-3">
          <el-button @click="selectAllPermissions" size="small">全选</el-button>
          <el-button @click="deselectAllPermissions" size="small">取消全选</el-button>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermissions">保存权限设置</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { permissionGroupApi, permissionApi } from '@/services/permission';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Setting, Delete, Loading } from '@element-plus/icons-vue';
import PageHeader from '@/components/PageHeader.vue';
import DataTable from '@/components/DataTable.vue';
import ActionButton from '@/components/ActionButton.vue';
import { debounce } from 'lodash';

interface Permission {
  id: number;
  name: string;
  description: string;
  createdAt?: string;
  updatedAt?: string;
}

interface PermissionGroup {
  id?: number;
  name: string;
  description: string;
  permissions?: Permission[];
  createdAt?: string;
  updatedAt?: string;
}

// 直接定义响应式变量和函数
const permissionGroups = ref<PermissionGroup[]>([]);
const allPermissions = ref<Permission[]>([]);
const loading = ref(true);
const loadingPermissions = ref(false);
const editingGroup = ref<PermissionGroup>({ name: '', description: '' });
const currentGroup = ref<PermissionGroup | null>(null);
const groupToDelete = ref<PermissionGroup | null>(null);
const selectedPermissionIds = ref<number[]>([]);
const permissionSearchQuery = ref('');

const groupDialogVisible = ref(false);
const permissionsDialogVisible = ref(false);
const groupFormRef = ref();

const currentPage = ref(1);
const pageSize = ref(20);
const total = ref(0); // 权限组总数
const totalPermissions = ref(0);
const hasMorePermissions = ref(true);
    
    // 表单验证规则
    const rules = {
      name: [
        { required: true, message: '请输入权限组名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      description: [
        { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
      ]
    };

    // 过滤权限列表
    const filteredPermissions = computed(() => {
      if (!permissionSearchQuery.value) {
        return allPermissions.value;
      }
      
      const query = permissionSearchQuery.value.toLowerCase();
      return allPermissions.value.filter(permission => 
        permission.name.toLowerCase().includes(query) || 
        permission.description.toLowerCase().includes(query)
      );
    });

    // 加载权限组列表
    const loadPermissionGroups = async () => {
  loading.value = true;
  try {
    const response = await permissionGroupApi.getAllPermissionGroups({
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value,
      sort: 'createdAt,desc'
    });
    console.log('Permission groups response:', response);
    if (response.data?.success && response.data?.data) {
      const { content, totalElements, totalPages } = response.data.data;
      if (Array.isArray(content)) {
        permissionGroups.value = content;
        total.value = totalElements;
        // 更新总页数，如果后端提供了这个信息
        // totalPages.value = totalPages;
      } else {
        console.error('Invalid content structure:', content);
        ElMessage.error('数据格式异常');
      }
    } else {
      console.error('Unexpected response structure:', response);
      ElMessage.error('数据结构异常');
    }
  } catch (error) {
    console.error('Failed to load permission groups:', error);
    ElMessage.error('加载权限组列表失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

    // 加载权限
    const loadPermissions = async (page: number, size: number) => {
      if (!hasMorePermissions.value) return;
      loadingPermissions.value = true;
      try {
        const response = await permissionApi.getAllPermissions({ page, size });
        const newPermissions = response.data.data.content || [];
        allPermissions.value = [...allPermissions.value, ...newPermissions];
        totalPermissions.value = response.data.data.totalElements;
        hasMorePermissions.value = allPermissions.value.length < totalPermissions.value;
        currentPage.value = page;
      } catch (error) {
        console.error('Failed to load permissions:', error);
        ElMessage.error('加载权限列表失败，请稍后重试');
      } finally {
        loadingPermissions.value = false;
      }
    };

    // 初始加载权限
    const initLoadPermissions = () => {
      allPermissions.value = [];
      currentPage.value = 1;
      hasMorePermissions.value = true;
      loadPermissions(currentPage.value, pageSize.value);
    };

    // 滚动加载更多权限
    const loadMorePermissions = () => {
      if (loadingPermissions.value || !hasMorePermissions.value) return;
      loadPermissions(currentPage.value + 1, pageSize.value);
    };

    // 使用防抖处理滚动事件
    const debouncedLoadMore = debounce(() => {
      const container = document.querySelector('.permission-list');
      if (container) {
        const { scrollTop, scrollHeight, clientHeight } = container;
        if (scrollTop + clientHeight >= scrollHeight - 50) {
          loadMorePermissions();
        }
      }
    }, 200);

    // 打开权限组编辑对话框
    const openGroupModal = (group?: PermissionGroup) => {
      if (group) {
        editingGroup.value = { ...group };
      } else {
        editingGroup.value = { name: '', description: '' };
      }
      groupDialogVisible.value = true;
    };

    // 打开权限管理模态框
    const openPermissionsModal = async (group: PermissionGroup) => {
      currentGroup.value = group;
      
      // 重置并加载权限
      initLoadPermissions();
      
      // 设置已选权限
      selectedPermissionIds.value = group.permissions?.map(p => p.id) || [];
      
      permissionsDialogVisible.value = true;
      
      // 在下一个 tick 添加滚动监听，延迟一点以避免 ResizeObserver 错误
      setTimeout(() => {
        nextTick(() => {
          const container = document.querySelector('.permission-list');
          if (container) {
            container.addEventListener('scroll', debouncedLoadMore);
          }
        });
      }, 100);
    };

    // 保存权限组
    const saveGroup = async () => {
      if (!groupFormRef.value) return;
      
      await groupFormRef.value.validate(async (valid: boolean) => {
        if (!valid) return;
        
        try {
          if (editingGroup.value.id) {
            // 更新权限组
            await permissionGroupApi.updatePermissionGroup(
              editingGroup.value.id,
              {
                name: editingGroup.value.name,
                description: editingGroup.value.description,
                permissionIds: editingGroup.value.permissions?.map(p => p.id) || []
              }
            );
          } else {
            // 创建新权限组
            await permissionGroupApi.createPermissionGroup({
              name: editingGroup.value.name,
              description: editingGroup.value.description,
              permissionIds: []
            });
          }
          
          // 关闭对话框并重新加载权限组列表
          groupDialogVisible.value = false;
          await loadPermissionGroups();
          ElMessage.success(editingGroup.value.id ? '更新成功' : '创建成功');
        } catch (error) {
          console.error('Failed to save permission group:', error);
          ElMessage.error('保存权限组失败，请稍后重试');
        }
      });
    };

    // 保存权限设置
    const savePermissions = async () => {
      if (!currentGroup.value?.id) return;
      
      try {
        // 直接使用更新权限组权限的API，一次性更新所有权限
        await permissionGroupApi.updatePermissionGroupPermissions(
          currentGroup.value.id, 
          selectedPermissionIds.value
        );
        
        // 关闭模态框并重新加载权限组列表
        permissionsDialogVisible.value = false;
        await loadPermissionGroups();
        ElMessage.success('权限设置已保存');
      } catch (error) {
        console.error('Failed to update permissions:', error);
        ElMessage.error('更新权限设置失败，请稍后重试');
      }
    };

    // 确认删除权限组
    const confirmDeleteGroup = (group: PermissionGroup) => {
      groupToDelete.value = group;
      ElMessageBox.confirm(
        `确认删除权限组"${group.name}"吗？此操作不可恢复。`,
        '确认删除',
        {
          confirmButtonText: '删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        deleteGroup(group);
      }).catch(() => {
        // 用户取消删除操作
      });
    };

    // 删除权限组
    const deleteGroup = async (group: PermissionGroup) => {
      if (!group.id) return;
      
      try {
        await permissionGroupApi.deletePermissionGroup(group.id);
        await loadPermissionGroups();
        ElMessage.success('删除成功');
      } catch (error) {
        console.error('Failed to delete permission group:', error);
        ElMessage.error('删除权限组失败，请稍后重试');
      }
    };

    // 格式化日期
    const formatDate = (dateString?: string) => {
      if (!dateString) return '-';
      return new Date(dateString).toLocaleString();
    };

onMounted(() => {
  loadPermissionGroups();
  initLoadPermissions(); // 在组件挂载时初始化加载权限
});

// 关闭权限管理模态框
const closePermissionsModal = () => {
  currentGroup.value = null;
  selectedPermissionIds.value = [];
  permissionsDialogVisible.value = false;
  // 移除滚动监听
  const container = document.querySelector('.permission-list');
  if (container) {
    container.removeEventListener('scroll', debouncedLoadMore);
  }
};

// 全选权限
const selectAllPermissions = () => {
  selectedPermissionIds.value = allPermissions.value.map(p => p.id);
};

// 取消全选权限
const deselectAllPermissions = () => {
  selectedPermissionIds.value = [];
};

// 在<script setup>中不需要显式返回，所有顶级变量和函数都会自动暴露给模板
// The closing bracket and parenthesis for defineComponent have been removed
</script>

<style scoped>
.permission-group-management {
  padding: 20px;
}

.permission-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 0.25rem;
  padding: 10px;
}
</style>