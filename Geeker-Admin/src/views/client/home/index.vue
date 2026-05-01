<template>
  <div class="client-home">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ username }}！</h2>
          <p>在这里预约场地、报名课程、购买商品，享受羽毛球的快乐！</p>
        </div>
        <div class="welcome-icon">🏸</div>
      </div>
    </el-card>
    
    <!-- 快捷功能 -->
    <div class="quick-section">
      <h3 class="section-title">快捷服务</h3>
      <el-row :gutter="16">
        <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="(item, index) in quickLinks" :key="index">
          <el-card shadow="hover" class="quick-card" @click="goTo(item.path)">
            <div class="card-icon" :style="{ background: item.color }">
              <el-icon :size="32"><component :is="item.icon" /></el-icon>
            </div>
            <div class="card-title">{{ item.title }}</div>
            <div class="card-desc">{{ item.desc }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 功能指引 -->
    <div class="guide-section">
      <el-card class="guide-card" shadow="never">
        <h3 class="section-title">使用指南</h3>
        <div class="guide-grid">
          <div class="guide-item">
            <div class="guide-num">1</div>
            <div class="guide-content">
              <h4>选择场地或课程</h4>
              <p>根据您的需求选择合适的羽毛球场地或课程</p>
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-num">2</div>
            <div class="guide-content">
              <h4>预约时间段</h4>
              <p>选择您方便的时间段进行预约</p>
            </div>
          </div>
          <div class="guide-item">
            <div class="guide-num">3</div>
            <div class="guide-content">
              <h4>确认并支付</h4>
              <p>确认预约信息并使用余额完成支付</p>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Reading, UserFilled, Goods, Calendar, Trophy } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import bgImage from '@/assets/images/VCG211321008927.jpg'

defineOptions({ name: 'ClientHome' })

const router = useRouter()
const userStore = useUserStore()

const username = ref(userStore.userInfo?.username || '用户')

const quickLinks = [
  { 
    title: '场地预约', 
    path: '/client/court', 
    icon: Location, 
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    desc: '快速预约球场'
  },
  { 
    title: '课程报名', 
    path: '/client/course', 
    icon: Reading, 
    color: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    desc: '参加专业训练'
  },
  { 
    title: '教练信息', 
    path: '/client/coach', 
    icon: UserFilled, 
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    desc: '了解专业教练'
  },
  { 
    title: '商品查看', 
    path: '/client/product', 
    icon: Goods, 
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    desc: '购买运动器材'
  },
  { 
    title: '活动报名', 
    path: '/client/activity', 
    icon: Trophy, 
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    desc: '参与精彩活动'
  },
  { 
    title: '我的预约', 
    path: '/client/booking', 
    icon: Calendar, 
    color: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    desc: '查看预约记录'
  }
]

const goTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.client-home {
  .welcome-card {
    border: none;
    border-radius: 16px;
    margin-bottom: 24px;
    overflow: hidden;
    position: relative;
    
    .welcome-bg {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-size: cover;
      background-position: center;
      z-index: 1;
    }
    
    .welcome-content {
      position: relative;
      z-index: 2;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px;
      
      .welcome-text {
        flex: 1;
        
        h2 {
          margin: 0 0 10px 0;
          color: white;
          font-size: 24px;
          font-weight: 700;
        }
        
        p {
          margin: 0;
          color: rgba(255, 255, 255, 0.85);
          font-size: 15px;
        }
      }
      
      .welcome-icon {
        font-size: 72px;
        opacity: 0.9;
        filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1));
      }
    }
  }
  
  .quick-section {
    margin-bottom: 24px;
    
    .section-title {
      font-size: 18px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 16px;
    }
    
    .quick-card {
      text-align: center;
      cursor: pointer;
      transition: all 0.3s;
      border: none;
      border-radius: 12px;
      margin-bottom: 16px;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
      }
      
      .card-icon {
        width: 64px;
        height: 64px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 16px auto 12px;
        color: white;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .card-title {
        font-size: 16px;
        font-weight: 700;
        color: #1e293b;
        margin-bottom: 4px;
      }
      
      .card-desc {
        font-size: 13px;
        color: #94a3b8;
      }
    }
  }
  
  .guide-section {
    .guide-card {
      border: none;
      border-radius: 16px;
      
      .section-title {
        font-size: 18px;
        font-weight: 700;
        color: #1e293b;
        margin: 0 0 20px;
      }
      
      .guide-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
        gap: 20px;
        
        .guide-item {
          display: flex;
          gap: 16px;
          padding: 20px;
          background: #f8fafc;
          border-radius: 12px;
          
          .guide-num {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 20px;
            flex-shrink: 0;
          }
          
          .guide-content {
            flex: 1;
            
            h4 {
              margin: 0 0 6px;
              font-size: 15px;
              font-weight: 700;
              color: #1e293b;
            }
            
            p {
              margin: 0;
              font-size: 13px;
              color: #64748b;
              line-height: 1.6;
            }
          }
        }
      }
    }
  }
}
</style>
