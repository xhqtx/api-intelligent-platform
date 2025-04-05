<template>
  <div class="theme-switcher">
    <el-dropdown trigger="click" @command="handleThemeChange">
      <el-button class="theme-button">
        <el-icon><Brush /></el-icon>
        主题设置
        <el-icon class="el-icon--right"><arrow-down /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="toggleDarkMode">
            {{ isDarkMode ? '浅色模式' : '深色模式' }}
          </el-dropdown-item>
          <el-dropdown-item command="openColorPicker">自定义主题色</el-dropdown-item>
          <el-dropdown-item command="toggleCompactMode">
            {{ isCompactMode ? '正常模式' : '紧凑模式' }}
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    
    <el-dialog v-model="colorPickerVisible" title="自定义主题色" width="300px">
      <el-color-picker v-model="primaryColor" show-alpha @change="handleColorChange" />
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { defineComponent, computed, ref } from 'vue';
import { useStore } from 'vuex';
import { Brush, ArrowDown } from '@element-plus/icons-vue';

export default defineComponent({
  name: 'ThemeSwitcher',
  components: {
    Brush,
    ArrowDown
  },
  setup() {
    const store = useStore();
    
    const isDarkMode = computed(() => store.state.isDarkMode);
    const isCompactMode = computed(() => store.state.isCompactMode);
    const primaryColor = ref(store.state.primaryColor || '#409EFF');
    const colorPickerVisible = ref(false);
    
    const toggleDarkMode = () => {
      store.dispatch('toggleDarkMode');
      localStorage.setItem('darkMode', (!isDarkMode.value).toString());
      applyTheme();
    };
    
    const toggleCompactMode = () => {
      store.dispatch('toggleCompactMode');
      localStorage.setItem('compactMode', (!isCompactMode.value).toString());
      applyTheme();
    };
    
    const openColorPicker = () => {
      colorPickerVisible.value = true;
    };
    
    const handleColorChange = (color: string) => {
      store.dispatch('setPrimaryColor', color);
      localStorage.setItem('primaryColor', color);
      applyTheme();
    };
    
    const applyTheme = () => {
      const root = document.documentElement;
      root.style.setProperty('--el-color-primary', primaryColor.value);
      root.classList.toggle('dark-mode', isDarkMode.value);
      root.classList.toggle('compact-mode', isCompactMode.value);
    };
    
    const handleThemeChange = (command: string) => {
      switch (command) {
        case 'toggleDarkMode':
          toggleDarkMode();
          break;
        case 'openColorPicker':
          openColorPicker();
          break;
        case 'toggleCompactMode':
          toggleCompactMode();
          break;
      }
    };
    
    // 初始化主题
    applyTheme();
    
    return {
      isDarkMode,
      isCompactMode,
      primaryColor,
      colorPickerVisible,
      handleThemeChange,
      handleColorChange
    };
  }
});
</script>

<style lang="scss" scoped>
.theme-switcher {
  margin-left: 16px;
}

.theme-button {
  @include transition(all);
  display: flex;
  align-items: center;
  
  &:hover {
    opacity: 0.8;
  }
}

:deep(.el-color-picker__trigger) {
  width: 100%;
  height: 40px;
}
</style>