<template>
  <div class="data-table">
    <el-table
      v-bind="$attrs"
      v-loading="loading"
      :data="data"
      border
      stripe
      style="width: 100%"
    >
      <slot></slot>
      <!-- 操作列 -->
      <el-table-column
        v-if="$slots.actions"
        label="操作"
        :width="actionsWidth"
        :fixed="actionsFixed ? 'right' : false"
      >
        <template #default="scope">
          <slot name="actions" :row="scope.row" :$index="scope.$index"></slot>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-if="showPagination"
      v-model:currentPage="currentPage"
      v-model:pageSize="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, watch } from 'vue'

export default defineComponent({
  name: 'DataTable',
  props: {
    data: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    },
    showPagination: {
      type: Boolean,
      default: true
    },
    total: {
      type: Number,
      default: 0
    },
    actionsWidth: {
      type: String,
      default: '150'
    },
    actionsFixed: {
      type: Boolean,
      default: true
    }
  },
  emits: ['update:current-page', 'update:pageSize', 'page-changed'],
  setup(props, { emit }) {
    const currentPage = ref(1)
    const pageSize = ref(10)

    const handleSizeChange = (val: number) => {
      pageSize.value = val
      emit('update:pageSize', val)
      console.log('DataTable - Size changed to:', val)
    }

    const handleCurrentChange = (val: number) => {
      currentPage.value = val
      emit('update:current-page', val)
      emit('page-changed', val)
      console.log('DataTable - Page changed to:', val)
    }

    watch(() => props.total, (newVal) => {
      if (newVal === 0) {
        currentPage.value = 1
      }
    })

    return {
      currentPage,
      pageSize,
      handleSizeChange,
      handleCurrentChange
    }
  }
})
</script>

<style lang="scss" scoped>
.data-table {
  .el-pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>