<template>
  <div class="court-list card">
    <!-- 预约可视化部分 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
            <div class="card-header">
              <span>场地预约管理</span>
              <div class="header-actions">
                <el-button type="primary" :icon="Refresh" @click="refreshAllData">
                  刷新数据
                </el-button>
                <el-date-picker
                  v-model="selectedDate"
                  type="date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  @change="loadVisualization"
                  style="width: 200px"
                />
              </div>
            </div>
          </template>
      
      <div class="court-grid" v-loading="visualizationLoading">
        <div class="court-card" v-for="court in courtDataList" :key="court.id">
          <div class="court-header">
            <h3>{{ court.name }}</h3>
            <span class="court-location">{{ court.location }}</span>
          </div>
          
          <div class="time-slot-section" v-if="court.timeSlot">
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
                :class="getSeatClass(index, court.timeSlot.bookings?.length || 0)"
              >
                <template v-if="index <= (court.timeSlot.bookings?.length || 0) && court.timeSlot.bookings?.[index - 1]">
                  <el-tooltip :content="getBookingTooltip(court.timeSlot.bookings[index - 1])" placement="top">
                    <div class="seat-booked">
                      <el-icon><User /></el-icon>
                      <div class="seat-label">{{ getUserName(court.timeSlot.bookings[index - 1]) }}</div>
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
            
            <div class="bookings-list" v-if="court.timeSlot.bookings?.length > 0">
              <div class="bookings-list-title">预约详情：</div>
              <div 
                v-for="booking in court.timeSlot.bookings" 
                :key="booking.bookingId"
                class="booking-item"
              >
                <div class="booking-user">
                  <el-icon><User /></el-icon>
                  <span>{{ booking.userName }}</span>
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
                  </el-button
                  >
                  <el-button 
                    v-if="booking.status === 1" 
                    type="primary" 
                    size="small"
                    @click="handleCompleteBooking(booking.bookingId, '确定要完成这个预约吗？')"
                  >
                    完成
                  </el-button
                  >
                  <el-button 
                    v-if="booking.status === 0 || booking.status === 1" 
                    type="danger" 
                    size="small"
                    @click="handleCancelBooking(booking.bookingId, '确定要取消这个预约吗？')"
                  >
                    取消
                  </el-button
                  >
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 场地管理部分 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>场地管理</span>
          <div class="right-btn">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索区 -->
      <div class="search-box">
        <el-form :model="queryParams" inline>
          <el-form-item label="场地名称">
            <el-input v-model="queryParams.name" placeholder="请输入场地名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
              <el-option label="维护中" :value="0" />
              <el-option label="可用" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格区 -->
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="场地名称" width="150" />
        <el-table-column prop="location" label="位置" width="200" />
        <el-table-column prop="capacity" label="容纳人数" width="100" />
        <el-table-column prop="pricePerHour" label="小时价格" width="120">
          <template #default="{ row }">
            ¥{{ row.pricePerHour }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '可用' : '维护中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="场地名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入场地名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入场地位置" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input-number v-model="formData.capacity" :min="1" placeholder="请输入容纳人数" />
        </el-form-item>
        <el-form-item label="小时价格" prop="pricePerHour">
          <el-input-number v-model="formData.pricePerHour" :min="0" :step="1" :precision="2" placeholder="请输入小时价格" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="0">维护中</el-radio>
            <el-radio :label="1">可用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Search, Refresh, Plus, User } from '@element-plus/icons-vue'
import { CourtAPI, CourtVO, CourtQuery } from '@/api/court'
import { CourtVisualizationAPI } from '@/api/court-visualization'
import { BookingAPI } from '@/api/booking'
import { formatDateTime } from '@/utils/date'
import { useBookingActions } from '@/hooks/useBookingActions'

defineOptions({ name: 'CourtList' })

const loading = ref(false)
const submitLoading = ref(false)
const visualizationLoading = ref(false)
const tableData = ref<CourtVO[]>([])
const courtDataList = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const selectedDate = ref(new Date().toISOString().split('T')[0])

const queryParams = reactive<CourtQuery>({
  pageNum: 1, pageSize: 10,
  name: '', status: undefined
})

const formData = reactive<CourtVO>({
  name: '', location: '', capacity: 4, status: 1,
  pricePerHour: 50, description: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入场地名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入场地位置', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容纳人数', trigger: 'blur' }],
  pricePerHour: [{ required: true, message: '请输入小时价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const loadVisualization = async () => {
  visualizationLoading.value = true
  try {
    const res = await CourtVisualizationAPI.getData(selectedDate.value)
    courtDataList.value = res.courts || []
  } catch (e: any) {
    ElMessage.error(e.message || '获取数据失败')
  } finally {
    visualizationLoading.value = false
  }
}

const refreshAllData = async () => {
  ElMessage.info('正在刷新数据...')
  await Promise.all([loadVisualization(), getList()])
  ElMessage.success('刷新成功！')
}

const { handleConfirmBooking, handleCompleteBooking, handleCancelBooking } = useBookingActions(refreshAllData)

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

// 获取列表
const getList = async () => {
  loading.value = true
  try {
    const res = await CourtAPI.getList(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const handleReset = () => {
  queryParams.name = ''
  queryParams.status = undefined
  handleSearch()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增场地'
  Object.assign(formData, {
    name: '',
    location: '',
    capacity: 4,
    status: 1,
    pricePerHour: 50,
    description: ''
  })
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: CourtVO) => {
  dialogTitle.value = '编辑场地'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: CourtVO) => {
  await ElMessageBox.confirm('确定要删除该场地吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await CourtAPI.delete(row.id!)
  ElMessage.success('删除成功')
  await refreshAllData()
}

// 提交
const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.id) {
      await CourtAPI.update(formData)
    } else {
      await CourtAPI.add(formData)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    await refreshAllData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
  loadVisualization()
})
</script>

<style scoped lang="scss">
.court-list {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-actions {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }
  
  .search-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
  
  .court-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    margin-top: 20px;
  }
  
  .court-card {
    border: 1px solid #e6e6e6;
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
        border-bottom: 1px solid #e6e6e6;
        
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
              background-color: #f56c6c;
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
              background-color: #f0f2f5;
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
          background-color: #f5f7fa;
          border-radius: 6px;
          margin-bottom: 8px;
          
          .booking-user {
            display: flex;
            align-items: center;
            gap: 6px;
            color: #303133;
          }
          
          .booking-actions {
            display: flex;
            gap: 6px;
          }
        }
      }
    }
  }
}
</style>
