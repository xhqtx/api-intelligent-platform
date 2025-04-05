<template>
  <div class="page-header">
    <h1>{{ title }}</h1>
    <slot name="actions"></slot>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'PageHeader',
  props: {
    title: {
      type: String,
      required: true
    }
  }
})
</script>

<style scoped>
/* 样式已经在 global.scss 中定义 */
</style>