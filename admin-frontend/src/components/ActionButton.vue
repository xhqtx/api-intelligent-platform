<template>
  <el-button
    :type="type"
    :size="size"
    :disabled="disabled"
    :loading="loading"
    @click="handleClick"
    class="action-button"
  >
    <el-icon v-if="icon" :size="iconSize">
      <component :is="icon" />
    </el-icon>
    <span v-if="$slots.default || text" :class="{ 'ml-1': icon }">
      <slot>{{ text }}</slot>
    </span>
  </el-button>
</template>

<script lang="ts" setup>
import { computed, withDefaults, defineProps, defineEmits } from 'vue'
import { ElMessageBox } from 'element-plus'

const props = withDefaults(defineProps<{
  type?: string
  size?: string
  icon?: string | object
  text?: string
  disabled?: boolean
  loading?: boolean
  confirmText?: string
  confirmType?: 'success' | 'warning' | 'info' | 'error'
}>(), {
  type: 'primary',
  size: 'small',
  icon: null,
  text: '',
  disabled: false,
  loading: false,
  confirmText: '',
  confirmType: 'warning'
})

const emit = defineEmits<{
  (e: 'click', event: MouseEvent): void
}>()

const iconSize = computed(() => {
  switch (props.size) {
    case 'large':
      return 18
    case 'small':
      return 14
    case 'mini':
      return 12
    default:
      return 16
  }
})

const handleClick = (event: MouseEvent) => {
  if (props.confirmText) {
    ElMessageBox.confirm(
      props.confirmText,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: props.confirmType as 'success' | 'warning' | 'info' | 'error',
        title: '提示'
      }
    )
      .then(() => {
        emit('click', event)
      })
      .catch(() => {
        // 用户取消操作
      })
  } else {
    emit('click', event)
  }
}
</script>

<style lang="scss" scoped>
.action-button {
  &.el-button--mini {
    padding: 4px 8px;
    font-size: 12px;
  }
  
  &.el-button--small {
    padding: 6px 10px;
    font-size: 12px;
    height: 28px;
    line-height: 1;
  }

  .ml-1 {
    margin-left: 4px;
  }
}
</style>