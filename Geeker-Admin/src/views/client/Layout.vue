<template>
  <el-container class="client-container">
    <el-header class="client-header">
      <div class="header-left">
        <h1 class="logo">羽毛球俱乐部</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-name">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo?.username || '用户' }}
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <el-aside width="200px" class="client-aside">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/client/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/client/court">
            <el-icon><Location /></el-icon>
            <span>场地预约</span>
          </el-menu-item>
          <el-menu-item index="/client/course">
            <el-icon><Reading /></el-icon>
            <span>课程报名</span>
          </el-menu-item>
          <el-menu-item index="/client/coach">
            <el-icon><UserFilled /></el-icon>
            <span>教练信息</span>
          </el-menu-item>
          <el-menu-item index="/client/product">
            <el-icon><Goods /></el-icon>
            <span>商品查看</span>
          </el-menu-item>
          <el-menu-item index="/client/booking">
            <el-icon><Calendar /></el-icon>
            <span>我的预约</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main class="client-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { HomeFilled, Location, Reading, UserFilled, Goods, Calendar, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import { LOGIN_URL } from '@/config'

defineOptions({ name: 'ClientLayout' })

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/client/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push(LOGIN_URL)
      ElMessage.success('已退出登录')
    })
  }
}
</script>

<style scoped lang="scss">
.client-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.client-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  height: 60px;
}

.header-left {
  .logo {
    color: white;
    font-size: 24px;
    margin: 0;
  }
}

.header-right {
  .user-name {
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.main-container {
  flex: 1;
  display: flex;
}

.client-aside {
  background: #304156;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.client-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
