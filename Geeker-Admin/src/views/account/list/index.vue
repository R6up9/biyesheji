<template>
  <div class="account-page">
    <el-card shadow="never">
      <el-form
        :model="searchForm"
        :inline="true"
        class="search-form"
      >
        <el-form-item label="账号">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入账号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            查询
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <div class="table-actions">
        <el-button type="primary" @click="handleAdd">
          新增账号
        </el-button>
      </div>
      <el-table
        :data="tableData"
        stripe
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="账号" width="150" />
        <el-table-column prop="status" label="状态" width="100" align="center">
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
            <el-button
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.username !== 'admin'"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
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
      :title="isEdit ? '编辑账号' : '新增账号'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="账号" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入账号"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            :placeholder="isEdit ? '不修改请留空' : '请输入密码'"
            show-password
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
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button type="primary" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import { AccountApi, type AccountVO, type AccountQuery } from "@/api/account";

const searchForm = reactive<AccountQuery>({
  pageNum: 1,
  pageSize: 10,
  username: "",
});

const tableData = ref<AccountVO[]>([]);
const total = ref(0);
const loading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();

const formData = reactive<AccountVO>({
  id: undefined,
  username: "",
  password: "",
  role: 0,
  status: 1,
});

const formRules: FormRules = {
  username: [
    { required: true, message: "请输入账号", trigger: "blur" },
  ],
  password: [
    { required: false, message: "请输入密码", trigger: "blur" },
  ],
  status: [
    { required: true, message: "请选择状态", trigger: "change" },
  ],
};

const loadData = async () => {
  loading.value = true;
  try {
    const res: any = await AccountApi.getList(searchForm);
    tableData.value = res.records || [];
    total.value = res.total || 0;
  } catch (err: any) {
    ElMessage.error(err.message || "获取数据失败");
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  searchForm.pageNum = 1;
  loadData();
};

const handleReset = () => {
  searchForm.username = "";
  searchForm.pageNum = 1;
  loadData();
};

const handleAdd = () => {
  isEdit.value = false;
  Object.assign(formData, {
    id: undefined,
    username: "",
    password: "",
    role: 0,
    status: 1,
  });
  dialogVisible.value = true;
};

const handleEdit = (row: AccountVO) => {
  isEdit.value = true;
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    password: "",
    role: row.role,
    status: row.status,
  });
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await AccountApi.update(formData);
          ElMessage.success("编辑成功");
        } else {
          await AccountApi.add(formData);
          ElMessage.success("新增成功");
        }
        dialogVisible.value = false;
        loadData();
      } catch (err: any) {
        ElMessage.error(err.message || "操作失败");
      }
    }
  });
};

const handleDelete = (row: AccountAPI.AccountVO) => {
  ElMessageBox.confirm("确认删除该账号吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(async () => {
    try {
      await AccountApi.delete(row.id!);
      ElMessage.success("删除成功");
      loadData();
    } catch (err: any) {
      ElMessage.error(err.message || "删除失败");
    }
  }).catch(() => {});
};

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
};

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.account-page {
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
</style>
