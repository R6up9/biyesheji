<template>
  <div class="activity-list card">
    <!-- 活动报名管理部分 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>活动报名管理</span>
          <el-button type="primary" :icon="Refresh" @click="refreshAllData">
            刷新数据
          </el-button>
        </div>
      </template>

      <div class="booking-list" v-loading="bookingLoading">
        <div class="booking-item" v-for="booking in activityBookings" :key="booking.id">
          <div class="booking-info">
            <div class="booking-activity">
              <el-icon><Location /></el-icon>
              <span>活动ID: {{ booking.targetId }}</span>
            </div>
            <div class="booking-user">
              <el-avatar :size="32" :src="booking.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span>用户: {{ booking.userName || '未知用户' }}</span>
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
            </el-button>
            <el-button
              v-if="booking.status === 1"
              type="primary"
              size="small"
              @click="handleCompleteBooking(booking.id)"
            >
              完成
            </el-button>
            <el-button
              v-if="booking.status === 0 || booking.status === 1"
              type="danger"
              size="small"
              @click="handleCancelBooking(booking.id)"
            >
              取消
            </el-button>
          </div>
        </div>
        <el-empty v-if="activityBookings.length === 0 && !bookingLoading" description="暂无活动报名" />
        
        <!-- 活动报名列表分页 -->
        <div class="booking-pagination" v-if="bookingTotal > 0">
          <el-pagination
            v-model:current-page="bookingQueryParams.pageNum"
            v-model:page-size="bookingQueryParams.pageSize"
            :total="bookingTotal"
            :page-sizes="[4, 8, 12, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getBookings"
            @current-change="getBookings"
          />
        </div>
      </div>
    </el-card>

    <!-- 活动管理部分 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <div class="right-btn">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon> 新增活动
            </el-button>
          </div>
        </div>
      </template>

      <div class="search-box">
        <el-form :model="queryParams" inline>
          <el-form-item label="活动标题">
            <el-input v-model="queryParams.title" placeholder="请输入活动标题" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
              <el-option label="已取消" :value="0" />
              <el-option label="报名中" :value="1" />
              <el-option label="进行中" :value="2" />
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

      <!-- 活动卡片展示 -->
      <el-row :gutter="20" v-loading="loading" class="activity-grid">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="activity in tableData" :key="activity.id" class="activity-col">
          <el-card shadow="hover" class="activity-card">
            <div class="activity-image-container">
              <img v-if="activity.image" :src="activity.image" class="activity-image" @error="handleActivityImageLoadError" />
              <div v-else class="activity-placeholder">
                <el-icon :size="60" color="#909399"><Calendar /></el-icon>
                <div class="placeholder-text">暂无图片</div>
              </div>
            </div>
            <div class="activity-content">
              <div class="activity-header">
                <h3 class="activity-name">{{ activity.title }}</h3>
                <el-tag :type="statusTagType(activity.status)">{{ statusLabel(activity.status) }}</el-tag>
              </div>
              <div class="activity-info">
                <div class="info-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ activity.location }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</span>
                </div>
              </div>
              <p class="activity-desc" v-if="activity.content">{{ activity.content }}</p>
              <div class="activity-footer">
                <div class="activity-participants">
                  <el-icon><User /></el-icon>
                  <span>{{ activity.currentParticipants || 0 }} / {{ activity.maxParticipants }}</span>
                </div>
              </div>
              <div class="activity-actions">
                <el-button type="primary" size="small" @click="handleEdit(activity)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(activity)">删除</el-button>
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
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入活动标题" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="formData.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="formData.startTime" type="datetime" placeholder="请选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="formData.endTime" type="datetime" placeholder="请选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number v-model="formData.maxParticipants" :min="1" placeholder="请输入最大人数" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动图片" prop="image">
          <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
            <el-upload
              class="activity-image-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              :headers="uploadHeaders"
            >
              <img v-if="formData.image" :src="formData.image" class="activity-preview-image" />
              <div v-else class="image-upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击上传</div>
              </div>
            </el-upload>
            <el-input
              v-model="formData.image"
              placeholder="或输入图片URL"
              style="flex: 1"
            />
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">已取消</el-radio>
            <el-radio :value="1">报名中</el-radio>
            <el-radio :value="2">进行中</el-radio>
            <el-radio :value="3">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="活动内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="4" placeholder="请输入活动内容描述" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules, UploadProps } from 'element-plus'
import { Search, Refresh, Plus, Calendar, Location, Clock, User } from '@element-plus/icons-vue'
import { ActivityAPI, ActivityVO, ActivityQuery } from '@/api/activity'
import { BookingAPI } from '@/api/booking'
import { useUserStore } from '@/stores/modules/user'

defineOptions({ name: 'ActivityList' })

const userStore = useUserStore()
const uploadHeaders = computed(() => {
  return {
    'x-access-token': userStore.getUserToken() || ''
  }
})
const uploadAction = computed(() => {
  return import.meta.env.VITE_API_URL + '/api/uploads/activity'
})

const loading = ref(false)
const submitLoading = ref(false)
const bookingLoading = ref(false)
const tableData = ref<ActivityVO[]>([])
const activityBookings = ref<any[]>([])
const total = ref(0)
const bookingTotal = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const queryParams = reactive<ActivityQuery>({
  pageNum: 1,
  pageSize: 8,
  title: '',
  status: undefined
})

const bookingQueryParams = reactive({
  pageNum: 1,
  pageSize: 4
})

const formData = reactive<ActivityVO>({
  title: '',
  content: '',
  image: '',
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: 50,
  status: 1
})

const formRules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  maxParticipants: [{ required: true, message: '请输入最大人数', trigger: 'blur' }]
}

const statusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: '已取消',
    1: '报名中',
    2: '进行中',
    3: '已结束'
  }
  return map[status] || '未知'
}

const statusTagType = (status: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'primary',
    3: '',
    4: 'danger'
  }
  return map[status] || ''
}

const formatTime = (time: string) => {
  if (!time) return ''
  const t = new Date(time)
  return `${t.getMonth() + 1}月${t.getDate()}日 ${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`
}

const formatDateTime = (time: string) => {
  if (!time) return ''
  const t = new Date(time)
  const year = t.getFullYear()
  const month = String(t.getMonth() + 1).padStart(2, '0')
  const day = String(t.getDate()).padStart(2, '0')
  const hour = String(t.getHours()).padStart(2, '0')
  const minute = String(t.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

const handleActivityImageLoadError = (e: any) => {
  console.log('Activity image load error:', e)
  console.log('Image src:', e.target.src)
  e.target.style.display = 'none'
}

const handleUploadError = (error: any, file: any) => {
  console.log('Activity upload error:', error)
  console.log('File:', file)
  ElMessage.error('图片上传失败，请重试')
}

const beforeImageUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  return true
}

const handleImageSuccess: UploadProps['onSuccess'] = (response: any) => {
  console.log('Activity upload success response:', response)
  if (response && response.code === 200 && response.data && response.data.url) {
    formData.image = response.data.url
    console.log('Set activity formData.image to:', formData.image)
    ElMessage.success('图片上传成功!')
  } else {
    console.log('Unexpected activity response format')
    ElMessage.error(response?.msg || '图片上传失败')
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await ActivityAPI.getList(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const getBookings = async () => {
  bookingLoading.value = true
  try {
    const res = await BookingAPI.getList({ 
      pageNum: bookingQueryParams.pageNum, 
      pageSize: bookingQueryParams.pageSize, 
      type: 3 
    })
    activityBookings.value = res.records || []
    bookingTotal.value = res.total || 0
  } finally {
    bookingLoading.value = false
  }
}

const refreshAllData = async () => {
  ElMessage.info('正在刷新数据...')
  await Promise.all([
    getList(),
    getBookings()
  ])
  ElMessage.success('刷新成功！')
}

const handleConfirmBooking = async (bookingId: number) => {
  try {
    await ElMessageBox.confirm('确定要确认这个活动报名吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: bookingId, status: 1 })
    ElMessage.success('确认成功！')
    await refreshAllData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleCompleteBooking = async (bookingId: number) => {
  try {
    await ElMessageBox.confirm('确定要完成这个活动报名吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: bookingId, status: 2 })
    ElMessage.success('完成成功！')
    await refreshAllData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleCancelBooking = async (bookingId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消这个活动报名吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.update({ id: bookingId, status: 3 })
    ElMessage.success('取消成功！')
    await refreshAllData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
  queryParams.title = ''
  queryParams.status = undefined
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增活动'
  Object.assign(formData, {
    title: '',
    content: '',
    image: '',
    startTime: '',
    endTime: '',
    location: '',
    maxParticipants: 50,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: ActivityVO) => {
  dialogTitle.value = '编辑活动'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row: ActivityVO) => {
  await ElMessageBox.confirm(`确定要删除活动「${row.title}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await ActivityAPI.delete(row.id!)
  ElMessage.success('删除成功')
  await refreshAllData()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (formData.id) {
      await ActivityAPI.update(formData)
    } else {
      await ActivityAPI.add(formData)
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
.activity-list {
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

        .booking-activity,
        .booking-user,
        .booking-time {
          display: flex;
          align-items: center;
          gap: 6px;
          color: #303133;
        }
        
        .booking-user {
          gap: 10px;
          span {
            font-weight: 500;
          }
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

  .activity-grid {
    margin-top: 20px;
  }

  .activity-col {
    margin-bottom: 20px;
  }

  .activity-card {
    transition: transform 0.3s, box-shadow 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
  }

  .activity-image-container {
    width: 100%;
    height: 180px;
    overflow: hidden;
    border-radius: 4px;
    background: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
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

  .activity-content {
    padding: 16px 0 0;
  }

  .activity-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;
    gap: 10px;
  }

  .activity-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin: 0;
    flex: 1;
    line-height: 1.4;
  }

  .activity-info {
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

  .activity-desc {
    font-size: 13px;
    color: #606266;
    margin: 8px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .activity-footer {
    display: flex;
    align-items: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f5f7fa;
  }

  .activity-participants {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #606266;

    .el-icon {
      color: #409eff;
    }
  }

  .activity-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }
}

.activity-image-uploader {
  :deep(.el-upload) {
    width: 120px;
    height: 120px;
  }
}

.activity-preview-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
}

.image-upload-placeholder {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: border-color 0.3s;
  background: #fafafa;

  &:hover {
    border-color: #409eff;
  }
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  font-size: 12px;
  color: #8c939d;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}

.booking-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
