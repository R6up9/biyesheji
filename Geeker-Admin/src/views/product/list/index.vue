<template>
  <div class="product-list card">
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
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="下架" :value="0" />
            <el-option label="上架" :value="1" />
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
      <div class="right-btn">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增商品
        </el-button>
      </div>
    </div>

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
              <el-tag :type="product.status === 1 ? 'success' : 'info'">{{ product.status === 1 ? '上架' : '下架' }}</el-tag>
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
              <el-button type="primary" size="small" @click="handleEdit(product)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(product)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型">
            <el-option label="器材" :value="1" />
            <el-option label="服装" :value="2" />
            <el-option label="配件" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="formData.price" :min="0" :step="1" :precision="2" placeholder="请输入价格" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存数量" prop="stock">
          <el-input-number v-model="formData.stock" :min="0" placeholder="请输入库存" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
            <el-upload
              class="product-image-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeImageUpload"
              :headers="uploadHeaders"
            >
              <img v-if="formData.image" :src="formData.image" class="product-preview-image" />
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
            <el-radio :value="0">下架</el-radio>
            <el-radio :value="1">上架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="4" placeholder="请输入商品详细描述" />
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
import { Search, Refresh, Plus, Picture } from '@element-plus/icons-vue'
import { ProductAPI, ProductVO, ProductQuery } from '@/api/product'
import { useUserStore } from '@/stores/modules/user'

defineOptions({ name: 'ProductList' })

const userStore = useUserStore()
const uploadHeaders = computed(() => {
  return {
    'x-access-token': userStore.getUserToken() || ''
  }
})
const uploadAction = computed(() => {
  return import.meta.env.VITE_API_URL + '/api/uploads/product'
})

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<ProductVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const queryParams = reactive<ProductQuery>({
  pageNum: 1,
  pageSize: 8,
  name: '',
  status: undefined,
  type: undefined
})

const formData = reactive<ProductVO>({
  name: '',
  type: 1,
  price: 0,
  stock: 0,
  image: '',
  description: '',
  status: 1
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择商品类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

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
  console.log('Image src:', e.target.src)
  console.log('Image naturalWidth:', e.target.naturalWidth)
  console.log('Image naturalHeight:', e.target.naturalHeight)
  // 暂时不隐藏，方便调试
  // e.target.style.display = 'none'
}

const handleUploadError = (error: any, file: any) => {
  console.log('Upload error:', error)
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
  console.log('Upload success response:', response)
  // el-upload不会经过axios拦截器，收到的是完整响应
  if (response && response.code === 200 && response.data && response.data.url) {
    formData.image = response.data.url
    console.log('Set formData.image to:', formData.image)
    ElMessage.success('图片上传成功!')
  } else {
    console.log('Unexpected response format')
    ElMessage.error(response?.msg || '图片上传失败')
  }
}

const getList = async () => {
  loading.value = true
  try {
    const res = await ProductAPI.getList(queryParams)
    tableData.value = res.records
    total.value = res.total
    console.log('Product list loaded:', res.records)
    console.log('Product images:', res.records.map((p: any) => p.image))
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
  queryParams.status = undefined
  queryParams.type = undefined
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增商品'
  Object.assign(formData, {
    name: '',
    type: 1,
    price: 0,
    stock: 0,
    image: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: ProductVO) => {
  dialogTitle.value = '编辑商品'
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = async (row: ProductVO) => {
  await ElMessageBox.confirm(`确定要删除商品「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await ProductAPI.delete(row.id!)
  ElMessage.success('删除成功')
  getList()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  console.log('Submitting formData:', formData)
  submitLoading.value = true
  try {
    if (formData.id) {
      await ProductAPI.update(formData)
    } else {
      await ProductAPI.add(formData)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.product-list {
  padding: 20px;
  
  .search-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .product-grid {
    margin-top: 20px;
  }
  
  .product-col {
    margin-bottom: 20px;
  }
  
  .product-card {
    transition: transform 0.3s, box-shadow 0.3s;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
    }
  }
  
  .product-image-container {
    width: 100%;
    height: 200px;
    overflow: hidden;
    border-radius: 4px;
    background: #f5f7fa;
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
    color: #909399;
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
    font-weight: 600;
    color: #303133;
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
    color: #606266;
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
    border-top: 1px solid #f5f7fa;
  }
  
  .product-price {
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
  
  .product-stock {
    font-size: 13px;
    color: #909399;
  }
  
  .product-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }
}

.product-image-uploader {
  :deep(.el-upload) {
    width: 120px;
    height: 120px;
  }
}

.product-preview-image {
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
</style>
