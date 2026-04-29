<template>
  <div class="client-course">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程报名</span>
          <el-button type="primary" :icon="Refresh" @click="refreshAllData">
            刷新数据
          </el-button>
        </div>
      </template>

      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="course in courseList" :key="course.id">
          <el-card shadow="hover" class="course-card">
            <h3>{{ course.name }}</h3>
            <p><el-icon><Calendar /></el-icon> {{ formatTime(course.startTime) }} - {{ formatTime(course.endTime) }}</p>
            <p><el-icon><Tickets /></el-icon> 人数：{{ course.currentCount || 0 }} / {{ course.maxCount }}</p>
            <p><el-icon><Money /></el-icon> 价格：¥{{ course.price }}</p>
            <p>状态：<el-tag :type="statusTagType(course.status)">
              {{ statusLabel(course.status) }}
            </el-tag></p>
            <p v-if="course.description" class="description">{{ course.description }}</p>
            <el-button
              type="primary"
              @click="handleEnroll(course)"
              :disabled="course.status !== 1 || course.currentCount >= course.maxCount || getIsEnrolled(course.id)">
              {{ getIsEnrolled(course.id) ? '已报名' : '立即报名' }}
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
import { Calendar, Tickets, Money, Refresh } from '@element-plus/icons-vue'
import { CourseClientAPI } from '@/api/course'
import { BookingAPI } from '@/api/booking'

defineOptions({ name: 'ClientCourse' })

const loading = ref(false)
const courseList = ref<any[]>([])
const bookingList = ref<any[]>([])

const getCourseList = async () => {
  loading.value = true
  try {
    const res = await CourseClientAPI.getAll()
    courseList.value = res || []
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

const getIsEnrolled = (courseId: number): boolean => {
  return bookingList.value.some(
    (booking: any) =>
      booking.targetId === courseId &&
      booking.type === 2 &&
      [0, 1, 2].includes(booking.status)
  )
}

const refreshAllData = async () => {
  ElMessage.info('正在刷新数据...')
  await Promise.all([getCourseList(), getBookingList()])
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

const handleEnroll = async (course: any) => {
  if (getIsEnrolled(course.id)) {
    ElMessage.warning('您已经报名过该课程了')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要报名「${course.name}」课程吗？`,
      '确认报名',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await CourseClientAPI.enroll(course.id)
    ElMessage.success('报名成功，请等待确认')
    await refreshAllData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '报名失败')
    }
  }
}

onMounted(() => {
  getCourseList()
  getBookingList()
})
</script>

<style scoped lang="scss">
.client-course {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .course-card {
    margin-bottom: 20px;

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
