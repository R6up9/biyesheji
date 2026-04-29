<template>
  <div class="client-coach">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教练信息</span>
          <el-button type="primary" :icon="Refresh" @click="refreshAllData">
            刷新数据
          </el-button>
        </div>
      </template>

      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="coach in coachList" :key="coach.id">
          <el-card shadow="hover" class="coach-card">
            <div class="coach-avatar">
              <img v-if="coach.avatar" :src="coach.avatar" class="avatar-img" />
              <el-icon v-else :size="60" color="#909399"><User /></el-icon>
            </div>
            <h3>{{ coach.realName }}</h3>
            <p><el-icon><Trophy /></el-icon> 专业资质：{{ coach.skills || '暂无' }}</p>
            <p><el-icon><Document /></el-icon> 简介：{{ coach.introduction || '暂无' }}</p>
            <p><el-icon><ChatDotRound /></el-icon> 年龄：{{ coach.age || 0 }}岁</p>
            <p>状态：<el-tag :type="coach.status === 1 ? 'success' : 'info'">
              {{ coach.status === 1 ? '在职' : '离职' }}
            </el-tag></p>
            <p>费用：¥{{ coach.hourlyRate || 0 }}/小时</p>
            <el-button
              type="primary"
              :disabled="coach.status !== 1 || getIsBooked(coach.id)"
              @click="handleBookCoach(coach)"
            >
              {{ getIsBooked(coach.id) ? '已预约' : '预约教练' }}
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
import { User, Trophy, Document, ChatDotRound, Refresh } from '@element-plus/icons-vue'
import { CoachClientAPI } from '@/api/coach'
import { BookingAPI } from '@/api/booking'

defineOptions({ name: 'ClientCoach' })

const loading = ref(false)
const coachList = ref<any[]>([])
const bookingList = ref<any[]>([])

const getCoachList = async () => {
  loading.value = true
  try {
    const res = await CoachClientAPI.getAll()
    coachList.value = res || []
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

const getIsBooked = (coachId: number): boolean => {
  return bookingList.value.some(
    (booking: any) =>
      booking.targetId === coachId &&
      booking.type === 4 &&
      [0, 1].includes(booking.status)
  )
}

const refreshAllData = async () => {
  ElMessage.info('正在刷新数据...')
  await Promise.all([getCoachList(), getBookingList()])
  ElMessage.success('刷新成功！')
}

const handleBookCoach = async (coach: any) => {
  try {
    await ElMessageBox.confirm(
      `确认预约教练「${coach.realName}」吗？费用：¥${coach.hourlyRate || 0}/小时，预约后请等待管理员确认具体授课时间。`,
      '预约教练',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await CoachClientAPI.book(coach.id)
    ElMessage.success(res || '预约成功！')
    await refreshAllData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '预约失败')
    }
  }
}

onMounted(() => {
  getCoachList()
  getBookingList()
})
</script>

<style scoped lang="scss">
.client-coach {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .coach-card {
    margin-bottom: 20px;
    text-align: center;

    .coach-avatar {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      background: #f5f7fa;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      overflow: hidden;

      .avatar-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
    }

    h3 {
      margin: 0 0 15px 0;
      color: #303133;
    }

    p {
      margin: 10px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      color: #606266;
      text-align: left;
    }
  }
}
</style>
