<template>
  <div class="client-profile">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="profile-info">
            <div class="avatar-upload-wrapper">
              <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeAvatarUpload"
                :headers="uploadHeaders"
              >
                <img v-if="memberInfo?.avatar" :src="memberInfo?.avatar" class="avatar-image" />
                <div v-else class="avatar-placeholder">
                  <el-icon :size="80" color="#909399"><User /></el-icon>
                </div>
                <div class="upload-tip">点击更换头像</div>
              </el-upload>
            </div>
            <h3>{{ memberInfo?.realName || '未设置' }}</h3>
            <p>账号：{{ userInfo?.username }}</p>
            <p>电话：{{ memberInfo?.phone || '未设置' }}</p>
            <p>年龄：{{ memberInfo?.age || '未设置' }}</p>
            <p>性别：{{ memberInfo?.gender === 1 ? '男' : memberInfo?.gender === 2 ? '女' : '未设置' }}</p>
            <p>会员等级：{{ memberInfo?.level || 1 }}</p>
            <p>积分：{{ memberInfo?.points || 0 }}</p>
            <p>余额：¥{{ memberInfo?.balance || 0 }}</p>
            <p style="color: #909399; font-size: 12px; margin-top: 15px;">
              * 如需修改个人信息，请联系管理员
            </p>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-tabs v-model="activeTab" type="border-card">
                <el-tab-pane label="我的预约" name="booking"></el-tab-pane>
                <el-tab-pane label="我的订单" name="order"></el-tab-pane>
              </el-tabs>
            </div>
          </template>
          <template v-if="activeTab === 'booking'">
            <div style="display: flex; justify-content: flex-end; margin-bottom: 10px;">
              <el-button type="primary" :icon="Refresh" @click="getBookingList">刷新</el-button>
            </div>
            <el-table :data="bookingList" v-loading="bookingLoading" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="type" label="预约类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.type === 1 ? 'primary' : row.type === 2 ? 'success' : 'warning'">
                    {{ row.type === 1 ? '场地预约' : row.type === 2 ? '课程报名' : '活动报名' }}
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
                    @click="handleCancelBooking(row)"
                    :disabled="row.status === 2 || row.status === 3"
                  >
                    取消预约
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
          <template v-else>
            <div style="display: flex; justify-content: flex-end; margin-bottom: 10px;">
              <el-button type="primary" :icon="Refresh" @click="getOrderList">刷新</el-button>
            </div>
            <el-table :data="orderList" v-loading="orderLoading" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="orderNo" label="订单号" width="180" />
              <el-table-column prop="productName" label="商品" width="150" />
              <el-table-column prop="quantity" label="数量" width="80" align="center" />
              <el-table-column prop="price" label="单价" width="100" align="right">
                <template #default="{ row }">
                  ¥{{ row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
                <template #default="{ row }">
                  <span style="color: #f56c6c; font-weight: 700;">¥{{ row.totalAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="orderStatusTagType(row.status)">
                    {{ orderStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="下单时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" align="center">
                <template #default="{ row }">
                  <el-button
                    v-if="row.status === 0"
                    type="info"
                    size="small"
                    @click="handleCancelOrder(row)"
                  >
                    取消订单
                  </el-button>
                  <el-button
                    v-if="row.status === 2"
                    type="primary"
                    size="small"
                    @click="handleCompleteOrder(row)"
                  >
                    确认收货
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type UploadProps } from 'element-plus'
import { User, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/modules/user'
import { AuthAPI } from '@/api/auth'
import { BookingAPI } from '@/api/booking'
import { OrderAPI } from '@/api/order'

defineOptions({ name: 'ClientProfile' })

const userStore = useUserStore()
const userInfo = ref<any>(null)
const memberInfo = ref<any>(null)
const bookingList = ref<any[]>([])
const bookingLoading = ref(false)
const orderList = ref<any[]>([])
const orderLoading = ref(false)
const activeTab = ref('booking')

const uploadHeaders = computed(() => {
  return {
    'x-access-token': userStore.getUserToken() || ''
  }
})
const uploadAction = computed(() => {
  return import.meta.env.VITE_API_URL + '/api/uploads/avatar'
})

const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
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

const handleAvatarSuccess: UploadProps['onSuccess'] = async (response: any) => {
  if (response && response.code === 200 && response.data && response.data.url) {
    try {
      await AuthAPI.updateAvatar(response.data.url)
      ElMessage.success('头像更新成功!')
      await getInfo()
    } catch {
      ElMessage.error('头像更新失败')
    }
  } else {
    ElMessage.error(response?.msg || '图片上传失败')
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const getInfo = async () => {
  try {
    console.log('DEBUG: Starting getInfo()')
    const [userRes, memberRes] = await Promise.all([
      AuthAPI.getCurrentUser(),
      AuthAPI.getMemberInfo()
    ])
    console.log('DEBUG: User response:', userRes)
    console.log('DEBUG: Member response:', memberRes)
    userInfo.value = userRes
    memberInfo.value = memberRes
    console.log('DEBUG: userInfo set:', userInfo.value)
    console.log('DEBUG: memberInfo set:', memberInfo.value)
  } catch (e) {
    console.error('获取信息失败', e)
  }
}

const getBookingList = async () => {
  bookingLoading.value = true
  try {
    const res = await BookingAPI.getMyList()
    bookingList.value = res.records || []
  } catch (e) {
    console.error('获取预约列表失败', e)
  } finally {
    bookingLoading.value = false
  }
}

const handleCancelBooking = async (row: any) => {
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

const formatTime = (time: string) => {
  if (!time) return ''
  const t = new Date(time)
  const hour = String(t.getHours()).padStart(2, '0')
  const minute = String(t.getMinutes()).padStart(2, '0')
  return `${hour}:${minute}`
}

const getOrderList = async () => {
  orderLoading.value = true
  try {
    const res = await OrderAPI.getMyOrders({
      pageNum: 1,
      pageSize: 100
    })
    orderList.value = res.records || []
  } catch (e) {
    console.error('获取订单列表失败', e)
  } finally {
    orderLoading.value = false
  }
}

const orderStatusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: '待付款',
    1: '已付款',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const orderStatusTagType = (status: number) => {
  const map: Record<number, any> = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: '',
    4: 'info'
  }
  return map[status] || ''
}

const handleCancelOrder = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await OrderAPI.cancelOrder(row.id)
    ElMessage.success('取消成功！')
    getOrderList()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleCompleteOrder = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认收货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await OrderAPI.completeOrder(row.id)
    ElMessage.success('操作成功！')
    getOrderList()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(() => {
  getInfo()
  getBookingList()
  getOrderList()
})
</script>

<style scoped lang="scss">
.client-profile {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .profile-info {
    text-align: center;

    .avatar-upload-wrapper {
      position: relative;
      display: inline-block;
      margin: 0 auto 20px;

      .avatar-uploader {
        position: relative;

        .avatar-image {
          width: 120px;
          height: 120px;
          border-radius: 50%;
          object-fit: cover;
          border: 3px solid #409eff;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 0 15px rgba(64, 158, 255, 0.4);
          }
        }

        .avatar-placeholder {
          width: 120px;
          height: 120px;
          border-radius: 50%;
          background: #f5f7fa;
          display: flex;
          align-items: center;
          justify-content: center;
          border: 2px dashed #d9d9d9;
          cursor: pointer;
          transition: all 0.3s;

          &:hover {
            border-color: #409eff;
            background: #ecf5ff;
          }
        }

        .upload-tip {
          position: absolute;
          bottom: -25px;
          left: 50%;
          transform: translateX(-50%);
          font-size: 12px;
          color: #909399;
          white-space: nowrap;
        }
      }
    }

    h3 {
      margin: 25px 0 15px 0;
      color: #303133;
    }

    p {
      margin: 10px 0;
      color: #606266;
    }
  }
}
</style>
