<template>
  <div class="course-list card">
    <!-- 课程报名管理部分 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>课程报名管理</span>
          <el-button type="primary" :icon="Refresh" @click="refreshAllData">
            刷新数据
          </el-button>
        </div>
      </template>

      <div class="booking-list" v-loading="bookingLoading">
        <div class="booking-item" v-for="booking in courseBookings" :key="booking.id">
          <div class="booking-info">
            <div class="booking-course">
              <el-icon><Location /></el-icon>
              <span>课程ID: {{ booking.targetId }}</span>
            </div>
            <div class="booking-user">
              <el-icon><User /></el-icon>
              <span>用户: {{ booking.userName }}</span>
            </div>
            <div class="booking-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(booking.createTime) }}</span>
            </div>
          </div>
          <div class="booking-status">
            <el-tag :type="statusTagType(booking.status)" size="small">
              {{ statusLabel(booking.status) }}
            </el-tag>
          </div>
          <div class="booking-actions">
            <el-button
              v-if="booking.status === 0"
              type="success"
              size="small"
              @click="handleConfirmBooking(booking.id)"
            >
              确认
            </el-button
            >
            <el-button
              v-if="booking.status === 1"
              type="primary"
              size="small"
              @click="handleCompleteBooking(booking.id, '确定要完成这个课程报名吗？')"
            >
              完成
            </el-button
            >
            <el-button
              v-if="booking.status === 0 || booking.status === 1"
              type="danger"
              size="small"
              @click="handleCancelBooking(booking.id, '确定要取消这个课程报名吗？')"
            >
              取消
            </el-button
            >
          </div>
        </div>
        <el-empty v-if="courseBookings.length === 0" description="暂无课程报名" />
      </div>
    </el-card>

    <!-- 课程管理部分 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
          <div class="right-btn">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增课程
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-box">
        <el-form :model="queryParams" inline>
          <el-form-item label="课程名称">
            <el-input v-model="queryParams.name" placeholder="请输入课程名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
              <el-option label="下架" :value="0" />
              <el-option label="开放报名" :value="1" />
              <el-option label="已满" :value="2" />
              <el-option label="已结束" :value="3" />
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

      <!-- 课程卡片展示 -->
      <el-row :gutter="20" v-loading="loading" class="course-grid">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in tableData" :key="course.id" class="course-col">
          <el-card shadow="hover" class="course-card">
            <div class="course-content">
              <div class="course-header">
                <h3 class="course-name">{{ course.name }}</h3>
                <el-tag :type="statusTagType(course.status)">{{ statusLabel(course.status) }}</el-tag>
              </div>
              <div class="course-info">
                <div class="info-item">
                  <el-icon><User /></el-icon>
                  <span>教练ID: {{ course.coachId }}</span>
                </div>
                <div class="info-item" v-if="course.courtId">
                  <el-icon><Location /></el-icon>
                  <span>场地ID: {{ course.courtId }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(course.startTime) }} - {{ formatTime(course.endTime) }}</span>
                </div>
              </div>
              <p class="course-desc" v-if="course.description">{{ course.description }}</p>
              <div class="course-footer">
                <div class="course-price">
                  <span class="currency">¥</span>
                  <span class="price-num">{{ course.price }}</span>
                </div>
                <div class="course-count">
                  <el-icon><User /></el-icon>
                  <span>{{ course.currentCount || 0 }} / {{ course.maxCount }}</span>
                </div>
              </div>
              <div class="course-actions">
                <el-button type="primary" size="small" @click="handleEdit(course)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(course)">删除</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[8, 16, 32, 64]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="教练ID" prop="coachId">
          <el-input-number v-model="formData.coachId" :min="1" placeholder="请输入教练ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="场地ID" prop="courtId">
          <el-input-number v-model="formData.courtId" :min="1" placeholder="请输入场地ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="formData.startTime" type="datetime" placeholder="请选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="formData.endTime" type="datetime" placeholder="请选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxCount">
          <el-input-number v-model="formData.maxCount" :min="1" placeholder="请输入最大人数" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课程价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :step="1" :precision="2" placeholder="请输入课程价格" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">下架</el-radio>
            <el-radio :value="1">开放报名</el-radio>
            <el-radio :value="2">已满</el-radio>
            <el-radio :value="3">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
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
import { Search, Refresh, Plus, Location, Clock, User } from '@element-plus/icons-vue'
import { CourseAPI, CourseVO, CourseQuery } from '@/api/course'
import { BookingAPI } from '@/api/booking'
import { formatTime, formatDateTime } from '@/utils/date'
import { useBookingActions } from '@/hooks/useBookingActions'

defineOptions({ name: 'CourseList' })

const loading = ref(false)
const submitLoading = ref(false)
const bookingLoading = ref(false)
const tableData = ref<CourseVO[]>([])
const courseBookings = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const queryParams = reactive<CourseQuery>({
  pageNum: 1, pageSize: 8,
  name: '',
  status: undefined
})

const formData = reactive<CourseVO>({
  name: '',
  coachId: 1,
  courtId: undefined,
  startTime: '',
  endTime: '',
  maxCount: 10,
  price: 0,
  description: '',
  status: 1
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请输入教练ID', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  maxCount: [{ required: true, message: '请输入最大人数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入课程价格', trigger: 'blur' }]
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
    3: '',
    4: 'danger'
  }
  return map[status] || ''
}

const getList = async () => {
  loading.value = true
  try {
    const res = await CourseAPI.getList(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const getBookings = async () => {
  bookingLoading.value = true
  try {
    const res = await BookingAPI.getList({ pageNum: 1, pageSize: 100, type: 2 })
    courseBookings.value = res.records || []
  } finally {
    bookingLoading.value = false
  }
}

const refreshAllData = async () => {
  ElMessage.info('正在刷新数据...')
  await Promise.all([getList(), getBookings()])
  ElMessage.success('刷新成功！')
}

const { handleConfirmBooking, handleCompleteBooking, handleCancelBooking } = useBookingActions(refreshAllData)

const handleSearch = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
  queryParams.name = ''
  queryParams.status = undefined
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增课程'
  Object.assign(formData, {
    name: '',
    coachId: 1,
    courtId: undefined,
    startTime: '',
    endTime: '',
    maxCount: 10,
    price: 0,
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: CourseVO) => {
  dialogTitle.value = '编辑课程'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row: CourseVO) => {
  await ElMessageBox.confirm(`确定要删除课程「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await CourseAPI.delete(row.id!)
  ElMessage.success('删除成功')
  await refreshAllData()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.id) {
      await CourseAPI.update(formData)
    } else {
      await CourseAPI.add(formData)
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
  getBookings()
})
</script>

<style scoped lang="scss">
.course-list {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .booking-list {
    .booking-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 15px;
      background-color: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 10px;

      .booking-info {
        display: flex;
        gap: 20px;

        .booking-course,
        .booking-user,
        .booking-time {
          display: flex;
          align-items: center;
          gap: 6px;
          color: #303133;
        }
      }

      .booking-status {
        flex-shrink: 0;
      }

      .booking-actions {
        display: flex;
        gap: 8px;
      }
    }
  }

  .search-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .course-grid {
    margin-top: 20px;
  }

  .course-col {
    margin-bottom: 20px;
  }

  .course-card {
    transition: transform 0.3s, box-shadow 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
  }

  .course-content {
    padding: 0;
  }

  .course-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    gap: 10px;
  }

  .course-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .course-info {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 8px;
  }

  .info-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #606266;

    .el-icon {
      color: #909399;
      font-size: 14px;
    }
  }

  .course-desc {
    font-size: 13px;
    color: #606266;
    margin: 8px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .course-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f5f7fa;
  }

  .course-price {
    display: flex;
    align-items: baseline;
  }

  .currency {
    font-size: 14px;
    color: #f56c6c;
    font-weight: 600;
  }

  .price-num {
    font-size: 20px;
    color: #f56c6c;
    font-weight: 700;
  }

  .course-count {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #606266;

    .el-icon {
      color: #409eff;
    }
  }

  .course-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}
</style>
