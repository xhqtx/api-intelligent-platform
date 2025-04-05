<template>
  <div class="search-form">
    <el-form :model="modelValue" :inline="inline" @submit.prevent="handleSearch">
      <slot></slot>
      <el-form-item>
        <div class="action-buttons">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

export default defineComponent({
  name: 'SearchForm',
  components: {
    Search,
    Refresh
  },
  props: {
    modelValue: {
      type: Object,
      required: true
    },
    inline: {
      type: Boolean,
      default: true
    }
  },
  emits: ['search', 'reset', 'update:modelValue'],
  setup(props, { emit }) {
    const handleSearch = () => {
      emit('search', props.modelValue)
    }

    const handleReset = () => {
      emit('update:modelValue', {})
      emit('reset')
    }

    return {
      handleSearch,
      handleReset
    }
  }
})
</script>

<style scoped>
/* 样式已经在 global.scss 中定义 */
</style>