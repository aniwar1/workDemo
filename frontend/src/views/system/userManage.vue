<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button v-if="isAdmin" type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增用户
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索用户名/昵称" clearable @clear="loadData">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="序号" width="60" align="center">
          <template #default="{ $index }">
            <span class="seq-no">{{ (query.pageNum - 1) * query.pageSize + $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar ? (row.avatar.startsWith('http') ? row.avatar : '/api' + row.avatar) : undefined">
              {{ (row.nickname || row.username || '?')[0].toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column v-if="isAdmin" prop="roleId" label="角色" width="120">
          <template #default="{ row }">
            <span>{{ getRoleName(row.roleId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" :width="isAdmin ? 220 : 0" :fixed="isAdmin ? 'right' : false">
          <template #default="{ row }">
            <template v-if="isAdmin">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80">
        <el-form-item label="头像" label-width="80">
          <div class="avatar-upload">
            <el-avatar
              :size="72"
              :src="form.avatar ? (form.avatar.startsWith('http') ? form.avatar : '/api' + form.avatar) : undefined"
            >
              {{ (form.nickname || form.username || '?')[0].toUpperCase() }}
            </el-avatar>
            <div class="upload-action">
              <el-button size="small" @click="triggerUpload" :loading="uploading">更换头像</el-button>
              <p class="upload-tip">支持 JPG/PNG，不超过 2MB</p>
              <input ref="fileInputRef" type="file" accept="image/jpeg,image/png,image/jpg" style="display:none" @change="handleFileChange" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="显示昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="电子邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="手机号码" />
        </el-form-item>
        <el-form-item v-if="isAdmin" label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getUserList, addUser, updateUser, deleteUser, resetPassword } from '@/api/user'
import { getAllRoles } from '@/api/role'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const roles = ref([])
const formRef = ref(null)
const fileInputRef = ref(null)
const uploading = ref(false)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ id: null, username: '', nickname: '', email: '', phone: '', roleId: null, status: 1, avatar: '' })

const isAdmin = computed(() => userStore.isAdmin)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  const res = await getAllRoles()
  roles.value = res.data
}

const getRoleName = (roleId) => {
  const r = roles.value.find(x => x.id === roleId)
  return r ? r.roleName : '-'
}

const handleAdd = () => {
  Object.assign(form, { id: null, username: '', nickname: '', email: '', phone: '', roleId: null, status: 1, avatar: '' })
  dialogTitle.value = '新增用户'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑用户'
  dialogVisible.value = true
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG/PNG 格式图片')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await request.post('/file/avatar', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.avatar = res.data
    ElMessage.success('头像上传成功')
  } catch {
    // error handled by interceptor
  } finally {
    uploading.value = false
    e.target.value = ''
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (form.id) {
    await updateUser(form.id, form)
    ElMessage.success('修改成功')
  } else {
    await addUser(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm('确定重置「' + row.username + '」的密码为 123456 吗？', '提示')
  await resetPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除用户「' + row.username + '」吗？', '提示')
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); loadRoles() })
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
.search-bar .el-input { width: 300px; }
.seq-no { color: #909399; font-size: 13px; }

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 16px;
}
.upload-action {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.upload-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
</style>
