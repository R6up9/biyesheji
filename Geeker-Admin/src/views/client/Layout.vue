<template>
  <el-container class="client-container">
    <el-header class="client-header">
      <div class="header-left">
        <div class="logo-container">
          <img class="logo-icon" src="@/assets/images/VCG211536356687.png" alt="logo" />
          <h1 class="logo">羽毛球俱乐部</h1>
        </div>
      </div>
      <div class="header-right">
        <div class="user-avatar" @click="handleCommand('profile')">
          <el-icon class="avatar-icon"><User /></el-icon>
        </div>
        <div class="user-info" @click="handleCommand('profile')">
          <div class="user-name">{{ userStore.userInfo?.username || '用户' }}</div>
          <div class="user-role">会员</div>
        </div>
        <el-dropdown @command="handleCommand">
          <el-icon class="more-icon"><ArrowDown /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                <span>个人中心</span>
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container class="main-container">
      <el-aside width="220px" class="client-aside">
        <div class="menu-scroll">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="transparent"
            text-color="#8b9eb0"
            active-text-color="#667eea"
            class="client-menu"
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
            <el-menu-item index="/client/activity">
              <el-icon><Trophy /></el-icon>
              <span>活动报名</span>
            </el-menu-item>
            <el-menu-item index="/client/booking">
              <el-icon><Calendar /></el-icon>
              <span>我的预约</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-aside>
      
      <el-main class="client-main">
        <div class="main-content">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { HomeFilled, Location, Reading, UserFilled, Goods, Trophy, Calendar, User, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
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
      type: 'warning',
      draggable: true
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
  background: #f8fafc;
}

.client-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  height: 68px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
  z-index: 100;
}

.header-left {
  .logo-container {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .logo-icon {
      width: 44px;
      height: 44px;
      border-radius: 8px;
      object-fit: contain;
      background: rgba(255, 255, 255, 0.9);
      padding: 4px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    
    .logo {
      color: white;
      font-size: 22px;
      font-weight: 700;
      margin: 0;
      letter-spacing: 0.5px;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .user-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.35);
      transform: scale(1.05);
    }
    
    .avatar-icon {
      font-size: 24px;
      color: white;
    }
  }
  
  .user-info {
    cursor: pointer;
    text-align: left;
    
    .user-name {
      color: white;
      font-weight: 600;
      font-size: 15px;
      line-height: 1.4;
    }
    
    .user-role {
      color: rgba(255, 255, 255, 0.8);
      font-size: 12px;
      line-height: 1.4;
    }
  }
  
  .more-icon {
    color: rgba(255, 255, 255, 0.9);
    font-size: 20px;
    cursor: pointer;
    padding: 6px;
    border-radius: 8px;
    transition: all 0.3s;
    
    &:hover {
      background: rgba(255, 255, 255, 0.15);
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
}

.client-aside {
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  height: calc(100vh - 68px);
  overflow-y: auto;
  border-right: 1px solid #e2e8f0;
  padding: 16px 0;
  
  .menu-scroll {
    height: 100%;
    overflow-y: auto;
  }
  
  .client-menu {
    border: none;
    
    :deep(.el-menu-item) {
      margin: 6px 12px;
      border-radius: 10px;
      height: 48px;
      line-height: 48px;
      transition: all 0.3s;
      
      &:hover {
        background: #f1f5f9;
      }
      
      &.is-active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        
        &:hover {
          background: linear-gradient(135deg, #7c8fef 0%, #875cb2 100%);
        }
        
        .el-icon {
          color: white;
        }
      }
      
      .el-icon {
        width: 20px;
        height: 20px;
        margin-right: 12px;
      }
    }
  }
}

.client-main {
  background: #f8fafc;
  padding: 0;
  overflow-y: auto;
  
  .main-content {
    padding: 24px;
    min-height: calc(100vh - 68px);
  }
}
</style>
