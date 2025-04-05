<template>
  <div class="dict-data-view page-container">
    <!-- 使用PageHeader组件 -->
    <PageHeader :title="`${typeName} - 字典数据管理`">
      <template #actions>
        <div class="header-actions">
          <el-button @click="goBack">
            <el-icon><Back /></el-icon>
            <span>返回</span>
          </el-button>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            <span>新增字典数据</span>
          </el-button>
        </div>
      </template>
    </PageHeader>

    <!-- 使用SearchForm组件 -->
    <SearchForm @search="handleSearch" @reset="resetSearchForm">
      <template #default>
        <el-form-item label="字典标签">
          <el-input v-model="searchForm.label" placeholder="请输入字典标签"></el-input>
        </el-form-item>
        <el-form-item label="字典值">
          <el-input v-model="searchForm.value" placeholder="请输入字典值"></el-input>
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
        :data="dictDataList"
        :loading="loading"
        :total="dictDataList.length"
        @update:page="handlePageChange"
        @update:pageSize="handleSizeChange">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="label" label="字典标签" width="180"></el-table-column>
        <el-table-column prop="value" label="字典值" width="180"></el-table-column>
        <el-table-column prop="sort" label="排序" width="100"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultFlag" label="默认值" width="100">
          <template #default="scope">
            <el-tag type="info" v-if="scope.row.defaultFlag === 1">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
        
        <!-- 操作列使用ActionButton组件 -->
        <template #actions="{ row }">
          <ActionButton type="primary" @click="editDictData(row)">编辑</ActionButton>
          <ActionButton 
            type="danger" 
              :confirm-text="`确认删除字典数据'${row.label}'吗？`"
            @click="deleteDictData(row)">
            删除
          </ActionButton>
        </template>
      </DataTable>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '新增字典数据' : '编辑字典数据'"
      v-model="dialogVisible"
      width="500px">
      <el-form :model="dictDataForm" :rules="rules" ref="dictDataFormRef" label-width="100px">
        <el-form-item label="字典标签" prop="label">
          <el-input v-model="dictDataForm.label" placeholder="请输入字典标签"></el-input>
        </el-form-item>
        <el-form-item label="字典值" prop="value">
          <el-input v-model="dictDataForm.value" placeholder="请输入字典值"></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dictDataForm.sort" :min="0" :max="9999"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dictDataForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否默认" prop="defaultFlag">
          <el-radio-group v-model="dictDataForm.defaultFlag">
            <el-radio :label="1">是</el-radio>
            <el-radio :label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dictDataForm.remark" type="textarea" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDictData">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { Back, Plus } from '@element-plus/icons-vue';
import { 
  getDictDataByType, 
  createDictData, 
  updateDictData, 
  deleteDictData as apiDeleteDictData, 
  DictData, 
  DictDataRequest 
} from '@/services/dict';
import PageHeader from '@/components/PageHeader.vue';
import SearchForm from '@/components/SearchForm.vue';
import DataTable from '@/components/DataTable.vue';
import ActionButton from '@/components/ActionButton.vue';

export default defineComponent({
  name: 'DictDataView',
  components: {
    Back,
    Plus,
    PageHeader,
    SearchForm,
    DataTable,
    ActionButton
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const loading = ref(false);
    const dictDataList = ref<DictData[]>([]);
    const dialogVisible = ref(false);
    const dialogType = ref<'add' | 'edit'>('add');
    const dictDataFormRef = ref();
    const typeCode = computed(() => route.params.typeCode as string);
    const typeName = computed(() => route.query.typeName as string);
    const currentEditId = ref<number>(0);
    
    // 搜索表单
    const searchForm = reactive({
      label: '',
      value: '',
      status: null as number | null
    });

    // 字典数据表单
    const dictDataForm = reactive<DictDataRequest>({
      dictTypeCode: typeCode.value,
      label: '',
      value: '',
      sort: 0,
      status: 1,
      defaultFlag: 0,
      remark: ''
    });

    // 表单验证规则
    const rules = {
      label: [
        { required: true, message: '请输入字典标签', trigger: 'blur' },
        { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
      ],
      value: [
        { required: true, message: '请输入字典值', trigger: 'blur' },
        { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
      ],
      sort: [
        { required: true, message: '请输入排序值', trigger: 'blur' }
      ]
    };

    // 获取字典数据列表
    const fetchDictData = async () => {
      if (!typeCode.value) {
        ElMessage.error('缺少字典类型编码');
        return;
      }
      
      loading.value = true;
      try {
        const response = await getDictDataByType(typeCode.value);
        if (response.data.success) {
          dictDataList.value = response.data.data;
        } else {
          ElMessage.error(response.data.message || '获取字典数据列表失败');
        }
      } catch (error) {
        console.error('获取字典数据列表出错:', error);
        ElMessage.error('获取字典数据列表失败');
      } finally {
        loading.value = false;
      }
    };

    // 重置表单
    const resetForm = () => {
      dictDataForm.label = '';
      dictDataForm.value = '';
      dictDataForm.sort = 0;
      dictDataForm.status = 1;
      dictDataForm.defaultFlag = 0;
      dictDataForm.remark = '';
      dictDataForm.dictTypeCode = typeCode.value;
    };

    // 显示添加对话框
    const showAddDialog = () => {
      dialogType.value = 'add';
      resetForm();
      dialogVisible.value = true;
    };

    // 编辑字典数据
    const editDictData = (row: DictData) => {
      dialogType.value = 'edit';
      currentEditId.value = row.id;
      dictDataForm.label = row.label;
      dictDataForm.value = row.value;
      dictDataForm.sort = row.sort;
      dictDataForm.status = row.status;
      dictDataForm.defaultFlag = row.defaultFlag;
      dictDataForm.remark = row.remark;
      dictDataForm.dictTypeCode = row.dictTypeCode;
      dialogVisible.value = true;
    };

    // 提交字典数据
    const submitDictData = async () => {
      if (!dictDataFormRef.value) return;
      
      dictDataFormRef.value.validate(async (valid: boolean) => {
        if (valid) {
          try {
            if (dialogType.value === 'add') {
              const response = await createDictData(dictDataForm);
              if (response.data.success) {
                ElMessage.success('添加成功');
                dialogVisible.value = false;
                fetchDictData();
              } else {
                ElMessage.error(response.data.message || '添加失败');
              }
            } else {
              const response = await updateDictData(currentEditId.value, dictDataForm);
              if (response.data.success) {
                ElMessage.success('更新成功');
                dialogVisible.value = false;
                fetchDictData();
              } else {
                ElMessage.error(response.data.message || '更新失败');
              }
            }
          } catch (error) {
            console.error('提交字典数据出错:', error);
            ElMessage.error('操作失败');
          }
        }
      });
    };

    // 删除字典数据
    const deleteDictData = (row: DictData) => {
      ElMessageBox.confirm(`确认删除字典数据"${row.label}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await apiDeleteDictData(row.id);
          if (response.data.success) {
            ElMessage.success('删除成功');
            fetchDictData();
          } else {
            ElMessage.error(response.data.message || '删除失败');
          }
        } catch (error) {
          console.error('删除字典数据出错:', error);
          ElMessage.error('删除失败');
        }
      }).catch(() => {
        // 取消删除
      });
    };

    // 返回上一页
    const goBack = () => {
      router.push({ name: 'DictType' });
    };

    // 搜索
    const handleSearch = () => {
      // 由于API没有提供搜索功能，这里暂时使用前端过滤
      fetchDictData();
    };

    // 重置搜索表单
    const resetSearchForm = () => {
      searchForm.label = '';
      searchForm.value = '';
      searchForm.status = null;
      handleSearch();
    };
    
    // 处理分页变化
    const handlePageChange = (page: number) => {
      // 这里可以添加分页逻辑
      console.log('当前页:', page);
    };
    
    // 处理每页条数变化
    const handleSizeChange = (size: number) => {
      // 这里可以添加分页逻辑
      console.log('每页条数:', size);
    };

    onMounted(() => {
      fetchDictData();
    });

    return {
      loading,
      dictDataList,
      typeCode,
      typeName,
      searchForm,
      dialogVisible,
      dialogType,
      dictDataForm,
      dictDataFormRef,
      rules,
      showAddDialog,
      editDictData,
      deleteDictData,
      submitDictData,
      goBack,
      handleSearch,
      resetSearchForm,
      handlePageChange,
      handleSizeChange
    };
  }
});
</script>

<style lang="scss" scoped>
.dict-data-view {
  .header-actions {
    display: flex;
    gap: 10px;
  }

  .el-dialog {
    .el-form {
      max-width: 460px;
      margin: 0 auto;
    }
  }
}
</style>