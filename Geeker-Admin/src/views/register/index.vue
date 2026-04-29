<template>
  <div class="register-container flex justify-center items-center">
    <div class="register-box">
      <switch-dark class="dark" />
      <div class="register-left">
        <img class="register-left-img" src="@/assets/images/login_left.png" alt="register" />
      </div>
      <div class="register-form">
        <div class="register-logo">
          <img class="register-icon" src="@/assets/images/logo.svg" alt="" />
          <h2 class="logo-text">会员注册</h2>
        </div>
        <register-form />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Register' })
import RegisterForm from '../login/components/RegisterForm.vue'
import SwitchDark from '@/components/SwitchDark/index.vue'
</script>

<style scoped lang="scss">
.register-container {
  width: 100%;
  height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;

  .register-box {
    width: 500px;
    padding: 40px;
    border-radius: 16px;
    background-color: rgba(255, 255, 255, 0.95);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    position: relative;

    .dark {
      position: absolute;
      top: 15px;
      right: 15px;
    }

    .register-left {
      display: none;
    }

    .register-form {
      width: 100%;
      display: flex;
      flex-direction: column;

      .register-logo {
        width: 100%;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;

        .logo-text {
          padding: 10px 0;
          font-size: 24px;
          font-weight: bold;
          color: #303133;
        }
      }

      :deep(.el-form-item__label) {
        width: 100px !important;
      }
    }
  }
}
</style>
