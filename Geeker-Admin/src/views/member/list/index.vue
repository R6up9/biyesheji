<template>
  <div class="member-list-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <h3>会员管理</h3>
        </div>
      </template>

      <div class="search-wrapper">
        <el-form :model="searchForm" inline class="search-form">
          <el-form-item label="姓名">
            <el-input v-model="searchForm.realName" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" placeholder="请选择性别" clearable @change="handleSearch">
              <el-option label="全部" :value="undefined" />
              <el-option label="未知" :value="0" />
              <el-option label="男" :value="1" />
              <el-option label="女" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="等级">
            <el-select v-model="searchForm.level" placeholder="请选择等级" clearable @change="handleSearch">
              <el-option label="全部" :value="undefined" />
              <el-option label="Lv.1" :value="1" />
              <el-option label="Lv.2" :value="2" />
              <el-option label="Lv.3" :value="3" />
              <el-option label="Lv.4" :value="4" />
              <el-option label="Lv.5" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="table-actions">
        <el-button type="primary" @click="openDialog()">新增会员</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe class="member-table" @sort-change="handleSortChange">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            <span>{{ genderMap[row.gender] ?? '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="level" label="等级" width="80" align="center" sortable>
          <template #default="{ row }">
            <el-tag size="small" :type="getLevelType(row.level)">Lv.{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="points" label="积分" width="80" align="center" sortable />
        <el-table-column prop="balance" label="余额" width="100" align="center" sortable>
          <template #default="{ row }">
            <span class="balance-text">¥{{ (row.balance || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="110">
          <template #default="{ row }">
            <span>{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" @closed="resetForm">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-divider content-position="left">账号信息</el-divider>
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="formData.username" placeholder="请输入登录账号" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="留空表示不修改" show-password />
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-divider content-position="left">会员信息</el-divider>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="formData.age" :min="1" :max="120" style="width: 100%" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="会员等级" prop="level">
          <el-select v-model="formData.level" style="width: 100%">
            <el-option label="Lv.1" :value="1" />
            <el-option label="Lv.2" :value="2" />
            <el-option label="Lv.3" :value="3" />
            <el-option label="Lv.4" :value="4" />
            <el-option label="Lv.5" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number v-model="formData.points" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="余额" prop="balance">
          <el-input-number v-model="formData.balance" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="简介" prop="introduction">
          <el-input v-model="formData.introduction" type="textarea" :rows="3" placeholder="请输入简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { MemberApi, MemberAPI } from '@/api/member'

const genderMap: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }

const getLevelType = (level: number) => {
  if (level >= 4) return 'success'
  if (level >= 2) return 'warning'
  return undefined
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

const tableData = ref<MemberAPI.MemberVO[]>([])
const loading = ref(false)
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ realName: '', gender: undefined as number | undefined, level: undefined as number | undefined })
const sortForm = reactive({ prop: undefined as string | undefined, order: undefined as string | undefined })

const fetchData = async () => {
  loading.value = true
  try {
    const pageData = await MemberApi.getList({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      realName: searchForm.realName || undefined,
      gender: searchForm.gender,
      level: searchForm.level,
      sortProp: sortForm.prop,
      sortOrder: sortForm.order,
    })
    tableData.value = pageData.records
    pagination.total = pageData.total
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleReset = () => {
  searchForm.realName = ''
  searchForm.gender = undefined
  searchForm.level = undefined
  sortForm.prop = undefined
  sortForm.order = undefined
  pagination.pageNum = 1
  fetchData()
}

const handleSizeChange = () => {
  pagination.pageNum = 1
  fetchData()
}

const handleSortChange = ({ prop, order }: { prop?: string; order?: string }) => {
  sortForm.prop = prop
  sortForm.order = order
  handleSearch()
}

const dialogVisible = ref(false)
const dialogTitle = ref('新增会员')
const formRef = ref<FormInstance>()
const formData = reactive<MemberAPI.MemberVO>({
  username: '',
  password: '',
  status: 1,
  realName: '',
  gender: 0,
  age: undefined,
  phone: '',
  level: 1,
  points: 0,
  balance: 0,
  introduction: '',
})
const rules: FormRules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
}

const openDialog = (row?: MemberAPI.MemberVO) => {
  if (row) {
    dialogTitle.value = '编辑会员'
    Object.assign(formData, row)
    formData.password = ''
  } else {
    dialogTitle.value = '新增会员'
    resetForm()
  }
  dialogVisible.value = true
}

const resetForm = () => {
  formData.id = undefined
  formData.userId = undefined
  formData.username = ''
  formData.password = ''
  formData.status = 1
  formData.realName = ''
  formData.gender = 0
  formData.age = undefined
  formData.phone = ''
  formData.level = 1
  formData.points = 0
  formData.balance = 0
  formData.introduction = ''
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  try {
    if (formData.id) {
      await MemberApi.update(formData)
      ElMessage.success('更新成功')
    } else {
      await MemberApi.add(formData)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row: MemberAPI.MemberVO) => {
  await ElMessageBox.confirm(`确定删除会员「${row.realName}」（账号：${row.username}）吗？删除后会同时删除该账号。`, '提示', { type: 'warning' })
  try {
    await MemberApi.delete(row.id!)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.member-list-page {
  padding: 16px;
  height: calc(100vh - 32px);
  display: flex;
  flex-direction: column;

  .page-header {
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .search-wrapper {
    background: #f5f7fa;
    padding: 12px 16px;
    border-radius: 6px;
    margin-bottom: 16px;
  }

  .search-form {
    margin-bottom: 0;
  }

  .table-actions {
    margin-bottom: 12px;
  }

  .member-table {
    flex: 1;
    overflow: hidden;

    .balance-text {
      color: #f56c6c;
      font-weight: 500;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    padding-top: 12px;
  }

  :deep(.el-card__body) {
    height: 100%;
    display: flex;
    flex-direction: column;
    padding: 16px;
  }
}
</style>
