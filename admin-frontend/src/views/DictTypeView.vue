<template>
  <div class="dict-type-view page-container">
    <!-- 使用PageHeader组件 -->
    <PageHeader title="字典类型管理">
      <template #actions>
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          <span>新增字典类型</span>
        </el-button>
      </template>
    </PageHeader>

    <!-- 使用SearchForm组件 -->
    <SearchForm @search="handleSearch" @reset="resetSearchForm">
      <template #default>
        <el-form-item label="字典名称">
          <el-input v-model="searchForm.name" placeholder="请输入字典名称"></el-input>
        </el-form-item>
        <el-form-item label="字典编码">
          <el-input v-model="searchForm.code" placeholder="请输入字典编码"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
      </template>
    </SearchForm>

    <!-- 使用DataTable组件 -->
    <div class="main-card">
      <DataTable
        :data="dictTypeList"
        :loading="loading"
        :total="pagination.total"
        :page-size="pagination.size"
        :current-page="pagination.page + 1"
        @update:page="handleCurrentChange"
        @update:pageSize="handleSizeChange"
      >
        <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
        <el-table-column prop="name" label="字典名称" width="160"></el-table-column>
        <el-table-column prop="code" label="字典编码" width="160"></el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160" align="center"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <ActionButtons>
              <ActionButton @click="viewDictData(row)">字典数据</ActionButton>
              <ActionButton type="primary" @click="editDictType(row)">编辑</ActionButton>
              <ActionButton 
                type="danger" 
                :confirm-text="`确认删除字典类型'${row.name}'吗？`"
                @click="deleteDictType(row)"
              >
                删除
              </ActionButton>
            </ActionButtons>
          </template>
        </el-table-column>
      </DataTable>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '新增字典类型' : '编辑字典类型'"
      v-model="dialogVisible"
      width="500px">
      <el-form :model="dictTypeForm" :rules="rules" ref="dictTypeFormRef" label-width="100px">
        <el-form-item label="字典名称" prop="name">
          <el-input v-model="dictTypeForm.name" placeholder="请输入字典名称"></el-input>
        </el-form-item>
        <el-form-item label="字典编码" prop="code">
          <el-input v-model="dictTypeForm.code" placeholder="请输入字典编码" :disabled="dialogType === 'edit'"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictTypeForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dictTypeForm.remark" type="textarea" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDictType">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getDictTypes, createDictType, updateDictType, deleteDictType as apiDeleteDictType, DictType, DictTypeRequest } from '@/services/dict';
import { useRouter } from 'vue-router';
import { Plus } from '@element-plus/icons-vue';
import PageHeader from '@/components/PageHeader.vue';
import SearchForm from '@/components/SearchForm.vue';
import DataTable from '@/components/DataTable.vue';
import ActionButton from '@/components/ActionButton.vue';
import ActionButtons from '@/components/ActionButtons.vue';

export default defineComponent({
  name: 'DictTypeView',
  components: {
    Plus,
    PageHeader,
    SearchForm,
    DataTable,
    ActionButton,
    ActionButtons
  },
  setup() {
    const router = useRouter();
    const loading = ref(false);
    const dictTypeList = ref<DictType[]>([]);
    const dialogVisible = ref(false);
    const dialogType = ref<'add' | 'edit'>('add');
    const dictTypeFormRef = ref();
    
    // 分页信息
    const pagination = reactive({
      page: 0,
      size: 10,
      total: 0
    });

    // 搜索表单
    const searchForm = reactive({
      name: '',
      code: '',
      status: null as number | null
    });

    // 字典类型表单
    const dictTypeForm = reactive<DictTypeRequest>({
      name: '',
      code: '',
      status: 1,
      remark: ''
    });

    // 表单验证规则
    const rules = {
      name: [
        { required: true, message: '请输入字典名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      code: [
        { required: true, message: '请输入字典编码', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ]
    };

    // 获取字典类型列表
    const fetchDictTypes = async () => {
      loading.value = true;
      try {
        const response = await getDictTypes(pagination.page, pagination.size);
        if (response.data.success) {
          dictTypeList.value = response.data.data.content;
          pagination.total = response.data.data.totalElements;
        } else {
          ElMessage.error(response.data.message || '获取字典类型列表失败');
        }
      } catch (error) {
        console.error('获取字典类型列表出错:', error);
        ElMessage.error('获取字典类型列表失败');
      } finally {
        loading.value = false;
      }
    };

    // 重置表单
    const resetForm = () => {
      dictTypeForm.name = '';
      dictTypeForm.code = '';
      dictTypeForm.status = 1;
      dictTypeForm.remark = '';
    };

    // 显示添加对话框
    const showAddDialog = () => {
      dialogType.value = 'add';
      resetForm();
      dialogVisible.value = true;
    };

    // 编辑字典类型
    const editDictType = (row: DictType) => {
      dialogType.value = 'edit';
      dictTypeForm.name = row.name;
      dictTypeForm.code = row.code;
      dictTypeForm.status = row.status;
      dictTypeForm.remark = row.remark;
      dialogVisible.value = true;
    };

    // 提交字典类型
    const submitDictType = async () => {
      if (!dictTypeFormRef.value) return;
      
      dictTypeFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          try {
            if (dialogType.value === 'add') {
              const response = await createDictType(dictTypeForm);
              if (response.data.success) {
                ElMessage.success('添加成功');
                dialogVisible.value = false;
                fetchDictTypes();
              } else {
                ElMessage.error(response.data.message || '添加失败');
              }
            } else {
              const response = await updateDictType(
                dictTypeList.value.find(item => item.code === dictTypeForm.code)?.id || 0, 
                dictTypeForm
              );
              if (response.data.success) {
                ElMessage.success('更新成功');
                dialogVisible.value = false;
                fetchDictTypes();
              } else {
                ElMessage.error(response.data.message || '更新失败');
              }
            }
          } catch (error) {
            console.error('提交字典类型出错:', error);
            ElMessage.error('操作失败');
          }
        }
      });
    };

    // 删除字典类型
    const deleteDictType = (row: DictType) => {
      ElMessageBox.confirm(`确认删除字典类型"${row.name}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await apiDeleteDictType(row.id);
          if (response.data.success) {
            ElMessage.success('删除成功');
            fetchDictTypes();
          } else {
            ElMessage.error(response.data.message || '删除失败');
          }
        } catch (error) {
          console.error('删除字典类型出错:', error);
          ElMessage.error('删除失败');
        }
      }).catch(() => {
        // 取消删除
      });
    };

    // 查看字典数据
    const viewDictData = (row: DictType) => {
      router.push({
        name: 'DictData',
        params: { typeCode: row.code },
        query: { typeName: row.name }
      });
    };

    // 搜索
    const handleSearch = () => {
      pagination.page = 0;
      fetchDictTypes();
    };

    // 重置搜索表单
    const resetSearchForm = () => {
      searchForm.name = '';
      searchForm.code = '';
      searchForm.status = null;
      handleSearch();
    };

    // 分页大小变化
    const handleSizeChange = (val: number) => {
      pagination.size = val;
      pagination.page = 0;
      fetchDictTypes();
    };

    // 页码变化
    const handleCurrentChange = (val: number) => {
      pagination.page = val - 1;
      fetchDictTypes();
    };

    onMounted(() => {
      fetchDictTypes();
    });

    return {
      loading,
      dictTypeList,
      pagination,
      searchForm,
      dialogVisible,
      dialogType,
      dictTypeForm,
      dictTypeFormRef,
      rules,
      showAddDialog,
      editDictType,
      deleteDictType,
      submitDictType,
      viewDictData,
      handleSearch,
      resetSearchForm,
      handleSizeChange,
      handleCurrentChange
    };
  }
});
</script>

<style lang="scss" scoped>
.dict-type-view {
  .el-dialog {
    .el-form {
      max-width: 460px;
      margin: 0 auto;
    }
  }
}
</style>