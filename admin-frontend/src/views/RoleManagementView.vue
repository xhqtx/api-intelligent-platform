<template>
  <div class="role-management-view page-container">
    <!-- 使用PageHeader组件 -->
    <PageHeader title="角色管理">
      <template #actions>
        <el-button type="primary" @click="handleCreate" :icon="Plus">新建角色</el-button>
      </template>
    </PageHeader>

    <!-- 使用SearchForm组件 -->
    <SearchForm @search="handleSearch" @reset="resetSearchForm">
      <template #default>
        <el-form-item label="角色名称">
          <el-input
            v-model="searchQuery.name"
            placeholder="搜索角色名称"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchQuery.status"
            placeholder="角色状态"
            clearable
            @clear="handleSearch"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
      </template>
    </SearchForm>

    <!-- 使用DataTable组件 -->
    <div class="main-card">
      <DataTable
        :data="roles"
        :loading="loading"
        :total="total"
        :page-size="searchQuery.size"
        :current-page="searchQuery.page"
        @update:page="handleCurrentChange"
        @update:pageSize="handleSizeChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="角色名称" min-width="120" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status ? 'success' : 'danger'">
              {{ row.status ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <!-- 操作列使用ActionButtons组件 -->
        <template #actions="{ row }">
          <ActionButtons>
            <ActionButton type="primary" @click="handleEdit(row)" :icon="Edit">编辑</ActionButton>
            <ActionButton type="success" @click="handlePermissions(row)" :icon="Key">权限</ActionButton>
            <ActionButton type="info" @click="handleUsers(row)" :icon="User">用户</ActionButton>
            <ActionButton 
              type="danger" 
              @click="handleDelete(row)" 
              :icon="Delete"
              :confirm-text="`确认删除角色'${row.name}'吗？`"
            >
              删除
            </ActionButton>
          </ActionButtons>
        </template>
      </DataTable>
    </div>

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新建角色'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="roleFormRef"
        :model="roleForm"
        :rules="rules"
        label-width="100px"
        @submit.prevent
      >
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="角色状态" prop="status" v-if="isEdit">
          <el-switch v-model="roleForm.status" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限配置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="配置权限"
      width="800px"
      destroy-on-close
    >
      <div class="permission-config-container">
        <!-- 权限组快速选择 -->
        <div class="permission-groups-section">
          <h4>权限组快速选择</h4>
          <div class="permission-groups-list">
            <el-checkbox-group v-model="selectedPermissionGroups" @change="handlePermissionGroupChange">
              <div v-for="group in permissionGroups" :key="group.id" class="permission-group-item">
                <el-checkbox :label="group.id">
                  {{ group.name }}
                  <el-tooltip :content="group.description || ''" placement="right">
                    <el-icon class="info-icon"><InfoFilled /></el-icon>
                  </el-tooltip>
                </el-checkbox>
                <div class="permission-count">
                  {{ group.permissions?.length || 0 }} 个权限
                </div>
              </div>
            </el-checkbox-group>
          </div>
        </div>

        <!-- 权限树 -->
        <div class="permissions-tree-section">
          <h4>详细权限配置</h4>
          <div class="tree-operations">
            <el-button size="small" @click="handleCheckAll">全选</el-button>
            <el-button size="small" @click="handleUncheckAll">取消全选</el-button>
          </div>
          <el-input
            v-model="permissionSearchKeyword"
            placeholder="搜索权限..."
            clearable
            class="permission-search"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-tree
            ref="permissionTreeRef"
            :data="filteredPermissionTree"
            show-checkbox
            node-key="id"
            :props="{ label: 'name', children: 'children' }"
            :default-checked-keys="selectedPermissions"
            :filter-node-method="filterNode"
            @check="handlePermissionCheck"
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handlePermissionSubmit"
            :loading="submitting"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户配置对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      title="配置用户"
      width="800px"
      destroy-on-close
    >
      <el-transfer
        v-model="selectedUsers"
        :data="allUsers"
        :titles="['可选用户', '已选用户']"
        :props="{
          key: 'id',
          label: 'username'
        }"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleUserSubmit"
            :loading="submitting"
          >
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Key,
  User,
  Delete,
  Search,
  InfoFilled
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import roleService, {
  Role,
  Permission,
  PermissionGroup,
  RoleQueryParams,
  CreateRoleRequest,
  UpdateRoleRequest
} from '@/services/role'
import PageHeader from '@/components/PageHeader.vue'
import SearchForm from '@/components/SearchForm.vue'
import DataTable from '@/components/DataTable.vue'
import ActionButton from '@/components/ActionButton.vue'
import ActionButtons from '@/components/ActionButtons.vue'

// 状态定义
const permissionTreeRef = ref()
const loading = ref(false)
const submitting = ref(false)
const roles = ref<Role[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)

// 监听对话框可见性变化
watch(permissionDialogVisible, (newValue) => {
  if (newValue) {
    // 当对话框打开时，确保权限树被正确初始化
    nextTick(() => {
      if (permissionTreeRef.value) {
        console.log('初始化权限树选中状态')
        permissionTreeRef.value.setCheckedKeys(selectedPermissions.value)
      }
    })
  }
})
const userDialogVisible = ref(false)
const isEdit = ref(false)
const currentRole = ref<Role | null>(null)
const selectedPermissions = ref<number[]>([])
const selectedPermissionGroups = ref<number[]>([])
const permissionGroups = ref<PermissionGroup[]>([])
const permissionSearchKeyword = ref('')
interface TreeNode {
  id: number;
  name: string;
  description?: string;
  children?: TreeNode[];
  code?: string;
}

interface UserInfo {
  id: number;
  username: string;
}

const permissionTree = ref<TreeNode[]>([])
const allUsers = ref<UserInfo[]>([])
const selectedUsers = ref<number[]>([])

// 搜索条件
const searchQuery = reactive<RoleQueryParams>({
  page: 1,
  size: 10,
  name: '',
  status: undefined
})

// 表单相关
const roleFormRef = ref<FormInstance>()
const roleForm = reactive({
  name: '',
  description: '',
  status: true
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入角色描述', trigger: 'blur' },
    { max: 200, message: '最大长度为 200 个字符', trigger: 'blur' }
  ]
}

// 重置搜索表单
const resetSearchForm = () => {
  searchQuery.name = ''
  searchQuery.status = undefined
  handleSearch()
}

// 处理表格选择
const handleSelectionChange = (selection: Role[]) => {
  // 处理选中的行
  console.log('选中的行:', selection)
}

// 监听权限搜索关键词
watch(permissionSearchKeyword, (val) => {
  if (permissionTreeRef.value) {
    permissionTreeRef.value.filter(val)
  }
})

// 过滤权限节点
const filterNode = (value: string, data: any) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

// 计算过滤后的权限树
const filteredPermissionTree = computed(() => {
  if (!permissionSearchKeyword.value) {
    return permissionTree.value
  }
  // 深拷贝权限树并过滤
  const filterTree = (nodes: any[]) => {
    return nodes.filter(node => {
      if (node.children) {
        node.children = filterTree(node.children)
        return node.children.length > 0 || node.name.toLowerCase().includes(permissionSearchKeyword.value.toLowerCase())
      }
      return node.name.toLowerCase().includes(permissionSearchKeyword.value.toLowerCase())
    })
  }
  return filterTree(JSON.parse(JSON.stringify(permissionTree.value)))
})

// 处理权限组选择变化
const handlePermissionGroupChange = (groupIds: number[]) => {
  // 获取所选权限组的所有权限ID
  const permissions = new Set<number>(selectedPermissions.value)
  for (const groupId of groupIds) {
    const group = permissionGroups.value.find(g => g.id === groupId)
    if (group && group.permissions) {
      group.permissions.forEach((p: Permission) => {
        permissions.add(p.id)
      })
    }
  }
  
  // 更新已选权限
  selectedPermissions.value = Array.from(permissions)
  if (permissionTreeRef.value) {
    permissionTreeRef.value.setCheckedKeys(selectedPermissions.value)
  }
}

// 加载权限组
const loadPermissionGroups = async () => {
  try {
    const response = await roleService.getPermissionGroups()
    // 从分页响应中获取权限组数组
    if (response && Array.isArray(response.content)) {
      permissionGroups.value = response.content
    } else {
      console.error('getPermissionGroups 返回的数据格式不正确:', response)
      permissionGroups.value = []
    }
    // 初始化选中的权限组
    selectedPermissionGroups.value = []
  } catch (error) {
    console.error('Failed to load permission groups:', error)
    ElMessage.error('加载权限组失败')
  }
}

// 全选/取消全选权限
const handleCheckAll = () => {
  const allKeys = getAllPermissionKeys(permissionTree.value)
  permissionTreeRef.value?.setCheckedKeys(allKeys)
}

const handleUncheckAll = () => {
  permissionTreeRef.value?.setCheckedKeys([])
}

// 获取所有权限的ID
const getAllPermissionKeys = (nodes: any[]): number[] => {
  const keys: number[] = []
  const traverse = (nodes: any[]) => {
    nodes.forEach(node => {
      keys.push(node.id)
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  traverse(nodes)
  return keys
}

// 初始化
onMounted(() => {
  loadRoles()
  loadPermissions()
  loadPermissionGroups()
})

// 加载角色列表
const loadRoles = async () => {
  loading.value = true
  try {
    const response = await roleService.getRoles(searchQuery)
    roles.value = response.content
    total.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 加载权限树
const loadPermissions = async () => {
  try {
    const permissions = await roleService.getAllPermissions()
    console.log('获取到的权限列表:', permissions)
    permissionTree.value = buildPermissionTree(permissions)
    console.log('构建的权限树:', permissionTree.value)
  } catch (error) {
    console.error('加载权限列表失败:', error)
    ElMessage.error('加载权限列表失败')
  }
}


// 构建权限树
const buildPermissionTree = (permissions: Permission[]): TreeNode[] => {
  const map = new Map<number, TreeNode>();
  const roots: TreeNode[] = [];

  permissions.forEach(permission => {
    const node: TreeNode = {
      id: permission.id,
      name: permission.name,
      code: permission.code,
      children: []
    };
    map.set(permission.id, node);

    // 假设权限的code格式为 "parent:child:grandchild"
    const parts = permission.code.split(':');
    if (parts.length === 1) {
      roots.push(node);
    } else {
      const parentCode = parts.slice(0, -1).join(':');
      const parent = Array.from(map.values()).find(n => n.code === parentCode);
      if (parent) {
        parent.children = parent.children || [];
        parent.children.push(node);
      } else {
        roots.push(node);
      }
    }
  });

  // 对根节点和每个节点的子节点进行排序
  const sortNodes = (nodes: TreeNode[]) => {
    nodes.sort((a, b) => a.name.localeCompare(b.name));
    nodes.forEach(node => {
      if (node.children && node.children.length > 0) {
        sortNodes(node.children);
      }
    });
  };
  
  sortNodes(roots);
  return roots;
}

// 搜索处理
const handleSearch = () => {
  searchQuery.page = 1
  loadRoles()
}

// 分页处理
const handleSizeChange = (val: number) => {
  searchQuery.size = val
  loadRoles()
}

const handleCurrentChange = (val: number) => {
  searchQuery.page = val
  loadRoles()
}

// 创建角色
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = async (row: Role) => {
  isEdit.value = true
  currentRole.value = row
  Object.assign(roleForm, row)
  dialogVisible.value = true
}

// 配置权限
const handlePermissions = async (row: Role) => {
  currentRole.value = row
  console.log('开始加载角色权限, 角色ID:', row.id)
  try {
    // 加载角色的已有权限
    console.log('调用getRolePermissions API...')
    const response = await roleService.getRolePermissions(row.id)
    console.log('API返回的权限数据:', JSON.stringify(response))
    
    // 确保response.data是数组
    if (!response.success || !Array.isArray(response.data)) {
      console.error('权限数据不是有效格式:', response)
      throw new Error('权限数据格式无效')
    }
    
    let permissionsData = response.data
    
    console.log('处理权限数据, 数量:', permissionsData.length)
    selectedPermissions.value = permissionsData.map(p => {
      console.log('处理权限项:', p)
      return p.id
    })
    
    console.log('设置选中的权限IDs:', selectedPermissions.value)
    
    // 确保权限树已加载
    if (permissionTree.value.length === 0) {
      console.log('权限树为空，重新加载权限')
      await loadPermissions()
    }
    
    console.log('当前权限树:', permissionTree.value)
    
    // 根据已选权限更新权限树的选中状态
    nextTick(() => {
      if (permissionTreeRef.value) {
        console.log('更新权限树选中状态')
        permissionTreeRef.value.setCheckedKeys(selectedPermissions.value)
      } else {
        console.error('permissionTreeRef.value 不存在')
      }
    })

    // 根据已选权限更新权限组的选中状态
    console.log('更新权限组选中状态')
    console.log('permissionGroups.value:', permissionGroups.value)
    if (Array.isArray(permissionGroups.value)) {
      selectedPermissionGroups.value = permissionGroups.value
        .filter(group => Array.isArray(group.permissions) && group.permissions.some(p => selectedPermissions.value.includes(p.id)))
        .map(group => group.id)
    } else {
      console.error('permissionGroups.value 不是数组:', permissionGroups.value)
      selectedPermissionGroups.value = []
    }
    console.log('选中的权限组:', selectedPermissionGroups.value)

    permissionDialogVisible.value = true
  } catch (error) {
    console.error('加载角色权限失败:', error)
    console.error('错误详情:', error instanceof Error ? error.stack : '未知错误')
    ElMessage.error('加载角色权限失败')
  }
}

// 权限树选择变化
const handlePermissionCheck = (checkedNodes: any, checkedInfo: any) => {
  selectedPermissions.value = checkedInfo.checkedKeys
  
  // 更新权限组的选中状态
  selectedPermissionGroups.value = permissionGroups.value
    .filter(group => group.permissions.every(p => selectedPermissions.value.includes(p.id)))
    .map(group => group.id)
}

// 配置用户
const handleUsers = async (row: Role) => {
  currentRole.value = row
  try {
    const users = await roleService.getRoleUsers(row.id)
    selectedUsers.value = users.map(u => u.id)
    userDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  }
}

// 删除角色
const handleDelete = async (row: Role) => {
  try {
    await ElMessageBox.confirm('确认删除该角色吗？', '警告', {
      type: 'warning'
    })
    await roleService.deleteRole(row.id)
    ElMessage.success('删除成功')
    loadRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 表单提交
const handleSubmit = async () => {
  if (!roleFormRef.value) return
  
  await roleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value && currentRole.value) {
          await roleService.updateRole(currentRole.value.id, roleForm as UpdateRoleRequest)
          ElMessage.success('更新成功')
        } else {
          await roleService.createRole(roleForm as CreateRoleRequest)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadRoles()
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 权限提交
const handlePermissionSubmit = async () => {
  if (!currentRole.value) return
  
  submitting.value = true
  try {
    // 这里需要根据实际接口调整
    await roleService.updateRole(currentRole.value.id, {
      ...currentRole.value,
      permissions: selectedPermissions.value
    } as UpdateRoleRequest)
    ElMessage.success('权限配置成功')
    permissionDialogVisible.value = false
    loadRoles()
  } catch (error) {
    ElMessage.error('权限配置失败')
  } finally {
    submitting.value = false
  }
}

// 用户提交
const handleUserSubmit = async () => {
  if (!currentRole.value) return
  
  submitting.value = true
  try {
    await roleService.assignUsers(currentRole.value.id, selectedUsers.value)
    ElMessage.success('用户配置成功')
    userDialogVisible.value = false
  } catch (error) {
    ElMessage.error('用户配置失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (roleFormRef.value) {
    roleFormRef.value.resetFields()
  }
  Object.assign(roleForm, {
    name: '',
    description: '',
    status: true
  })
}

// 格式化日期时间
const formatDateTime = (datetime: string) => {
  return new Date(datetime).toLocaleString()
}
</script>

<style lang="scss" scoped>
.role-management-view {
  .dialog-footer {
    padding-top: 20px;
    text-align: right;
  }

  :deep(.el-transfer) {
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .permission-config-container {
    display: flex;
    gap: 20px;
    height: 500px;

    .permission-groups-section,
    .permissions-tree-section {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 10px;
      height: 100%;

      h4 {
        margin: 0 0 10px;
      }
    }

    .permission-groups-section {
      flex: 0 0 250px;
      border-right: 1px solid #dcdfe6;
      padding-right: 20px;

      .permission-groups-list {
        overflow-y: auto;
      }

      .permission-group-item {
        margin-bottom: 10px;
        display: flex;
        align-items: center;

        .info-icon {
          margin-left: 5px;
          font-size: 14px;
          color: #909399;
        }
      }
    }

    .permissions-tree-section {
      .tree-operations {
        display: flex;
        gap: 10px;
        margin-bottom: 10px;
      }

      .permission-search {
        margin-bottom: 10px;
      }

      :deep(.el-tree) {
        flex: 1;
        overflow-y: auto;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        padding: 10px;
      }
    }
  }
}
</style>