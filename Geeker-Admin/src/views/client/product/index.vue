<template>
  <div class="product-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">🛍️ 商品查看</h2>
      <p class="page-desc">浏览并购买羽毛球相关的商品</p>
    </div>
    
    <!-- 搜索区域 -->
    <el-card class="search-card" shadow="never">
      <div class="search-box">
        <el-form :model="queryParams" inline>
          <el-form-item label="商品名称">
            <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable />
          </el-form-item>
          <el-form-item label="商品类型">
            <el-select v-model="queryParams.type" placeholder="请选择类型" clearable>
              <el-option label="器材" :value="1" />
              <el-option label="服装" :value="2" />
              <el-option label="配件" :value="3" />
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
    </el-card>

    <!-- 商品卡片展示 -->
    <el-row :gutter="20" v-loading="loading" class="product-grid">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="product in tableData" :key="product.id" class="product-col">
        <el-card shadow="hover" class="product-card">
          <div class="product-image-container">
            <img v-if="product.image" :src="product.image" class="product-image" @error="handleProductImageLoadError" />
            <div v-else class="product-placeholder">
              <el-icon :size="60" color="#909399"><Picture /></el-icon>
              <div class="placeholder-text">暂无图片</div>
            </div>
          </div>
          <div class="product-content">
            <div class="product-header">
              <h3 class="product-name">{{ product.name }}</h3>
              <el-tag :type="product.status === 1 ? 'success' : 'info'">{{ product.status === 1 ? '在售' : '下架' }}</el-tag>
            </div>
            <div class="product-type-tag">
              <el-tag size="small" type="info">{{ typeLabel(product.type) }}</el-tag>
            </div>
            <p class="product-desc" v-if="product.description">{{ product.description }}</p>
            <div class="product-footer">
              <div class="product-price">
                <span class="currency">¥</span>
                <span class="price-num">{{ product.price }}</span>
              </div>
              <div class="product-stock">库存: {{ product.stock }}</div>
            </div>
            <div class="product-actions">
              <el-button
                type="primary"
                :disabled="product.status !== 1 || product.stock <= 0"
                @click="handleBuy(product)"
              >
                {{ product.stock <= 0 ? '已售罄' : '立即购买' }}
              </el-button>
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

    <!-- 购买弹窗 -->
    <el-dialog v-model="buyDialogVisible" title="确认购买" width="500px">
      <div v-if="selectedProduct" class="buy-dialog-content">
        <div class="product-info">
          <img v-if="selectedProduct.image" :src="selectedProduct.image" class="product-thumb" />
          <div class="info">
            <h3>{{ selectedProduct.name }}</h3>
            <p class="price">¥{{ selectedProduct.price }}</p>
            <p class="stock">库存: {{ selectedProduct.stock }}</p>
          </div>
        </div>

        <el-form :model="buyForm" label-width="80px" style="margin-top: 20px;">
          <el-form-item label="购买数量">
            <el-input-number
              v-model="buyForm.quantity"
              :min="1"
              :max="selectedProduct.stock"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="收货人">
            <el-input v-model="buyForm.receiver" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="buyForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="收货地址">
            <el-input v-model="buyForm.address" type="textarea" :rows="2" placeholder="请输入收货地址" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="buyForm.remark" type="textarea" :rows="2" placeholder="选填" />
          </el-form-item>
        </el-form>

        <div class="total-amount">
          <span>总计:</span>
          <span class="amount">¥{{ (selectedProduct.price * buyForm.quantity).toFixed(2) }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="buyLoading" @click="handleConfirmBuy">确认购买</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import { ProductAPI, ProductVO, ProductQuery } from '@/api/product'
import { OrderAPI } from '@/api/order'
import { AuthAPI } from '@/api/auth'
import { useUserStore } from '@/stores/modules/user'

defineOptions({ name: 'ClientProduct' })

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<ProductVO[]>([])
const total = ref(0)
const buyDialogVisible = ref(false)
const buyLoading = ref(false)
const selectedProduct = ref<ProductVO | null>(null)

const queryParams = reactive<ProductQuery>({
  pageNum: 1,
  pageSize: 8,
  name: '',
  status: 1,
  type: undefined
})

const buyForm = reactive({
  quantity: 1,
  receiver: '',
  phone: '',
  address: '',
  remark: ''
})

const typeLabel = (type: number) => {
  const map: Record<number, string> = {
    1: '器材',
    2: '服装',
    3: '配件'
  }
  return map[type] || '未知'
}

const handleProductImageLoadError = (e: any) => {
  console.log('Image load error:', e)
}

const getList = async () => {
  loading.value = true
  try {
    const res = await ProductAPI.getList(queryParams)
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
  queryParams.name = ''
  queryParams.type = undefined
  handleSearch()
}

const handleBuy = (product: ProductVO) => {
  if (!userStore.getUserToken()) {
    ElMessage.warning('请先登录')
    return
  }

  selectedProduct.value = product
  buyForm.quantity = 1
  buyForm.receiver = ''
  buyForm.phone = ''
  buyForm.address = ''
  buyForm.remark = ''
  buyDialogVisible.value = true
}

const refreshUserInfo = async () => {
  try {
    await AuthAPI.getMemberInfo()
  } catch (e) {
    console.error('刷新用户信息失败', e)
  }
}

const handleConfirmBuy = async () => {
  if (!selectedProduct.value) return

  if (!buyForm.receiver) {
    ElMessage.warning('请输入收货人')
    return
  }
  if (!buyForm.phone) {
    ElMessage.warning('请输入联系电话')
    return
  }
  if (!buyForm.address) {
    ElMessage.warning('请输入收货地址')
    return
  }

  buyLoading.value = true
  try {
    await OrderAPI.createOrder({
      productId: selectedProduct.value.id!,
      quantity: buyForm.quantity,
      receiver: buyForm.receiver,
      phone: buyForm.phone,
      address: buyForm.address,
      remark: buyForm.remark
    })

    ElMessage.success('购买成功！')
    buyDialogVisible.value = false
    await refreshUserInfo() // 刷新用户信息以更新余额
    getList()
  } catch (err: any) {
    ElMessage.error(err.message || '购买失败')
  } finally {
    buyLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.product-list {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 700;
      color: #1e293b;
    }
    
    .page-desc {
      margin: 0;
      font-size: 14px;
      color: #64748b;
    }
  }
  
  .search-card {
    border: none;
    border-radius: 16px;
    margin-bottom: 24px;
    
    .search-box {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .product-grid {
    margin-top: 0;
  }

  .product-col {
    margin-bottom: 20px;
  }

  .product-card {
    transition: all 0.3s;
    border: none;
    border-radius: 12px;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
    }
  }

  .product-image-container {
    width: 100%;
    height: 200px;
    overflow: hidden;
    border-radius: 8px;
    background: #f8fafc;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .product-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .product-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
  }

  .placeholder-text {
    color: #94a3b8;
    font-size: 14px;
  }

  .product-content {
    padding: 16px 0 0;
  }

  .product-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  .product-name {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    margin: 0;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding-right: 10px;
  }

  .product-type-tag {
    margin-bottom: 8px;
  }

  .product-desc {
    font-size: 13px;
    color: #64748b;
    margin: 8px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .product-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f1f5f9;
  }

  .product-price {
    display: flex;
    align-items: baseline;
  }

  .currency {
    font-size: 14px;
    color: #ef4444;
    font-weight: 700;
  }

  .price-num {
    font-size: 22px;
    color: #ef4444;
    font-weight: 700;
  }

  .product-stock {
    font-size: 13px;
    color: #94a3b8;
  }

  .product-actions {
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

.buy-dialog-content {
  .product-info {
    display: flex;
    gap: 16px;
    padding: 16px;
    background: #f8fafc;
    border-radius: 12px;

    .product-thumb {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 8px;
    }

    .info {
      flex: 1;

      h3 {
        margin: 0 0 8px;
        font-size: 16px;
        color: #1e293b;
        font-weight: 700;
      }

      .price {
        font-size: 20px;
        color: #ef4444;
        font-weight: 700;
        margin: 0 0 4px;
      }

      .stock {
        font-size: 13px;
        color: #94a3b8;
        margin: 0;
      }
    }
  }

  .total-amount {
    display: flex;
    justify-content: flex-end;
    align-items: baseline;
    padding-top: 16px;
    border-top: 1px solid #e2e8f0;
    font-size: 14px;
    color: #64748b;

    .amount {
      font-size: 24px;
      color: #ef4444;
      font-weight: 700;
      margin-left: 8px;
    }
  }
}
</style>
