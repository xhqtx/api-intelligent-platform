<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">API智能平台管理系统</h2>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0px">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" prefix-icon="el-icon-user" placeholder="管理员用户名"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" prefix-icon="el-icon-lock" type="password" placeholder="密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-button" @click="handleLogin" :loading="loading">{{ loading ? '登录中...' : '登录' }}</el-button>
        </el-form-item>
        <div class="login-options">
          <el-button type="text" @click="goToRegister">没有账号？去注册</el-button>
        </div>
        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-button type="text" @click="goToForgotPassword">忘记密码?</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';
import AuthService from '@/services/auth';

export default defineComponent({
  name: 'LoginView',
  setup() {
    const router = useRouter();
    const store = useStore();
    const loginFormRef = ref<HTMLFormElement | null>(null);
    const rememberMe = ref(false);
    const loading = ref(false);
    
    const loginForm = ref({
      username: '',
      password: ''
    });
    
    const loginRules = {
      username: [
        { required: true, message: '请输入管理员用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ]
    };
    
    const handleLogin = async () => {
      if (loginFormRef.value) {
        try {
          const valid = await loginFormRef.value.validate();
          if (valid) {
            loading.value = true;
            const response = await AuthService.login(loginForm.value);
            
            if (response.success) {
              // 记住用户名
              if (rememberMe.value) {
                localStorage.setItem('remembered_username', loginForm.value.username);
              } else {
                localStorage.removeItem('remembered_username');
              }
              
              const userInfo = await store.getters.currentUser;
              ElMessage.success(`欢迎回来，${userInfo.username || '管理员'}！`);
              router.push('/'); // 登录成功后跳转到首页
            } else {
              ElMessage.error(response.message || '登录失败，请检查用户名和密码');
            }
          }
        } catch (error: unknown) {
          if (error instanceof Error) {
            ElMessage.error(error.message || '登录失败，请检查用户名和密码');
          } else {
            ElMessage.error('登录失败，请检查用户名和密码');
          }
        } finally {
          loading.value = false;
        }
      }
    };
    
    // 检查是否有记住的用户名
    const rememberedUsername = localStorage.getItem('remembered_username');
    if (rememberedUsername) {
      loginForm.value.username = rememberedUsername;
      rememberMe.value = true;
    }

    const goToRegister = () => {
      router.push('/register');
    };

    const goToForgotPassword = () => {
      router.push('/forgot-password');
    };

    return {
      loginFormRef,
      rememberMe,
      loginForm,
      loginRules,
      handleLogin,
      loading,
      goToRegister,
      goToForgotPassword
    };
  }
});
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.login-card {
  width: 400px;
  padding: 20px;
}
.login-title {
  text-align: center;
  margin-bottom: 20px;
  color: #409EFF;
}
.login-button {
  width: 100%;
}

.login-options {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.login-options {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}
</style>