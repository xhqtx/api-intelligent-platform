<template>
  <div class="about">
    <h1>关于管理系统</h1>
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>系统信息</span>
        </div>
      </template>
      <div class="item">
        <p><strong>系统名称：</strong> API智能平台管理系统</p>
        <p><strong>版本号：</strong> 1.0.0</p>
        <p><strong>开发团队：</strong> API智能平台开发团队</p>
        <p><strong>联系方式：</strong> support@apiplatform.com</p>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'AboutView'
});
</script>

<style scoped>
.about {
  padding: 20px;
}
.box-card {
  max-width: 600px;
  margin: 0 auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item {
  margin-bottom: 18px;
}
</style>