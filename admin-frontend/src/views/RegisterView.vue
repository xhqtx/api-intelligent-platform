<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="register-title">管理员注册</h2>
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="0px">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" prefix-icon="el-icon-user" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="registerForm.email" prefix-icon="el-icon-message" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" prefix-icon="el-icon-lock" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" prefix-icon="el-icon-lock" type="password" placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="register-button" @click="handleRegister" :loading="loading">
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
        <div class="register-options">
          <el-button type="text" @click="goToLogin">已有账号？去登录</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import AuthService from '@/services/auth';

export default defineComponent({
  name: 'RegisterView',
  setup() {
    const router = useRouter();
    const registerFormRef = ref<HTMLFormElement | null>(null);
    const loading = ref(false);
    
    const registerForm = ref({
      username: '',
      email: '',
      password: '',
      confirmPassword: ''
    });
    
    // 自定义密码确认验证规则
    const validateConfirmPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
      if (value === '') {
        callback(new Error('请再次输入密码'));
      } else if (value !== registerForm.value.password) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    };
    
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ]
    };
    
    const handleRegister = async () => {
      if (registerFormRef.value) {
        try {
          const valid = await registerFormRef.value.validate();
          if (valid) {
            loading.value = true;
            const { ...registerData } = registerForm.value;
            const response = await AuthService.register(registerData);
            
            if (response.success) {
              ElMessage.success(response.message || '注册成功，请登录');
              router.push('/login');
            } else {
              ElMessage.error(response.message || '注册失败，请重试');
            }
          }
        } catch (error: unknown) {
          if (error instanceof Error) {
            ElMessage.error(error.message || '注册失败，请重试');
          } else {
            ElMessage.error('注册失败，请重试');
          }
        } finally {
          loading.value = false;
        }
      }
    };
    
    const goToLogin = () => {
      router.push('/login');
    };
    
    return {
      registerFormRef,
      registerForm,
      registerRules,
      handleRegister,
      goToLogin,
      loading
    };
  }
});
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.register-card {
  width: 400px;
  padding: 20px;
}
.register-title {
  text-align: center;
  margin-bottom: 20px;
  color: #409EFF;
}
.register-button {
  width: 100%;
}
.register-options {
  display: flex;
  justify-content: center;
  margin-top: 15px;
}
</style>