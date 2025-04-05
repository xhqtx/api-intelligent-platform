<template>
  <div class="home">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>用户总数</span>
            </div>
          </template>
          <div class="card-content">
            <h2>{{ stats.userCount || 0 }}</h2>
            <div class="trend">
              <span :class="{ 'up': stats.userGrowth > 0, 'down': stats.userGrowth < 0 }">
                {{ stats.userGrowth > 0 ? '+' : '' }}{{ stats.userGrowth || 0 }}%
              </span>
              <span class="trend-text">较上月</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Key /></el-icon>
              <span>权限总数</span>
            </div>
          </template>
          <div class="card-content">
            <h2>{{ stats.permissionCount || 0 }}</h2>
            <div class="trend">
              <span :class="{ 'up': stats.permissionGrowth > 0, 'down': stats.permissionGrowth < 0 }">
                {{ stats.permissionGrowth > 0 ? '+' : '' }}{{ stats.permissionGrowth || 0 }}%
              </span>
              <span class="trend-text">较上月</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>权限组总数</span>
            </div>
          </template>
          <div class="card-content">
            <h2>{{ stats.groupCount || 0 }}</h2>
            <div class="trend">
              <span :class="{ 'up': stats.groupGrowth > 0, 'down': stats.groupGrowth < 0 }">
                {{ stats.groupGrowth > 0 ? '+' : '' }}{{ stats.groupGrowth || 0 }}%
              </span>
              <span class="trend-text">较上月</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>字典总数</span>
            </div>
          </template>
          <div class="card-content">
            <h2>{{ stats.dictCount || 0 }}</h2>
            <div class="trend">
              <span :class="{ 'up': stats.dictGrowth > 0, 'down': stats.dictGrowth < 0 }">
                {{ stats.dictGrowth > 0 ? '+' : '' }}{{ stats.dictGrowth || 0 }}%
              </span>
              <span class="trend-text">较上月</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 权限变更日志 -->
    <el-card class="log-card">
      <template #header>
        <div class="card-header">
          <span>最近权限变更</span>
          <el-button type="text" @click="viewAllLogs">查看全部</el-button>
        </div>
      </template>
      <el-table :data="recentLogs" stripe style="width: 100%">
        <el-table-column prop="timestamp" label="时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getChangeTypeTag(scope.row.type)">
              {{ scope.row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="operator" label="操作人" width="120" />
      </el-table>
    </el-card>

    <!-- 系统状态 -->
    <el-card class="status-card">
      <template #header>
        <div class="card-header">
          <span>系统状态</span>
          <el-button type="text" @click="refreshStatus">刷新</el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="status-item">
            <div class="status-label">CPU 使用率</div>
            <el-progress 
              :percentage="systemStatus.cpuUsage" 
              :color="getProgressColor(systemStatus.cpuUsage)"
            />
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-item">
            <div class="status-label">内存使用率</div>
            <el-progress 
              :percentage="systemStatus.memoryUsage"
              :color="getProgressColor(systemStatus.memoryUsage)"
            />
          </div>
        </el-col>
        <el-col :span="8">
          <div class="status-item">
            <div class="status-label">磁盘使用率</div>
            <el-progress 
              :percentage="systemStatus.diskUsage"
              :color="getProgressColor(systemStatus.diskUsage)"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { User, Key, List, Document } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

interface LogEntry {
  timestamp: number;
  type: string;
  description: string;
  operator: string;
}

export default defineComponent({
  name: 'HomeView',
  components: {
    User,
    Key,
    List,
    Document
  },
  setup() {
    const router = useRouter();
    const stats = ref({
      userCount: 0,
      userGrowth: 0,
      permissionCount: 0,
      permissionGrowth: 0,
      groupCount: 0,
      groupGrowth: 0,
      dictCount: 0,
      dictGrowth: 0
    });

    const recentLogs = ref<LogEntry[]>([]);
    const systemStatus = ref({
      cpuUsage: 0,
      memoryUsage: 0,
      diskUsage: 0
    });

    const fetchStats = async () => {
      try {
        // TODO: 实现获取统计数据的API调用
        // 临时模拟数据
        stats.value = {
          userCount: 156,
          userGrowth: 5.2,
          permissionCount: 324,
          permissionGrowth: -2.1,
          groupCount: 45,
          groupGrowth: 1.8,
          dictCount: 89,
          dictGrowth: 0.5
        };
      } catch (error) {
        ElMessage.error('获取统计数据失败');
      }
    };

    const fetchRecentLogs = async () => {
      try {
        // TODO: 实现获取最近日志的API调用
        // 临时模拟数据
        recentLogs.value = [
          {
            timestamp: new Date().getTime(),
            type: '新增',
            description: '新增用户权限：访问仪表盘',
            operator: '管理员'
          },
          // 更多日志数据...
        ];
      } catch (error) {
        ElMessage.error('获取日志数据失败');
      }
    };

    const fetchSystemStatus = async () => {
      try {
        // TODO: 实现获取系统状态的API调用
        // 临时模拟数据
        systemStatus.value = {
          cpuUsage: 45,
          memoryUsage: 62,
          diskUsage: 78
        };
      } catch (error) {
        ElMessage.error('获取系统状态失败');
      }
    };

    const formatDate = (timestamp: number) => {
      return new Date(timestamp).toLocaleString();
    };

    const getChangeTypeTag = (type: string) => {
      const types: { [key: string]: string } = {
        '新增': 'success',
        '修改': 'warning',
        '删除': 'danger'
      };
      return types[type] || 'info';
    };

    const getProgressColor = (value: number) => {
      if (value < 60) return '#67C23A';
      if (value < 80) return '#E6A23C';
      return '#F56C6C';
    };

    const viewAllLogs = () => {
      router.push('/permission-logs');
    };

    const refreshStatus = async () => {
      await fetchSystemStatus();
      ElMessage.success('系统状态已更新');
    };

    onMounted(async () => {
      await Promise.all([
        fetchStats(),
        fetchRecentLogs(),
        fetchSystemStatus()
      ]);
    });

    return {
      stats,
      recentLogs,
      systemStatus,
      formatDate,
      getChangeTypeTag,
      getProgressColor,
      viewAllLogs,
      refreshStatus
    };
  }
});
</script>

<style lang="scss" scoped>
.home {
  .el-row {
    margin-bottom: 20px;
  }

  .stat-card {
    margin-bottom: 20px;
    .card-header {
      display: flex;
      align-items: center;
      .el-icon {
        margin-right: 8px;
        font-size: 20px;
      }
    }
    .card-content {
      text-align: center;
      h2 {
        font-size: 28px;
        margin: 10px 0;
        color: var(--text-color);
      }
      .trend {
        font-size: 14px;
        .up {
          color: #67C23A;
        }
        .down {
          color: #F56C6C;
        }
        .trend-text {
          margin-left: 5px;
          color: #909399;
        }
      }
    }
  }

  .log-card {
    margin-bottom: 20px;
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .status-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .status-item {
      margin-bottom: 15px;
      .status-label {
        margin-bottom: 5px;
        color: var(--text-color);
      }
    }
  }
}

// 响应式布局
@media (max-width: 768px) {
  .home {
    .el-card {
      margin-bottom: 15px;
    }
  }
}
</style>