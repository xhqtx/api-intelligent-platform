<template>
  <div class="permission-log">
    <h1>权限变更日志</h1>
    
    <!-- 筛选条件 -->
    <div class="card mb-4">
      <div class="card-header">
        <h5 class="mb-0">筛选条件</h5>
      </div>
      <div class="card-body">
        <form @submit.prevent="loadLogs(1)" class="row g-3">
          <div class="col-md-4">
            <label for="entityType" class="form-label">实体类型</label>
            <select id="entityType" class="form-select" v-model="filter.entityType">
              <option value="">全部</option>
              <option value="USER">用户</option>
              <option value="ROLE">角色</option>
              <option value="PERMISSION_GROUP">权限组</option>
            </select>
          </div>
          <div class="col-md-4">
            <label for="entityId" class="form-label">实体ID</label>
            <input
              type="number"
              class="form-control"
              id="entityId"
              v-model="filter.entityId"
              placeholder="输入实体ID"
            />
          </div>
          <div class="col-md-4 d-flex align-items-end">
            <button type="submit" class="btn btn-primary">
              <i class="fas fa-search"></i> 查询
            </button>
            <button type="button" class="btn btn-secondary ms-2" @click="resetFilter">
              <i class="fas fa-redo"></i> 重置
            </button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- 日志列表 -->
    <div class="card">
      <div class="card-header">
        <h5 class="mb-0">变更日志列表</h5>
      </div>
      <div class="card-body">
        <div class="table-responsive">
          <table class="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>实体类型</th>
                <th>实体ID</th>
                <th>操作类型</th>
                <th>操作人</th>
                <th>变更内容</th>
                <th>变更时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="7" class="text-center">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">加载中...</span>
                  </div>
                </td>
              </tr>
              <tr v-else-if="logs.length === 0">
                <td colspan="7" class="text-center">暂无数据</td>
              </tr>
              <tr v-else v-for="log in logs" :key="log.id">
                <td>{{ log.id }}</td>
                <td>{{ formatEntityType(log.entityType) }}</td>
                <td>{{ log.entityId }}</td>
                <td>
                  <span :class="getOperationTypeClass(log.operationType)">
                    {{ formatOperationType(log.operationType) }}
                  </span>
                </td>
                <td>{{ log.operatorName || log.operatorId }}</td>
                <td>
                  <button class="btn btn-sm btn-outline-info" @click="viewChangeDetails(log)">
                    查看详情
                  </button>
                </td>
                <td>{{ formatDate(log.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 分页 -->
        <div class="d-flex justify-content-between align-items-center mt-3">
          <div>
            共 {{ totalItems }} 条记录，当前第 {{ currentPage }}/{{ totalPages }} 页
          </div>
          <nav aria-label="Page navigation">
            <ul class="pagination mb-0">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="loadLogs(1)">首页</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="loadLogs(currentPage - 1)">上一页</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="loadLogs(currentPage + 1)">下一页</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="loadLogs(totalPages)">末页</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>

    <!-- 变更详情模态框 -->
    <div class="modal fade" id="changeDetailsModal" tabindex="-1" aria-labelledby="changeDetailsModalLabel" aria-hidden="true" ref="changeDetailsModalRef">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="changeDetailsModalLabel">变更详情</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedLog">
              <div class="mb-3">
                <h6>基本信息</h6>
                <table class="table table-bordered">
                  <tbody>
                    <tr>
                      <th width="150">实体类型</th>
                      <td>{{ formatEntityType(selectedLog.entityType) }}</td>
                    </tr>
                    <tr>
                      <th>实体ID</th>
                      <td>{{ selectedLog.entityId }}</td>
                    </tr>
                    <tr>
                      <th>操作类型</th>
                      <td>
                        <span :class="getOperationTypeClass(selectedLog.operationType)">
                          {{ formatOperationType(selectedLog.operationType) }}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>操作人</th>
                      <td>{{ selectedLog.operatorName || selectedLog.operatorId }}</td>
                    </tr>
                    <tr>
                      <th>操作时间</th>
                      <td>{{ formatDate(selectedLog.createdAt) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
              
              <div class="mb-3">
                <h6>变更内容</h6>
                <div v-if="selectedLog.operationType === 'ADD'">
                  <div class="alert alert-success">
                    <strong>添加了权限：</strong>
                    <ul class="mb-0">
                      <li v-for="(perm, index) in parseChanges(selectedLog.changes).added" :key="index">
                        {{ perm.name }} ({{ perm.id }})
                      </li>
                    </ul>
                  </div>
                </div>
                <div v-else-if="selectedLog.operationType === 'REMOVE'">
                  <div class="alert alert-danger">
                    <strong>移除了权限：</strong>
                    <ul class="mb-0">
                      <li v-for="(perm, index) in parseChanges(selectedLog.changes).removed" :key="index">
                        {{ perm.name }} ({{ perm.id }})
                      </li>
                    </ul>
                  </div>
                </div>
                <div v-else>
                  <pre class="p-3 bg-light">{{ JSON.stringify(parseChanges(selectedLog.changes), null, 2) }}</pre>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, computed } from 'vue';
import { permissionLogApi } from '@/services/permission';
import { Modal } from 'bootstrap';

interface PermissionLog {
  id: number;
  entityType: string;
  entityId: number;
  operationType: string;
  operatorId: number;
  operatorName?: string;
  changes: string;
  createdAt: string;
}

interface LogFilter {
  entityType: string;
  entityId: number | null;
  page: number;
  size: number;
}

export default defineComponent({
  name: 'PermissionLogView',
  setup() {
    const logs = ref<PermissionLog[]>([]);
    const loading = ref(true);
    const selectedLog = ref<PermissionLog | null>(null);
    const changeDetailsModalRef = ref<HTMLElement | null>(null);
    let changeDetailsModal: Modal | null = null;
    
    // 分页相关
    const currentPage = ref(1);
    const pageSize = ref(10);
    const totalItems = ref(0);
    const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value));
    
    // 筛选条件
    const filter = ref<LogFilter>({
      entityType: '',
      entityId: null,
      page: 1,
      size: 10
    });

    // 加载日志
    const loadLogs = async (page: number) => {
      loading.value = true;
      filter.value.page = page;
      currentPage.value = page;
      
      try {
        interface LogParams {
          page: number;
          size: number;
          entityType?: string;
          entityId?: number;
        }

        const params: LogParams = {
          page: filter.value.page - 1,  // 后端从0开始计数
          size: filter.value.size
        };
        
        if (filter.value.entityType) {
          params.entityType = filter.value.entityType;
        }
        
        if (filter.value.entityId) {
          params.entityId = filter.value.entityId;
        }
        
        const response = await permissionLogApi.getPermissionLogs(params);
        logs.value = response.data.data.content || [];
        totalItems.value = response.data.data.totalElements || 0;
      } catch (error) {
        console.error('Failed to load permission logs:', error);
        alert('加载权限变更日志失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    };

    // 重置筛选条件
    const resetFilter = () => {
      filter.value = {
        entityType: '',
        entityId: null,
        page: 1,
        size: 10
      };
      loadLogs(1);
    };

    // 查看变更详情
    const viewChangeDetails = (log: PermissionLog) => {
      selectedLog.value = log;
      changeDetailsModal?.show();
    };

    // 格式化实体类型
    const formatEntityType = (type: string) => {
      const types: Record<string, string> = {
        'USER': '用户',
        'ROLE': '角色',
        'PERMISSION_GROUP': '权限组'
      };
      return types[type] || type;
    };

    // 格式化操作类型
    const formatOperationType = (type: string) => {
      const types: Record<string, string> = {
        'ADD': '添加权限',
        'REMOVE': '移除权限',
        'UPDATE': '更新权限',
        'CREATE': '创建',
        'DELETE': '删除'
      };
      return types[type] || type;
    };

    // 获取操作类型对应的样式
    const getOperationTypeClass = (type: string) => {
      const classes: Record<string, string> = {
        'ADD': 'badge bg-success',
        'REMOVE': 'badge bg-danger',
        'UPDATE': 'badge bg-warning',
        'CREATE': 'badge bg-info',
        'DELETE': 'badge bg-secondary'
      };
      return classes[type] || 'badge bg-primary';
    };

    // 解析变更内容
    const parseChanges = (changesJson: string) => {
      try {
        return JSON.parse(changesJson);
      } catch (error) {
        return { error: '无法解析变更内容' };
      }
    };

    // 格式化日期
    const formatDate = (dateString: string) => {
      return new Date(dateString).toLocaleString();
    };

    onMounted(() => {
      // 初始化模态框
      if (changeDetailsModalRef.value) {
        changeDetailsModal = new Modal(changeDetailsModalRef.value);
      }
      
      // 加载日志
      loadLogs(1);
    });

    return {
      logs,
      loading,
      selectedLog,
      changeDetailsModalRef,
      currentPage,
      pageSize,
      totalItems,
      totalPages,
      filter,
      loadLogs,
      resetFilter,
      viewChangeDetails,
      formatEntityType,
      formatOperationType,
      getOperationTypeClass,
      parseChanges,
      formatDate
    };
  }
});
</script>

<style scoped>
.permission-log {
  padding: 20px;
}
</style>