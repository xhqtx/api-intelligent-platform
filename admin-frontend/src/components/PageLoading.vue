<template>
  <div class="page-loading" v-if="loading">
    <el-skeleton :rows="6" animated />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'PageLoading',
  props: {
    loading: {
      type: Boolean,
      default: false
    }
  }
})
</script>

<style lang="scss" scoped>
.page-loading {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>