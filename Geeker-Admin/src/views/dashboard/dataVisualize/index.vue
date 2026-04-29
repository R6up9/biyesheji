<template>
  <div class="dataVisualize-box">
    <div class="card top-box">
      <div class="top-title">
        <span>数据可视化</span>
        <el-button
          type="primary"
          :loading="loading"
          @click="handleRefresh"
          class="refresh-btn"
        >
          <el-icon><Refresh /></el-icon>
          <span>{{ loading ? '刷新中...' : '刷新数据' }}</span>
        </el-button>
      </div>
      <div class="top-content">
        <el-row :gutter="40">
          <el-col class="mb-10" :xs="24" :sm="12" :md="12" :lg="6" :xl="6">
            <div class="item-left truncate">
              <span class="left-title">总预订数</span>
              <div class="img-box">
                <img src="./images/book-sum.png" alt="" />
              </div>
              <span class="left-number">{{ stats.totalBookings || 0 }}</span>
            </div>
          </el-col>
          <el-col class="mb-10" :xs="24" :sm="12" :md="12" :lg="8" :xl="8">
            <div class="item-center">
              <div class="gitee-traffic traffic-box">
                <div class="traffic-img">
                  <img src="./images/add_person.png" alt="" />
                </div>
                <span class="item-value">{{ stats.todayBookings || 0 }}</span>
                <span class="traffic-name truncate">今日预订</span>
              </div>
              <div class="gitHub-traffic traffic-box">
                <div class="traffic-img">
                  <img src="./images/add_team.png" alt="" />
                </div>
                <span class="item-value">{{ stats.totalMembers || 0 }}</span>
                <span class="traffic-name truncate">会员总数</span>
              </div>
              <div class="today-traffic traffic-box">
                <div class="traffic-img">
                  <img src="./images/today.png" alt="" />
                </div>
                <span class="item-value">{{ stats.todayOrders || 0 }}</span>
                <span class="traffic-name truncate">今日订单</span>
              </div>
              <div class="yesterday-traffic traffic-box">
                <div class="traffic-img">
                  <img src="./images/book_sum.png" alt="" />
                </div>
                <span class="item-value">{{ formatAmount(stats.totalAmount) }}</span>
                <span class="traffic-name truncate">总销售额</span>
              </div>
            </div>
          </el-col>
          <el-col class="mb-10" :xs="24" :sm="24" :md="24" :lg="10" :xl="10">
            <div class="item-right">
              <div class="echarts-title">预订类型占比</div>
              <div class="book-echarts">
                <pie ref="pieRef" :data="bookingTypeData" />
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <div class="card bottom-box">
      <div class="bottom-title">预订趋势</div>
      <div class="curve-echarts">
        <curve ref="curveRef" :data="bookingTrendData" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'DataVisualize' })
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import Pie from './components/pie.vue'
import Curve from './components/curve.vue'
import { DashboardAPI } from '@/api/dashboard'

const stats = ref<any>({})
const bookingTypeData = ref<any[]>([])
const bookingTrendData = ref<any[]>([])
const loading = ref(false)

const formatAmount = (amount: any) => {
  const num = Number(amount) || 0
  if (num >= 10000) {
    return '¥' + (num / 10000).toFixed(2) + 'w'
  }
  return '¥' + num.toFixed(2)
}

const loadData = async () => {
  try {
    const [statsRes, typeRes, trendRes] = await Promise.all([
      DashboardAPI.getStats(),
      DashboardAPI.getBookingByType(),
      DashboardAPI.getBookingTrend()
    ])
    stats.value = statsRes
    bookingTypeData.value = typeRes
    bookingTrendData.value = trendRes
    console.log('DEBUG: Dashboard data loaded', stats.value, bookingTypeData.value, bookingTrendData.value)
  } catch (e) {
    console.error('Load dashboard data failed', e)
  }
}

const handleRefresh = async () => {
  loading.value = true
  try {
    await loadData()
    ElMessage.success('数据刷新成功！')
  } catch (e) {
    ElMessage.error('数据刷新失败，请稍后重试')
    console.error('Refresh data failed', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
@use './index';
</style>
