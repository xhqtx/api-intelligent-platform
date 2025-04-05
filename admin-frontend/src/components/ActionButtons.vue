<template>
  <div class="action-buttons">
    <slot />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'ActionButtons'
})
</script>

<style lang="scss" scoped>
.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  
  :deep(.el-button) {
    margin: 0;  // 覆盖 element-plus 默认的 margin
  }
}
</style>