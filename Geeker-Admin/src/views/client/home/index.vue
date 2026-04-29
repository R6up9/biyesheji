<template>
  <div class="client-home">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card">
          <h2>欢迎来到羽毛球俱乐部，{{ username }}！</h2>
          <p>在这里您可以预约场地、报名课程、查看商品和管理您的预约记录。</p>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6" v-for="(item, index) in quickLinks" :key="index">
        <el-card shadow="hover" class="quick-card" @click="goTo(item.path)">
          <div class="card-icon" :style="{ background: item.color }">
            <el-icon :size="40"><component :is="item.icon" /></el-icon>
          </div>
          <div class="card-title">{{ item.title }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Reading, UserFilled, Goods, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'

defineOptions({ name: 'ClientHome' })

const router = useRouter()
const userStore = useUserStore()

const username = ref(userStore.userInfo?.username || '用户')

const quickLinks = [
  { title: '场地预约', path: '/client/court', icon: Location, color: '#409eff' },
  { title: '课程报名', path: '/client/course', icon: Reading, color: '#67c23a' },
  { title: '教练信息', path: '/client/coach', icon: UserFilled, color: '#e6a23c' },
  { title: '商品查看', path: '/client/product', icon: Goods, color: '#f56c6c' },
  { title: '我的预约', path: '/client/booking', icon: Calendar, color: '#909399' }
]

const goTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.client-home {
  .welcome-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    
    h2 {
      margin: 0 0 10px 0;
    }
    
    p {
      margin: 0;
      opacity: 0.9;
    }
  }
  
  .quick-card {
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
    }
    
    .card-icon {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 20px auto 15px;
      color: white;
    }
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}
</style>
