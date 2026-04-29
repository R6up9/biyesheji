<template>
  <div class="client-activity">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动报名</span>
          <el-button type="primary" :icon="Refresh" @click="refreshAllData">
            刷新数据
          </el-button>
        </div>
      </template>

      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="activity in activityList" :key="activity.id">
          <el-card shadow="hover" class="activity-card">
            <div class="activity-image-container">
              <img v-if="activity.image" :src="activity.image" class="activity-image" />
              <div v-else class="activity-placeholder">
                <el-icon :size="60" color="#909399"><Picture /></el-icon>
                <div class="placeholder-text">暂无图片</div>
              </div>
            </div>
            <h3>{{ activity.title }}</h3>
            <p><el-icon><Location /></el-icon> {{ activity.location }}</p>
            <p><el-icon><Calendar /></el-icon> {{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</p>
            <p><el-icon><Tickets /></el-icon> 人数：{{ activity.currentParticipants || 0 }} / {{ activity.maxParticipants }}</p>
            <p>状态：<el-tag :type="statusTagType(activity.status)">
              {{ statusLabel(activity.status) }}
            </el-tag></p>
            <p v-if="activity.content" class="description">{{ activity.content }}</p>
            <el-button
              type="primary"
              @click="handleEnroll(activity)"
              :disabled="activity.status !== 1 || activity.currentParticipants >= activity.maxParticipants || getIsEnrolled(activity.id)">
              {{ getIsEnrolled(activity.id) ? '已报名' : '立即报名' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Calendar, Tickets, Refresh, Picture } from '@element-plus/icons-vue'
import { ActivityClientAPI } from '@/api/activity'
import { BookingAPI } from '@/api/booking'

defineOptions({ name: 'ClientActivity' })

const loading = ref(false)
const activityList = ref<any[]>([])
const bookingList = ref<any[]>([])

const getActivityList = async () => {
  loading.value = true
  try {
    const res = await ActivityClientAPI.getAll()
    activityList.value = res || []
  } finally {
    loading.value = false
  }
}

const getBookingList = async () => {
  try {
    const res = await BookingAPI.getMyList()
    bookingList.value = res.records || []
  } catch (e) {
    console.log(e)
  }
}

const getIsEnrolled = (activityId: number): boolean => {
  return bookingList.value.some(
    (booking: any) =>
      booking.targetId === activityId &&
      booking.type === 3 &&
      [0, 1, 2].includes(booking.status)
  )
}

const refreshAllData = async () => {
  ElMessage.info('正在刷新数据...')
  await Promise.all([getActivityList(), getBookingList()])
  ElMessage.success('刷新成功！')
}

const formatTime = (time: string) => {
  if (!time) return ''
  const t = new Date(time)
  return `${t.getMonth() + 1}月${t.getDate()}日 ${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: '下架',
    1: '开放报名',
    2: '已满',
    3: '已结束'
  }
  return map[status] || '未知'
}

const statusTagType = (status: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: ''
  }
  return map[status] || ''
}

const handleEnroll = async (activity: any) => {
  if (getIsEnrolled(activity.id)) {
    ElMessage.warning('您已经报名过该活动了')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要报名「${activity.title}」活动吗？`,
      '确认报名',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await ActivityClientAPI.enroll(activity.id)
    ElMessage.success('报名成功，请等待确认')
    await refreshAllData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '报名失败')
    }
  }
}

onMounted(() => {
  getActivityList()
  getBookingList()
})
</script>

<style scoped lang="scss">
.client-activity {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .activity-card {
    margin-bottom: 20px;

    .activity-image-container {
      width: 100%;
      height: 180px;
      overflow: hidden;
      border-radius: 4px;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 15px;
    }

    .activity-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .activity-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
    }

    .placeholder-text {
      color: #909399;
      font-size: 14px;
    }

    h3 {
      margin: 0 0 15px 0;
      color: #303133;
    }

    p {
      margin: 10px 0;
      display: flex;
      align-items: center;
      gap: 6px;
      color: #606266;
    }

    .description {
      color: #909399;
      font-size: 13px;
    }
  }
}
</style>
