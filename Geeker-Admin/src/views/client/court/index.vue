<template>
  <div class="client-court">
    <!-- 场地预约部分 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>场地预约</span>
          <el-button type="primary" :icon="Refresh" @click="refreshAllData">
            刷新数据
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="8" v-for="court in courtList" :key="court.id">
          <el-card shadow="hover" class="court-card" @click="handleBook(court)">
            <h3>{{ court.name }}</h3>
            <p><el-icon><Location /></el-icon> {{ court.location }}</p>
            <p><el-icon><Timer /></el-icon> 价格：¥{{ court.pricePerHour }}/小时</p>
            <p><el-icon><User /></el-icon> 容量：{{ court.capacity }}人</p>
            <p>
              今日预约：
              <span class="booked-count">{{ getTodayBookedCount(court.id) }}</span> / {{ court.capacity }}
              <span v-if="getTodayBookedCount(court.id) >= court.capacity" class="full-tag">
                <el-tag type="danger" size="small">已满</el-tag>
              </span>
              <span v-if="getUserHasBookedToday(court.id)" class="booked-tag">
                <el-tag type="warning" size="small">我已预约</el-tag>
              </span>
            </p>
            <p>状态：<el-tag :type="court.status === 1 ? 'success' : 'info'">
              {{ court.status === 1 ? '可用' : '维护中' }}
            </el-tag></p>
            <el-button 
              type="primary" 
              @click.stop="handleBook(court)" 
              :disabled="court.status !== 1 || getUserHasBookedToday(court.id)"
            >
              {{ getUserHasBookedToday(court.id) ? '我已预约' : '立即预约' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 预约对话框 -->
    <el-dialog v-model="bookDialogVisible" title="预约场地" width="600px">
      <el-form :model="bookForm" label-width="100px">
        <el-form-item label="选择日期">
          <el-date-picker
            v-model="bookForm.date"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            :disabled-date="disabledDate"
            value-format="YYYY-MM-DD"
            @change="loadTimeSlots"
          />
        </el-form-item>
        <el-form-item label="选择时段">
          <div class="time-slot-grid" v-if="timeSlots.length > 0">
            <div
              v-for="slot in timeSlots"
              :key="slot.startTime"
              class="time-slot-item"
              :class="{
                'is-full': slot.isFull,
                'is-selected': bookForm.timeSlot === slot.timeRange,
                'is-booked': slot.userHasBooked
              }"
              @click="!slot.isFull && !slot.userHasBooked && selectTimeSlot(slot)"
            >
              <div class="time-range">{{ slot.timeRange }}</div>
              <div class="slot-info">
                <span :class="{ 'text-danger': slot.isFull }">
                  {{ slot.bookedCount }}/{{ slot.capacity }}人
                </span>
                <span v-if="slot.isFull" class="full-text">已满</span>
                <span v-else-if="slot.userHasBooked" class="booked-text">我已预约</span>
                <span v-else class="remain-text">剩余{{ slot.remainingCount }}个</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="请先选择日期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBook" :loading="submitLoading" :disabled="!bookForm.timeSlot">
          确认预约
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Location, Timer, User, Refresh } from '@element-plus/icons-vue'
import { CourtAPI } from '@/api/court'
import { AuthAPI } from '@/api/auth'

defineOptions({ name: 'ClientCourt' })

const loading = ref(false)
const submitLoading = ref(false)
const bookDialogVisible = ref(false)
const courtList = ref<any[]>([])
const selectedCourt = ref<any>(null)
const timeSlots = ref<any[]>([])
const todayBookedMap = ref<Map<number, number>>(new Map())
const userBookedTodayMap = ref<Map<number, boolean>>(new Map())

const bookForm = ref({
  date: '',
  timeSlot: '',
  startTime: '',
  endTime: ''
})

// 获取今日预约数
const getTodayBookedCount = (courtId: number) => {
  return todayBookedMap.value.get(courtId) || 0
}

// 获取用户今天是否已预约该场地
const getUserHasBookedToday = (courtId: number) => {
  return userBookedTodayMap.value.get(courtId) || false
}

const getCourtList = async () => {
  loading.value = true
  try {
    const res = await CourtAPI.getAllCourts()
    courtList.value = res || []
    
    // 加载每个场地今日预约情况
    const today = new Date().toISOString().split('T')[0]
    for (const court of courtList.value) {
      try {
        const bookingRes = await CourtAPI.getBookings(court.id, today)
        if (bookingRes && bookingRes.timeSlots && bookingRes.timeSlots.length > 0) {
          const totalBooked = bookingRes.timeSlots[0].bookedCount
          todayBookedMap.value.set(court.id, totalBooked)
          userBookedTodayMap.value.set(court.id, bookingRes.userHasBooked || false)
        } else {
          todayBookedMap.value.set(court.id, 0)
          userBookedTodayMap.value.set(court.id, false)
        }
      } catch (e) {
        console.error('获取预约情况失败', e)
        todayBookedMap.value.set(court.id, 0)
        userBookedTodayMap.value.set(court.id, false)
      }
    }
  } finally {
    loading.value = false
  }
}

const refreshAllData = async () => {
  ElMessage.info('正在刷新数据...')
  try {
    // 刷新用户信息（余额）
    await AuthAPI.getMemberInfo()
  } catch (e) {
    console.error('刷新用户信息失败', e)
  }
  await getCourtList()
  ElMessage.success('刷新成功！')
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7
}

const handleBook = (court: any) => {
  if (court.status !== 1) {
    ElMessage.warning('该场地暂不可用')
    return
  }
  if (getUserHasBookedToday(court.id)) {
    ElMessage.warning('您今天已经预约过这个场地了，每人每天每场只能预约一次')
    return
  }
  selectedCourt.value = court
  bookForm.value = {
    date: '',
    timeSlot: '',
    startTime: '',
    endTime: ''
  }
  timeSlots.value = []
  bookDialogVisible.value = true
}

const loadTimeSlots = async () => {
  if (!bookForm.value.date || !selectedCourt.value) return
  
  try {
    const res = await CourtAPI.getBookings(selectedCourt.value.id, bookForm.value.date)
    timeSlots.value = res.timeSlots || []
  } catch (e: any) {
    ElMessage.error(e.message || '获取时段信息失败')
  }
}

const selectTimeSlot = (slot: any) => {
  bookForm.value.timeSlot = slot.timeRange
  bookForm.value.startTime = slot.startTime
  bookForm.value.endTime = slot.endTime
}

const submitBook = async () => {
  if (!bookForm.value.date || !bookForm.value.timeSlot) {
    ElMessage.warning('请选择日期和时段')
    return
  }
  
  submitLoading.value = true
  try {
    await CourtAPI.book({
      targetId: selectedCourt.value.id,
      type: 1, // 1=场地
      startTime: bookForm.value.startTime,
      endTime: bookForm.value.endTime
    })
    ElMessage.success('预约成功！')
    bookDialogVisible.value = false
    // 刷新数据
    await refreshAllData()
  } catch (e: any) {
    ElMessage.error(e.message || '预约失败')
  } finally {
    submitLoading.value = false
  }
}

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
}

onMounted(() => {
  getCourtList()
})
</script>

<style scoped lang="scss">
.client-court {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .court-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: transform 0.3s;
    
    &:hover {
      transform: translateY(-5px);
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
      
      .booked-count {
        font-weight: bold;
        color: #409eff;
      }
      
      .full-tag,
      .booked-tag {
        margin-left: 8px;
      }
    }
  }
  
  .time-slot-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
    
    .time-slot-item {
      border: 1px solid #dcdfe6;
      border-radius: 8px;
      padding: 12px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover:not(.is-full):not(.is-booked) {
        border-color: #409eff;
        background-color: #f0f7ff;
      }
      
      &.is-full,
      &.is-booked {
        background-color: #f5f7fa;
        cursor: not-allowed;
        opacity: 0.7;
      }
      
      &.is-selected {
        border-color: #409eff;
        background-color: #ecf5ff;
      }
      
      .time-range {
        font-size: 16px;
        font-weight: bold;
        margin-bottom: 8px;
      }
      
      .slot-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 14px;
        
        .text-danger {
          color: #f56c6c;
        }
        
        .full-text {
          color: #f56c6c;
          font-weight: bold;
        }
        
        .booked-text {
          color: #e6a23c;
          font-weight: bold;
        }
        
        .remain-text {
          color: #67c23a;
        }
      }
    }
  }
}
</style>