<template>
  <div class="user-management page-container">
    <!-- 使用PageHeader组件 -->
    <PageHeader title="用户管理">
      <template #actions>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          <span>新增用户</span>
        </el-button>
      </template>
    </PageHeader>

    <!-- 使用SearchForm组件 -->
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="resetSearchForm">
      <el-form-item label="用户名">
        <el-input v-model="searchForm.username" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="searchForm.email" placeholder="请输入邮箱"></el-input>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable>
          <el-option label="启用" :value="true"></el-option>
          <el-option label="禁用" :value="false"></el-option>
        </el-select>
      </el-form-item>
    </SearchForm>

    <!-- 使用DataTable组件 -->
    <div class="main-card">
      <DataTable
        :data="users"
        :loading="loading"
        :total="pagination.total"
        :page-size="pagination.size"
        :current-page="pagination.page + 1"
        @update:page="handleCurrentChange"
        @update:pageSize="handleSizeChange"
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="150"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
              {{ scope.row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="150">
          <template #default="scope">
            <el-tag
              v-for="role in scope.row.roles"
              :key="role.id"
              :type="role.name === 'ADMIN' ? 'danger' : 'info'"
              class="mr-1"
            >
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <template #actions="{ row }">
          <ActionButtons>
            <el-button type="primary" size="small" @click="editUser(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="confirmDeleteUser(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </ActionButtons>
        </template>
      </DataTable>
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog
      :title="isEdit ? '编辑用户' : '新增用户'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="userForm.enabled"></el-switch>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.roles" multiple placeholder="请选择角色">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SearchForm from '@/components/SearchForm.vue'
import DataTable from '@/components/DataTable.vue'
import ActionButtons from '@/components/ActionButtons.vue'
import { 
  getUsers, 
  createUser,
  updateUser, 
  deleteUser, 
  getUserRoles,
  assignRolesToUser,
  type UserDTO,
  type UserRegistrationRequest,
  type UserUpdateRequest
} from '@/services/user'

export default defineComponent({
  name: 'UserManagementView',
  components: {
    Plus,
    Edit,
    Delete,
    PageHeader,
    SearchForm,
    DataTable,
    ActionButtons
  },
  setup() {
    // 搜索表单数据
    const searchForm = reactive({
      username: '',
      email: '',
      enabled: null as boolean | null
    })

    // 表格数据和加载状态
    const users = ref<UserDTO[]>([])
    const loading = ref(false)
    const pagination = reactive({
      page: 0,
      size: 10,
      total: 0
    })

    // 表单对话框相关
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const userFormRef = ref<FormInstance>()
    const userForm = reactive({
      id: 0,
      username: '',
      email: '',
      password: '',
      enabled: true,
      roles: [] as number[]
    })

    // 表单验证规则
    const userFormRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
      ]
    }

    // 角色选项
    const roleOptions = ref([
      { id: 1, name: 'ADMIN' },
      { id: 2, name: 'USER' }
    ])

    // 获取用户列表数据
    const fetchUsers = async () => {
      loading.value = true
      try {
        const response = await getUsers(
          pagination.page,
          pagination.size,
          searchForm.username || undefined,
          searchForm.email || undefined,
          searchForm.enabled || undefined
        )
        users.value = response.data.content
        pagination.total = response.data.totalElements
      } catch (error) {
        ElMessage.error('获取用户列表失败')
        console.error('Failed to fetch users:', error)
      } finally {
        loading.value = false
      }
    }

    // 搜索处理
    const handleSearch = async () => {
      pagination.page = 0
      await fetchUsers()
    }

    const resetSearchForm = async () => {
      searchForm.username = ''
      searchForm.email = ''
      searchForm.enabled = null
      await fetchUsers()
    }

    // 分页处理
    const handleCurrentChange = async (page: number) => {
      pagination.page = page - 1
      await fetchUsers()
    }

    const handleSizeChange = async (size: number) => {
      pagination.size = size
      pagination.page = 0
      await fetchUsers()
    }

    // 新增用户
    const showAddDialog = () => {
      isEdit.value = false
      Object.assign(userForm, {
        id: 0,
        username: '',
        email: '',
        password: '',
        enabled: true,
        roles: []
      })
      dialogVisible.value = true
    }

    // 编辑用户
    const editUser = (user: any) => {
      isEdit.value = true
      Object.assign(userForm, {
        id: Number(user.id),
        username: user.username,
        email: user.email,
        enabled: user.enabled,
        roles: user.roles.map((role: any) => role.id)
      })
      dialogVisible.value = true
    }

    // 删除用户
    const confirmDeleteUser = (user: UserDTO) => {
      ElMessageBox.confirm(
        `确认删除用户 "${user.username}" 吗？`,
        '删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await deleteUser(user.id)
          ElMessage.success('删除成功')
          await fetchUsers()
        } catch (error) {
          ElMessage.error('删除用户失败')
          console.error('Failed to delete user:', error)
        }
      }).catch(() => {
        // 取消删除
      })
    }

    // 提交表单
    const submitForm = async () => {
      if (!userFormRef.value) return
      
      await userFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (isEdit.value) {
              // 更新用户
              const updateData: UserUpdateRequest = {
                email: userForm.email,
                status: userForm.enabled,
                roles: userForm.roles
              }
              await updateUser(Number(userForm.id), updateData)
              
              // 更新用户角色
              const currentRoles = await getUserRoles(Number(userForm.id))
              await assignRolesToUser(Number(userForm.id), userForm.roles)
            } else {
              // 创建新用户
              const registrationData: UserRegistrationRequest = {
                username: userForm.username,
                password: userForm.password,
                email: userForm.email,
                roles: userForm.roles
              }
              await createUser(registrationData)
            }

            dialogVisible.value = false
            ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
            await fetchUsers()
          } catch (error) {
            ElMessage.error(isEdit.value ? '更新用户失败' : '创建用户失败')
            console.error('Failed to submit user form:', error)
          }
        }
      })
    }

    // 在组件挂载时获取用户列表
    onMounted(() => {
      fetchUsers()
    })

    return {
      // 数据
      searchForm,
      users,
      loading,
      pagination,
      dialogVisible,
      isEdit,
      userForm,
      userFormRef,
      userFormRules,
      roleOptions,

      // 方法
      handleSearch,
      resetSearchForm,
      handleCurrentChange,
      handleSizeChange,
      showAddDialog,
      editUser,
      confirmDeleteUser,
      submitForm
    }
  }
})
</script>

<style lang="scss" scoped>
.mr-1 {
  margin-right: 8px;
}
</style>