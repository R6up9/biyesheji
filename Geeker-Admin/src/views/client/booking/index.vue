<template>
  <div class="client-booking">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
        </div>
      </template>
      
      <el-table :data="bookingList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="预约类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'primary' : row.type === 2 ? 'success' : row.type === 3 ? 'warning' : 'info'">
              {{ row.type === 1 ? '场地预约' : row.type === 2 ? '课程报名' : row.type === 3 ? '活动报名' : '教练预约' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="220">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
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
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getBookingList"
        @current-change="getBookingList"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BookingAPI } from '@/api/booking'

defineOptions({ name: 'ClientBooking' })

const loading = ref(false)
const bookingList = ref<any[]>([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 8
})

const getBookingList = async () => {
  loading.value = true
  try {
    const res = await BookingAPI.getMyList(queryParams)
    bookingList.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
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

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await BookingAPI.cancel(row.id)
    ElMessage.success('取消成功！')
    getBookingList()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(() => {
  getBookingList()
})
</script>

<style scoped lang="scss">
.client-booking {
}
</style>
