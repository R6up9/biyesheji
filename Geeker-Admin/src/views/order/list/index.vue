<template>
  <div class="order-list card">
    <div class="search-box">
      <el-form :model="queryParams" inline>
        <el-form-item label="订单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待付款" :value="0" />
            <el-option label="已付款" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
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

    <el-table :data="tableData" stripe border style="width: 100%" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="userName" label="用户" width="100" />
      <el-table-column prop="productName" label="商品" width="150" />
      <el-table-column prop="quantity" label="数量" width="80" align="center" />
      <el-table-column prop="price" label="单价" width="100" align="right">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="120" align="right">
        <template #default="{ row }">
          <span class="total-amount">¥{{ row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiver" label="收货人" width="100" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="createTime" label="下单时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="220" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 1"
            type="primary"
            size="small"
            @click="handleShip(row)"
          >
            发货
          </el-button>
          <el-button
            v-if="row.status === 0"
            type="success"
            size="small"
            @click="handleMarkPaid(row)"
          >
            标记已付
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="handleViewDetail(row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

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

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusTagType(currentOrder.status)">
              {{ statusLabel(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ currentOrder.productName }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ currentOrder.quantity }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ currentOrder.price }}</el-descriptions-item>
          <el-descriptions-item label="总金额" :span="2">
            <span class="total-amount">¥{{ currentOrder.totalAmount }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.receiver }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ formatDateTime(currentOrder.createTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { OrderAPI, OrderVO, OrderQuery } from '@/api/order'

defineOptions({ name: 'OrderList' })

const loading = ref(false)
const tableData = ref<OrderVO[]>([])
const total = ref(0)
const detailVisible = ref(false)
const currentOrder = ref<OrderVO | null>(null)

const queryParams = reactive<OrderQuery>({
  pageNum: 1,
  pageSize: 10,
  status: undefined,
  orderNo: ''
})

const statusLabel = (status: number) => {
  const map: Record<number, string> = {
    0: '待付款',
    1: '已付款',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const statusTagType = (status: number) => {
  const map: Record<number, any> = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: '',
    4: 'info'
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

const getList = async () => {
  loading.value = true
  try {
    const res = await OrderAPI.getList(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  getList()
}

const handleReset = () => {
  queryParams.status = undefined
  queryParams.orderNo = ''
  handleSearch()
}

const handleShip = async (row: OrderVO) => {
  try {
    await ElMessageBox.confirm('确定要标记为已发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await OrderAPI.updateStatus(row.id!, 2)
    ElMessage.success('发货成功')
    getList()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleMarkPaid = async (row: OrderVO) => {
  try {
    await ElMessageBox.confirm('确定要标记为已付款吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await OrderAPI.updateStatus(row.id!, 1)
    ElMessage.success('标记成功')
    getList()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

const handleViewDetail = (row: OrderVO) => {
  currentOrder.value = row
  detailVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.order-list {
  padding: 20px;

  .search-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .total-amount {
    color: #f56c6c;
    font-weight: 700;
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}

.order-detail {
  padding: 20px 0;
}
</style>
