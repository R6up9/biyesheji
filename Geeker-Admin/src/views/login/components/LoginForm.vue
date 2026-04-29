<template>
  <div>
    <div class="form-container">
      <!-- 登录表单 -->
      <div v-show="innerTab === 'login'" key="login">
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
          <el-form-item prop="username" :show-message="false">
            <el-input v-model="loginForm.username" placeholder="用户名：admin">
              <template #prefix>
                <el-icon class="el-input__icon">
                  <user />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password" :show-message="false">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码：123456"
              show-password
              autocomplete="new-password"
            >
              <template #prefix>
                <el-icon class="el-input__icon">
                  <lock />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
        <div class="login-btn">
          <el-button :icon="CircleClose" round size="large" @click="resetForm(loginFormRef)"> 重置 </el-button>
          <el-button :icon="UserFilled" round size="large" type="primary" :loading="loading" @click="login(loginFormRef)">
            登录
          </el-button>
        </div>
        <div class="switch-link">
          还没有账号？<el-button type="primary" text @click="switchTab('register')">立即注册</el-button>
        </div>
      </div>

      <!-- 注册表单 -->
      <div v-show="innerTab === 'register'" key="register">
        <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" size="large" label-width="80px">
          <el-form-item label="账号" prop="username" :show-message="false">
            <el-input v-model="registerForm.username" placeholder="请输入账号" />
          </el-form-item>
          <el-form-item label="密码" prop="password" :show-message="false">
            <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword" :show-message="false">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName" :show-message="false">
            <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="性别" prop="gender" :show-message="false">
            <el-radio-group v-model="registerForm.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="年龄" prop="age" :show-message="false">
            <el-input v-model="registerForm.age" placeholder="请输入年龄" type="number" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone" :show-message="false">
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>
        <div class="login-btn">
          <el-button type="primary" round size="large" :loading="registerLoading" @click="handleRegister">
            注册
          </el-button>
        </div>
        <div class="switch-link">
          已有账号？<el-button type="primary" text @click="switchTab('login')">立即登录</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTimeState, parseRedirect } from '@/utils'
import { ElNotification, ElMessage } from 'element-plus'
import { AuthApi, type ReqLoginForm, type ReqRegisterForm } from '@/api/auth'
import { useUserStore } from '@/stores/modules/user'
import { useTabsStore } from '@/stores/modules/tabs'
import { useKeepAliveStore } from '@/stores/modules/keepAlive'
import { initDynamicRouter } from '@/routers/modules/dynamicRouter'
import { CircleClose, UserFilled } from '@element-plus/icons-vue'
import type { ElForm } from 'element-plus'
import { useLoadingStore } from '@/stores/modules/loading'
import { storeToRefs } from 'pinia'
import { useDictStore } from '@/stores/modules/dict'
import { useAuthStore } from '@/stores/modules/auth'
import { LOGIN_URL } from '@/config'

const props = defineProps<{
  modelValue: 'login' | 'register'
}>()
const emit = defineEmits<{
  (e: 'update:modelValue', value: 'login' | 'register'): void
}>()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const authStore = useAuthStore()
const tabsStore = useTabsStore()
const keepAliveStore = useKeepAliveStore()

// 当前tab状态
const innerTab = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const switchTab = (tab: 'login' | 'register') => {
  innerTab.value = tab
}

type FormInstance = InstanceType<typeof ElForm>
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

// 登录表单
const loginRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
})

const { loading } = storeToRefs(useLoadingStore())
const loginForm = reactive<ReqLoginForm>({
  username: 'admin',
  password: '123456',
})

// 注册表单
const registerLoading = ref(false)
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
  gender: 1,
  age: '',
  phone: '',
})

const login = (formEl: FormInstance | undefined) => {
  if (!formEl) {
    return
  }
  formEl.validate(async valid => {
    if (!valid) {
      return
    }
    try {
      // 先清除旧的登录状态
      userStore.clearUserInfo()
      
      const res: any = await AuthApi.login(loginForm)
      
      userStore.setUserAndRole(res)

      // 所有用户都使用动态路由系统
      await authStore.getAuthMenuList()
      await authStore.getAuthButtonList()
      await initDynamicRouter()
      useDictStore().getAllDict()

      tabsStore.setTabs([])
      keepAliveStore.setKeepAliveName([])

      // 等待一小段时间确保路由已完全添加
      await new Promise(resolve => setTimeout(resolve, 200))
      
      // 根据角色跳转到不同首页
      if (res.role === 0) {
        router.push('/home')
      } else {
        router.push('/client/home')
      }

      ElNotification({
        title: getTimeState(),
        message: '欢迎登录',
        type: 'success',
        duration: 3000,
      })
    } catch (error) {
      console.error('Login failed:', error)
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async valid => {
    if (!valid) return
    
    registerLoading.value = true
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
      innerTab.value = 'login'
    } catch (error) {
      console.error('Register failed:', error)
    } finally {
      registerLoading.value = false
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) {
    return
  }
  formEl.resetFields()
}

onMounted(() => {
  document.onkeydown = (e: KeyboardEvent) => {
    if (e.code === 'Enter' || e.code === 'enter' || e.code === 'NumpadEnter') {
      if (loading.value) {
        return
      }
      if (innerTab.value === 'login') {
        login(loginFormRef.value)
      }
    }
  }
})

onBeforeUnmount(() => {
  document.onkeydown = null
})
</script>

<style scoped lang="scss">
@use '../index';

.form-container {
  width: 100%;
}

.form-row {
  display: flex;
  gap: 16px;
  
  .form-item-half {
    flex: 1;
  }
}

/* 性别单选按钮横向排列 */
:deep(.el-radio-group) {
  display: flex;
  gap: 16px;
}

/* 更小的表单项间距 */
:deep(.el-form-item) {
  margin-bottom: 8px !important;
}

/* 缩小验证错误提示 */
:deep(.el-form-item__error) {
  font-size: 11px !important;
  padding-top: 2px !important;
}

/* 登录按钮100%居中 */
:deep(.login-btn) {
  display: flex;
  justify-content: center !important;
  align-items: center !important;
  gap: 12px;
  width: 100%;
  text-align: center;
}

/* 切换链接 - 更缩小、间距更短 */
.switch-link {
  text-align: center;
  margin-top: 4px;
  font-size: 11px;
  color: #909399;
  
  .el-button {
    font-size: 11px !important;
    padding: 0 6px !important;
    height: auto !important;
  }
}
</style>
