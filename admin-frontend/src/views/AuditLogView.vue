<template>
  <div class="audit-log">
    <PageHeader title="审计日志" />
    <div class="search-form">
      <el-form @submit.prevent="search" inline>
        <el-form-item>
          <el-input v-model="filters.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="filters.action" placeholder="操作" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="filters.resourceType" placeholder="资源类型" />
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="filters.startTime"
            type="datetime"
            placeholder="开始时间"
          />
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="filters.endTime"
            type="datetime"
            placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button type="success" @click="exportLogs">导出</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table
      :data="auditLogs"
      style="width: 100%"
      border
      stripe
      :loading="loading"
    >
      <el-table-column
        v-for="field in fields"
        :key="field.key"
        :prop="field.key"
        :label="field.label"
        :sortable="field.sortable"
      >
        <template #default="scope">
          <span v-if="field.formatter">{{ field.formatter(scope.row[field.key]) }}</span>
          <span v-else>{{ scope.row[field.key] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="text" @click="showDetails(scope.row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        v-model:currentPage="currentPage"
        v-model:pageSize="perPage"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onPageSizeChange"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, onMounted } from 'vue';
import { ElMessageBox } from 'element-plus';
import PageHeader from '@/components/PageHeader.vue';
import { getAuditLogs, exportAuditLogs } from '@/services/api';

export default defineComponent({
  name: 'AuditLogView',
  components: {
    PageHeader,
    DataTable,
  },
  setup() {
    const auditLogs = ref<any[]>([]);
    const total = ref(0);
    const currentPage = ref(1);
    const perPage = ref(10);
    const loading = ref(false);
    const filters = reactive({
      username: '',
      action: '',
      resourceType: '',
      startTime: '',
      endTime: '',
    });

    const fields = [
      { key: 'id', label: 'ID', sortable: true },
      { key: 'createdAt', label: '创建时间', sortable: true, formatter: (value) => formatDate(value) },
      { key: 'username', label: '用户名', sortable: true },
      { key: 'action', label: '操作', sortable: true },
      { key: 'resourceType', label: '资源类型', sortable: true },
      { key: 'resourceId', label: '资源ID', sortable: true },
      { key: 'ipAddress', label: 'IP地址', sortable: true },
      { key: 'details', label: '详细信息', formatter: (value) => truncateText(value, 50) },
      { key: 'actions', label: '操作' },
    ];

    const formatDate = (date: string) => {
      return new Date(date).toLocaleString();
    };

    const truncateText = (text: string, length: number) => {
      return text.length > length ? text.substring(0, length) + '...' : text;
    };

    const showDetails = (item: any) => {
      // 实现显示详细信息的逻辑，例如使用弹窗
      console.log('Show details for:', item);
      // 这里可以使用 Element Plus 的 ElMessageBox 来显示详细信息
      ElMessageBox.alert(item.details, '详细信息', {
        confirmButtonText: '确定',
      });
    };

    const search = async () => {
      try {
        loading.value = true;
        const response = await getAuditLogs({
          ...filters,
          page: currentPage.value - 1,
          size: perPage.value,
        });
        auditLogs.value = response.data.content;
        total.value = response.data.totalElements;
      } catch (error) {
        console.error('Failed to fetch audit logs', error);
        ElMessageBox.alert('获取审计日志失败，请稍后重试。', '错误', {
          confirmButtonText: '确定',
          type: 'error',
        });
      } finally {
        loading.value = false;
      }
    };

    const exportLogs = async () => {
      try {
        const response = await exportAuditLogs(filters);
        const blob = new Blob([JSON.stringify(response.data)], { type: 'application/json' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = 'audit_logs.json';
        link.click();
        URL.revokeObjectURL(link.href);
      } catch (error) {
        console.error('Failed to export audit logs', error);
      }
    };

    const onPageChange = (page: number) => {
      currentPage.value = page;
      search();
    };

    const onPageSizeChange = (size: number) => {
      perPage.value = size;
      search();
    };

    onMounted(() => {
      search();
    });

    return {
      auditLogs,
      total,
      currentPage,
      perPage,
      filters,
      fields,
      search,
      exportLogs,
      onPageChange,
      showDetails,
    };
  },
});
</script>

<style scoped>
.audit-log {
  padding: 20px;
}
.search-form {
  margin-bottom: 20px;
}
</style>