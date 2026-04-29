<template>
  <div class="coach-page">
    <el-card shadow="never">
      <el-form
        :model="searchForm"
        :inline="true"
        class="search-form"
      >
        <el-form-item label="教练姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="table-actions">
        <el-button type="primary" @click="handleAdd">新增教练</el-button>
      </div>
      <el-table
        :data="tableData"
        stripe
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            {{ genderMap[row.gender || 0] }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="hourlyRate" label="课时费(元)" width="120" align="right">
          <template #default="{ row }">
            ¥{{ row.hourlyRate || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ row.createTime ? formatDate(row.createTime) : '' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑教练' : '新增教练'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="realName">
          <el-input
            v-model="formData.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
            <el-radio :value="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="formData.age" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入联系电话"
          />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
            <el-upload
              class="avatar-uploader"
              :action="uploadAction"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
            >
              <img v-if="formData.avatar" :src="formData.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-input
              v-model="formData.avatar"
              placeholder="或输入图片URL"
              style="width: 300px"
            />
          </div>
        </el-form-item>
        <el-form-item label="简介" prop="introduction">
          <el-input
            v-model="formData.introduction"
            type="textarea"
            :rows="3"
            placeholder="请输入简介"
          />
        </el-form-item>
        <el-form-item label="特长/资质" prop="skills">
          <el-input
            v-model="formData.skills"
            type="textarea"
            :rows="2"
            placeholder="请输入特长或资质"
          />
        </el-form-item>
        <el-form-item label="课时费(元)" prop="hourlyRate">
          <el-input-number
            v-model="formData.hourlyRate"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="formData.status"
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图片上传弹窗，复用 UploadController -->
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { CoachApi, type CoachAPI } from '@/api/coach'
import { useUserStore } from '@/stores/modules/user'

const userStore = useUserStore()
const uploadAction = computed(() => {
  return import.meta.env.VITE_API_URL + '/api/uploads/coach'
})
const uploadHeaders = computed(() => {
  return {
    'x-access-token': userStore.token || ''
  }
})

const genderMap: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

const handleAvatarSuccess: UploadProps['onSuccess'] = (response) => {
  console.log('上传成功响应:', response)
  if (response.code === 200 && response.data && response.data.url) {
    formData.avatar = response.data.url
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || response.message || '上传失败')
  }
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isJPG = rawFile.type === 'image/jpeg' || rawFile.type === 'image/jpg'
  const isPNG = rawFile.type === 'image/png'
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  realName: '',
  status: undefined as number | undefined,
})

const tableData = ref<CoachAPI.CoachVO[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive<CoachAPI.CoachVO>({
  id: undefined,
  realName: '',
  gender: 1,
  age: undefined,
  phone: '',
  avatar: '',
  introduction: '',
  skills: '',
  hourlyRate: 0,
  status: 1,
})

const formRules: FormRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await CoachApi.getList(searchForm)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (err: any) {
    ElMessage.error(err.message || '获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.realName = ''
  searchForm.status = undefined
  searchForm.pageNum = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, {
    id: undefined,
    realName: '',
    gender: 1,
    age: undefined,
    phone: '',
    avatar: '',
    introduction: '',
    skills: '',
    hourlyRate: 0,
    status: 1,
  })
  dialogVisible.value = true
}

const handleEdit = (row: CoachAPI.CoachVO) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    realName: row.realName,
    gender: row.gender || 1,
    age: row.age,
    phone: row.phone,
    avatar: row.avatar,
    introduction: row.introduction,
    skills: row.skills,
    hourlyRate: row.hourlyRate,
    status: row.status || 1,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await CoachApi.update(formData)
          ElMessage.success('编辑成功')
        } else {
          await CoachApi.add(formData)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (err: any) {
        ElMessage.error(err.message || '操作失败')
      }
    }
  })
}

const handleDelete = (row: CoachAPI.CoachVO) => {
  ElMessageBox.confirm('确定要删除该教练吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await CoachApi.delete(row.id!)
      ElMessage.success('删除成功')
      loadData()
    } catch (err: any) {
      ElMessage.error(err.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.coach-page {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.table-actions {
  margin-bottom: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100px;
  height: 100px;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.avatar-uploader :deep(.el-upload) img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}
</style>
