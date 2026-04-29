<template>
  <div class="login-container flex justify-center items-center">
    <div class="login-box">
      <switch-dark class="dark" />
      <div class="login-left">
        <img class="login-left-img" src="@/assets/images/login_left.png" alt="login" />
      </div>
      <div class="login-form">
        <login-form v-model="activeTab" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Login' })
import { ref } from 'vue'
import LoginForm from './components/LoginForm.vue'
import SwitchDark from '@/components/SwitchDark/index.vue'

const activeTab = ref<'login' | 'register'>('login')
</script>

<style scoped lang="scss">
@use './index';
</style>
