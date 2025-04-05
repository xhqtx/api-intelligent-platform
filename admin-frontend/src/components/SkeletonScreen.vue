&lt;template>
  &lt;div class="skeleton-container" :class="type">
    &lt;template v-if="type === 'table'">
      &lt;el-skeleton :rows="rowCount" animated>
        &lt;template #template>
          &lt;el-skeleton-item v-for="i in rowCount" :key="i" variant="p" style="width: 100%; height: 40px; margin-top: 8px;" />
        &lt;/template>
      &lt;/el-skeleton>
    &lt;/template>

    &lt;template v-else-if="type === 'form'">
      &lt;el-skeleton animated>
        &lt;template #template>
          &lt;div v-for="i in rowCount" :key="i" style="margin-bottom: 16px;">
            &lt;el-skeleton-item variant="text" style="width: 30%; margin-right: 16px;" />
            &lt;el-skeleton-item variant="p" style="width: 70%;" />
          &lt;/div>
        &lt;/template>
      &lt;/el-skeleton>
    &lt;/template>

    &lt;template v-else-if="type === 'card'">
      &lt;div class="card-skeleton-container">
        &lt;div v-for="i in cardCount" :key="i" class="card-skeleton">
          &lt;el-skeleton animated>
            &lt;template #template>
              &lt;el-skeleton-item variant="image" style="width: 100%; height: 120px;" />
              &lt;div style="padding: 14px;">
                &lt;el-skeleton-item variant="h3" style="width: 50%;" />
                &lt;div style="display: flex; align-items: center; justify-content: space-between; margin-top: 16px; height: 16px;">
                  &lt;el-skeleton-item variant="text" style="width: 60%;" />
                  &lt;el-skeleton-item variant="text" style="width: 30%;" />
                &lt;/div>
              &lt;/div>
            &lt;/template>
          &lt;/el-skeleton>
        &lt;/div>
      &lt;/div>
    &lt;/template>

    &lt;template v-else>
      &lt;el-skeleton :rows="rowCount" animated />
    &lt;/template>
  &lt;/div>
&lt;/template>

&lt;script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'SkeletonScreen',
  props: {
    type: {
      type: String,
      default: 'default',
      validator: (value: string) => ['default', 'table', 'form', 'card'].includes(value)
    },
    rowCount: {
      type: Number,
      default: 4
    },
    cardCount: {
      type: Number,
      default: 3
    }
  }
})
&lt;/script>

&lt;style lang="scss" scoped>
.skeleton-container {
  width: 100%;
  
  &.table {
    padding: 16px;
    background: var(--background-color);
    border-radius: 4px;
    box-shadow: var(--box-shadow-light);
  }
  
  &.card {
    .card-skeleton-container {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 16px;
      
      .card-skeleton {
        border-radius: 4px;
        overflow: hidden;
        background: var(--background-color);
        box-shadow: var(--box-shadow-light);
      }
    }
  }
}

@media (max-width: 768px) {
  .skeleton-container.card {
    .card-skeleton-container {
      grid-template-columns: 1fr;
    }
  }
}
&lt;/style>