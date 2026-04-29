<template>
  <div class="register-container">
    <div class="register-header">
      <h2>会员注册</h2>
      <p>注册成为会员，享受更多服务</p>
    </div>
    <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large" label-width="100px">
      <el-divider content-position="left">账号信息</el-divider>
      <el-form-item label="登录账号" prop="username">
        <el-input v-model="registerForm.username" placeholder="请输入登录账号" />
      </el-form-item>
      <el-form-item label="登录密码" prop="password">
        <el-input v-model="registerForm.password" type="password" placeholder="请输入登录密码" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
      </el-form-item>
      <el-divider content-position="left">个人信息</el-divider>
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="registerForm.gender">
          <el-radio :value="0">未知</el-radio>
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="年龄" prop="age">
        <el-input-number v-model="registerForm.age" :min="1" :max="120" style="width: 100%" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
      </el-form-item>
    </el-form>
    <div class="register-btn">
      <el-button round size="large" @click="goToLogin"> 返回登录 </el-button>
      <el-button type="primary" round size="large" :loading="loading" @click="handleRegister"> 注册 </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { AuthApi, type ReqRegisterForm } from '@/api/auth'
import type { ElForm } from 'element-plus'

const router = useRouter()
type FormInstance = InstanceType<typeof ElForm>

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = reactive({
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
})

const registerForm = reactive<ReqRegisterForm>({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  gender: 0,
  age: undefined,
  phone: '',
})

const goToLogin = () => {
  router.push('/login')
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async valid => {
    if (!valid) return
    
    loading.value = true
    try {
      await AuthApi.register({
        username: registerForm.username,
        password: registerForm.password,
        realName: registerForm.realName,
        gender: registerForm.gender,
        age: registerForm.age,
        phone: registerForm.phone,
      })
      ElMessage.success('注册成功！请登录')
      router.push('/login')
    } catch (error) {
      console.error('Register failed:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.register-container {
  width: 100%;
  
  .register-header {
    display: none;
  }
  
  .register-btn {
    display: flex;
    gap: 16px;
    margin-top: 32px;
    
    .el-button {
      flex: 1;
      height: 48px;
      font-size: 16px;
    }
  }
}
</style>
