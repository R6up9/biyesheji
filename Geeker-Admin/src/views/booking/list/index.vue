<template>
  <div class="booking-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
          <div class="header-right">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              @change="loadData"
              style="width: 200px"
            />
          </div>
        </div>
      </template>
      
      <div class="court-grid" v-loading="loading">
        <div class="court-card" v-for="court in courtDataList" :key="court.id">
          <div class="court-header">
            <h3>{{ court.name }}</h3>
            <span class="court-location">{{ court.location }}</span>
          </div>
          
          <div class="time-slot-section">
            <div class="time-slot-header">
              <span class="time-range">{{ court.timeSlot.timeRange }}</span>
              <div class="booking-summary">
                <span class="booked">{{ court.timeSlot.bookedCount }} 人已预约</span>
                <span class="remaining">/{{ court.timeSlot.capacity }} 剩余 {{ court.timeSlot.remainingCount }}</span>
                <el-tag :type="court.timeSlot.isFull ? 'danger' : 'success'" size="small">
                  {{ court.timeSlot.isFull ? '已满' : '可预约' }}
                </el-tag>
              </div>
            </div>
            
            <div class="seats-container">
              <div 
                v-for="index in court.capacity" 
                :key="index"
                class="seat"
                :class="getSeatClass(index, court.timeSlot.bookings.length)"
              >
                <template v-if="index <= court.timeSlot.bookings.length">
                  <el-tooltip :content="getBookingTooltip(court.timeSlot.bookings[index - 1])" placement="top">
                    <div class="seat-booked">
                      <el-avatar :size="30" :src="court.timeSlot.bookings[index - 1].avatar">
                        <el-icon><User /></el-icon>
                      </el-avatar>
                    </div>
                  </el-tooltip>
                </template>
                <template v-else>
                  <div class="seat-available">
                    <el-icon><User /></el-icon>
                    <div class="seat-label">空</div>
                  </div>
                </template>
              </div>
            </div>
            
            <div class="bookings-list" v-if="court.timeSlot.bookings.length > 0">
              <div class="bookings-list-title">预约详情：</div>
              <div 
                v-for="booking in court.timeSlot.bookings" 
                :key="booking.bookingId"
                class="booking-item"
              >
                <div class="booking-user">
                  <el-avatar :size="32" :src="booking.avatar">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                  <span>{{ booking.userName || '未知用户' }}</span>
                </div>
                <el-tag :type="booking.status === 0 ? 'warning' : 'success'" size="small">
                  {{ booking.statusLabel }}
                </el-tag>
                <div class="booking-actions">
                  <el-button 
                    v-if="booking.status === 0" 
                    type="success" 
                    size="small"
                    @click="handleConfirmBooking(booking.bookingId)"
                  >
                    确认
                  </el-button>
                  <el-button 
                    v-if="booking.status === 1" 
                    type="primary" 
                    size="small"
                    @click="handleCompleteBooking(booking.bookingId)"
                  >
                    完成
                  </el-button>
                  <el-button 
                    v-if="booking.status === 0 || booking.status === 1" 
                    type="danger" 
                    size="small"
                    @click="handleCancelBooking(booking.bookingId)"
                  >
                    取消
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <el-divider style="margin: 30px 0" />
      
      <div class="full-list-section">
        <div class="section-header">
          <h4>全部预约记录</h4>
        </div>
        
        <!-- 搜索栏 -->
        <el-form :inline="true" style="margin-top: 20px">
          <el-form-item label="预约类型">
            <el-select v-model="filterParams.type" placeholder="请选择类型" clearable style="width: 200px">
              <el-option label="全部" :value="null" />
              <el-option label="场地预约" :value="1" />
              <el-option label="课程报名" :value="2" />
              <el-option label="活动报名" :value="3" />
              <el-option label="教练预约" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="filterParams.status" placeholder="请选择状态" clearable style="width: 200px">
              <el-option label="全部" :value="null" />
              <el-option label="待确认" :value="0" />
              <el-option label="已确认" :value="1" />
              <el-option label="已完成" :value="2" />
              <el-option label="已取消" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="getList">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 表格 -->
        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="用户信息" width="200">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="40" :src="row.avatar">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <span class="user-name">{{ row.userName || '未知用户' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="预约类型" width="120">
            <template #default="{ row }">
              <el-tag :type="row.type === 1 ? 'primary' : row.type === 2 ? 'success' : row.type === 3 ? 'warning' : 'info'">
              {{ row.type === 1 ? '场地预约' : row.type === 2 ? '课程报名' : row.type === 3 ? '活动报名' : '教练预约' }}
            </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetId" label="目标ID" width="100" />
          <el-table-column label="预约时间" width="240">
            <template #default="{ row }">
              {{ formatDateTime(row.startTime) }} - {{ formatTime(row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="success" 
                size="small" 
                @click="handleConfirm(row)" 
                :disabled="row.status !== 0"
              >
                确认预约
              </el-button>
              <el-button 
                type="primary" 
                size="small" 
                @click="handleComplete(row)" 
                :disabled="row.status !== 1"
              >
                完成预约
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleCancel(row)" 
                :disabled="row.status === 2 || row.status === 3"
              >
                取消预约
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <el-pagination
          v-model:current-page="paginationParams.pageNum"
          v-model:page-size="paginationParams.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getList"
          @current-change="getList"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { CourtVisualizationAPI } from '@/api/court-visualization'
import { BookingAPI } from '@/api/booking'

defineOptions({ name: 'BookingList' })

const loading = ref(false)
const courtDataList = ref<any[]>([])
const tableData = ref<any[]>([])
const total = ref(0)
const selectedDate = ref(new Date().toISOString().split('T')[0])

const filterParams = reactive({
  type: null as number | null,
  status: null as number | null
})

const paginationParams = reactive({
  pageNum: 1,
  pageSize: 5
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await CourtVisualizationAPI.getData(selectedDate.value)
    courtDataList.value = res.courts || []
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

const getList = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: paginationParams.pageNum,
      pageSize: paginationParams.pageSize
    }
    if (filterParams.type !== null) params.type = filterParams.type
    if (filterParams.status !== null) params.status = filterParams.status
    
    const res = await BookingAPI.getList(params)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  paginationParams.pageNum = 1
  paginationParams.pageSize = 5
  filterParams.type = null
  filterParams.status = null
  getList()
}

const getSeatClass = (index: number, bookedCount: number) => {
  return index <= bookedCount ? 'seat-booked-wrapper' : 'seat-available-wrapper'
}

const getUserName = (booking: any) => {
  return booking ? booking.userName : ''
}

const getBookingTooltip = (booking: any) => {
  if (!booking) return ''
  return `用户：${booking.userName}\n状态：${booking.statusLabel}\n预约时间：${formatDateTime(booking.createTime)}`
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: '待确认',
    1: '已确认',
    2: '已完成',
    3: '已取消'
  }
  return map[status] || '未知'
}

const statusTagType = (status: number) => {
  const map: Record<number, any> = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'danger'
  }
  return map[status] || ''
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

const handleConfirm = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要确认这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: row.id, status: 1 })
    ElMessage.success('确认成功！')
    getList()
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleComplete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要完成这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: row.id, status: 2 })
    ElMessage.success('完成成功！')
    getList()
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: row.id, status: 3 })
    ElMessage.success('取消成功！')
    getList()
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleConfirmBooking = async (bookingId: number) => {
  try {
    await ElMessageBox.confirm('确定要确认这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: bookingId, status: 1 })
    ElMessage.success('确认成功！')
    getList()
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleCompleteBooking = async (bookingId: number) => {
  try {
    await ElMessageBox.confirm('确定要完成这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: bookingId, status: 2 })
    ElMessage.success('完成成功！')
    getList()
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleCancelBooking = async (bookingId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: bookingId, status: 3 })
    ElMessage.success('取消成功！')
    getList()
    loadData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(() => {
  loadData()
  getList()
})
</script>

<style scoped lang="scss">
.booking-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .court-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    margin-top: 20px;
  }
  
  .court-card {
    border: 1px solid #ebeef5;
    border-radius: 8px;
    padding: 20px;
    
    .court-header {
      h3 {
        margin: 0 0 10px 0;
        color: #303133;
      }
      .court-location {
        color: #909399;
        font-size: 14px;
      }
    }
    
    .time-slot-section {
      margin-top: 20px;
      
      .time-slot-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 1px solid #ebeef5;
        
        .time-range {
          font-size: 16px;
          font-weight: bold;
          color: #303133;
        }
        
        .booking-summary {
          .booked {
            color: #409eff;
            font-weight: bold;
          }
          .remaining {
            color: #909399;
            margin-left: 5px;
          }
        }
      }
      
      .seats-container {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(60px, 1fr));
        gap: 10px;
        margin-bottom: 20px;
        
        .seat {
          aspect-ratio: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 8px;
          
          &.seat-booked-wrapper {
            .seat-booked {
              width: 100%;
              height: 100%;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              background: #f56c6c;
              border-radius: 8px;
              color: white;
              cursor: pointer;
              
              .seat-label {
                font-size: 10px;
                margin-top: 4px;
                max-width: 100%;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
          }
          
          &.seat-available-wrapper {
            .seat-available {
              width: 100%;
              height: 100%;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              background: #f0f2f5;
              border: 2px dashed #c0c4cc;
              border-radius: 8px;
              color: #909399;
              
              .seat-label {
                font-size: 12px;
                margin-top: 4px;
              }
            }
          }
        }
      }
      
      .bookings-list {
        .bookings-list-title {
          font-size: 14px;
          color: #606266;
          margin-bottom: 10px;
        }
        
        .booking-item {
              display: flex;
              align-items: center;
              justify-content: space-between;
              padding: 10px;
              background: #f5f7fa;
              border-radius: 6px;
              margin-bottom: 8px;
              
              .booking-user {
                display: flex;
                align-items: center;
                gap: 10px;
                color: #303133;
                
                span {
                  font-weight: 500;
                }
              }
              
              .booking-actions {
                display: flex;
                gap: 6px;
              }
            }
      }
    }
  }
  
  .full-list-section {
    .section-header {
      margin-bottom: 10px;
      h4 {
        margin: 0;
        color: #303133;
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .user-name {
        color: #303133;
        font-size: 14px;
        font-weight: 500;
      }
    }
  }
}
</style>
