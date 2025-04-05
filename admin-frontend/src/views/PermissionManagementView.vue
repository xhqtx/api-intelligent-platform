<template>
  <div class="permission-view page-container">
    <!-- 使用PageHeader组件 -->
    <PageHeader title="权限管理">
      <template #actions>
        <el-button v-if="isAdmin" type="success" @click="scanAndUpdatePermissions">
          <el-icon><Refresh /></el-icon>
          <span>一键授权</span>
        </el-button>
        <el-button type="primary" @click="openPermissionModal()">
          <el-icon><Plus /></el-icon>
          <span>新增权限</span>
        </el-button>
      </template>
    </PageHeader>
    
    <!-- 权限列表 -->
    <div class="main-card">
      <DataTable
        :data="permissions"
        :loading="loading"
        :total="pagination.totalElements"
        :page-size="pagination.pageSize"
        :current-page="pagination.currentPage + 1"
        @update:page="(page) => handlePageChange(page - 1)"
        @update:pageSize="(size) => { pagination.pageSize = size; loadPermissions(); }"
      >
        <el-table-column label="序号" width="80">
          <template #default="{ $index }">
            {{ pagination.pageSize * pagination.currentPage + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="权限名称" width="180"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <!-- 操作列使用ActionButton组件 -->
        <template #actions="{ row }">
          <ActionButton type="primary" @click="openPermissionModal(row)">编辑</ActionButton>
          <ActionButton 
            type="danger" 
            :confirm-text="`确认删除权限'${row.name}'吗？此操作不可恢复。`"
            @click="confirmDeletePermission(row)"
          >
            删除
          </ActionButton>
        </template>
      </DataTable>
    </div>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      :title="editingPermission.id ? '编辑权限' : '新增权限'"
      width="500px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form
        ref="permissionFormRef"
        :model="editingPermission"
        :rules="permissionRules"
        label-width="80px"
        class="permission-form"
      >
        <el-form-item label="权限名称" prop="name">
          <el-input
            v-model="editingPermission.name"
            :disabled="!!editingPermission.id"
            placeholder="请输入权限名称"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="form-tip" v-if="editingPermission.id">权限名称不可修改</div>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editingPermission.description"
            type="textarea"
            :rows="3"
            placeholder="请输入权限描述"
            show-word-limit
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermission" :loading="saveLoading">
            {{ editingPermission.id ? '更新' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除确认"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="delete-confirm">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <p>确定要删除权限 "{{ permissionToDelete?.name }}" 吗？</p>
        <p class="warning-text">此操作不可恢复。</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="deletePermission" :loading="deleteLoading">删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, watch, computed } from 'vue';
import { permissionApi } from '@/services/permission';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { useStore } from 'vuex';
import { Plus, Refresh, Key, Warning } from '@element-plus/icons-vue';
import PageHeader from '@/components/PageHeader.vue';
import DataTable from '@/components/DataTable.vue';
import ActionButton from '@/components/ActionButton.vue';

interface Permission {
  id?: number;
  name: string;
  description: string;
  createdAt?: string;
  updatedAt?: string;
}

export default defineComponent({
  name: 'PermissionManagementView',
  components: {
    Plus,
    Refresh,
    Key,
    Warning,
    PageHeader,
    DataTable,
    ActionButton
  },
  setup() {
    const store = useStore();
    const isAdmin = computed(() => store.getters.isAdmin);
    const permissions = ref<Permission[]>([]);
    const loading = ref(true);
    const editingPermission = ref<Permission>({ name: '', description: '' });
    const permissionToDelete = ref<Permission | null>(null);
    const permissionFormRef = ref<FormInstance>();
    const permissionDialogVisible = ref(false);
    const deleteDialogVisible = ref(false);
    const saveLoading = ref(false);
    const deleteLoading = ref(false);

    // 表单验证规则
    const permissionRules = {
      name: [
        { required: true, message: '请输入权限名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      description: [
        { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
      ]
    };

    // 重置表单
    const resetForm = () => {
      if (permissionFormRef.value) {
        permissionFormRef.value.resetFields();
      }
      editingPermission.value = { name: '', description: '' };
    };
    
    // 分页相关
    const pagination = ref({
      currentPage: 0,
      totalPages: 1,
      totalElements: 0,
      pageSize: 10
    });
    
    // 移除不再需要的 ref 和 Modal 变量

    // 加载权限列表
    const loadPermissions = async (page = 0) => {
      loading.value = true;
      try {
        const response = await permissionApi.getAllPermissions({
          page: page,
          size: pagination.value.pageSize,
          sort: 'id,desc'
        });
        if (response.data && response.data.success) {
          const data = response.data.data;
          permissions.value = data.content || [];
          console.log('Loaded permissions:', permissions.value);
          
          // 更新分页信息
          pagination.value = {
            currentPage: data.number !== undefined ? data.number : 0,
            totalPages: data.totalPages || 1,
            totalElements: data.totalElements || 0,
            pageSize: pagination.value.pageSize
          };
          console.log('Updated pagination:', pagination.value);
        } else {
          console.error('API returned an unsuccessful response:', response.data);
          alert('加载权限列表失败：' + (response.data?.message || '未知错误'));
        }
      } catch (error) {
        console.error('Failed to load permissions:', error);
        alert('加载权限列表失败，请检查网络连接并稍后重试');
      } finally {
        loading.value = false;
      }
    };

    // 处理分页变化
    const handlePageChange = (page: number) => {
      if (page < 0 || page >= pagination.value.totalPages) return;
      loadPermissions(page);
    };

    // 打开权限编辑对话框
    const openPermissionModal = async (permission?: Permission) => {
      if (permission) {
        editingPermission.value = { ...permission };
      } else {
        editingPermission.value = { name: '', description: '' };
      }
      permissionDialogVisible.value = true;
    };

    // 保存权限
    const savePermission = async () => {
      if (!permissionFormRef.value) return;
      
      await permissionFormRef.value.validate(async (valid) => {
        if (!valid) {
          ElMessage.warning('请检查表单填写是否正确');
          return;
        }
        
        saveLoading.value = true;
      try {
        if (editingPermission.value.id) {
          // 更新权限，只更新描述
          const response = await permissionApi.updatePermission(
            editingPermission.value.id,
            {
              name: editingPermission.value.name, // 保持原有名称
              description: editingPermission.value.description
            }
          );
          
          if (!response.data.success) {
            alert(response.data.message || '保存权限失败');
            return;
          }
        } else {
          // 创建新权限
          await permissionApi.createPermission({
            name: editingPermission.value.name,
            description: editingPermission.value.description
          });
        }
        
        // 关闭对话框并重新加载权限列表
        permissionDialogVisible.value = false;
        ElMessage.success(editingPermission.value.id ? '权限更新成功' : '权限创建成功');
        await loadPermissions();
      } catch (error) {
        console.error('Failed to save permission:', error);
        ElMessage.error('保存权限失败，请稍后重试');
      } finally {
        saveLoading.value = false;
      }
      });
    };

    // 确认删除权限
    const confirmDeletePermission = (permission: Permission) => {
      permissionToDelete.value = permission;
      deleteDialogVisible.value = true;
    };

    // 删除权限
    const deletePermission = async () => {
      if (!permissionToDelete.value?.id) return;
      deleteLoading.value = true;
      
      try {
        await permissionApi.deletePermission(permissionToDelete.value.id);
        deleteDialogVisible.value = false;
        await loadPermissions();
        ElMessage.success('权限删除成功');
      } catch (error) {
        console.error('Failed to delete permission:', error);
        ElMessage.error('删除权限失败，请稍后重试');
      } finally {
        deleteLoading.value = false;
      }
    };

    // 格式化日期
    const formatDate = (dateString?: string) => {
      if (!dateString) return '-';
      return new Date(dateString).toLocaleString();
    };

    // 扫描并更新权限
    const scanAndUpdatePermissions = async () => {
      try {
        const response = await permissionApi.scanAndUpdatePermissions();
        if (response.data && response.data.success) {
          ElMessage.success('权限扫描和更新成功');
          await loadPermissions(); // 重新加载权限列表
        } else {
          ElMessage.error('权限扫描和更新失败：' + (response.data?.message || '未知错误'));
        }
      } catch (error) {
        console.error('Failed to scan and update permissions:', error);
        ElMessage.error('权限扫描和更新失败，请检查网络连接并稍后重试');
      }
    };

    onMounted(() => {
      // 加载权限列表
      loadPermissions();
    });

// 添加 watch 以响应 permissions 的变化
watch(permissions, (newPermissions) => {
  console.log('Permissions updated:', newPermissions);
}, { deep: true });

// 添加 watch 以响应 pagination 的变化
watch(pagination, (newPagination) => {
  console.log('Pagination updated:', newPagination);
}, { deep: true });

    return {
      permissions,
      loading,
      pagination,
      editingPermission,
      permissionToDelete,
      permissionFormRef,
      permissionDialogVisible,
      deleteDialogVisible,
      saveLoading,
      deleteLoading,
      permissionRules,
      resetForm,
      openPermissionModal,
      savePermission,
      confirmDeletePermission,
      deletePermission,
      formatDate,
      handlePageChange,
      isAdmin,
      scanAndUpdatePermissions
    };
  }
});
</script>

<style lang="scss" scoped>
.permission-view {
  .permission-form {
    padding: 0 20px;
  }

  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }

  .delete-confirm {
    text-align: center;
    padding: 20px 0;

    .warning-icon {
      font-size: 48px;
      color: #e6a23c;
      margin-bottom: 20px;
    }

    p {
      margin: 10px 0;
      font-size: 16px;
    }

    .warning-text {
      color: #f56c6c;
      font-size: 14px;
    }
  }

  .dialog-footer {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }

  :deep(.el-dialog) {
    border-radius: 8px;
    
    .el-dialog__header {
      margin: 0;
      padding: 20px;
      border-bottom: 1px solid #dcdfe6;
    }

    .el-dialog__body {
      padding: 30px 0;
    }

    .el-dialog__footer {
      margin: 0;
      padding: 20px;
      border-top: 1px solid #dcdfe6;
    }
  }
}
</style>