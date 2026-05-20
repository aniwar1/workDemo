<template>
  <div class="page-container">
    <el-card style="max-width: 560px">
      <template #header>
        <span>{{ isAdmin ? '修改用户密码' : '修改密码' }}</span>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="110">
        <el-form-item v-if="isAdmin" label="目标用户" prop="targetUserId">
          <el-select
            v-model="form.targetUserId"
            filterable
            placeholder="请选择要修改密码的用户"
            style="width: 100%"
            :loading="userLoading"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.username + (user.nickname ? ' (' + user.nickname + ')' : '')"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="!isAdmin" label="旧密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            show-password
            placeholder="请输入旧密码"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isAdmin ? '确认修改' : '确认修改' }}
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { changePassword } from '@/api/auth'
import { adminChangePassword, getUserList } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const formRef = ref(null)
const submitting = ref(false)
const userLoading = ref(false)
const userOptions = ref([])

const form = reactive({
  targetUserId: null,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const getRules = () => {
  const base = {
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      { validator: validateConfirm, trigger: 'blur' }
    ]
  }

  if (isAdmin.value) {
    base.targetUserId = [
      { required: true, message: '请选择要修改密码的用户', trigger: 'change' }
    ]
  } else {
    base.oldPassword = [
      { required: true, message: '请输入旧密码', trigger: 'blur' }
    ]
  }

  return base
}

const rules = getRules()

const loadUsers = async () => {
  if (!isAdmin.value) return
  userLoading.value = true
  try {
    const res = await getUserList({ pageNum: 1, pageSize: 1000 })
    userOptions.value = res.data?.list || res.data || []
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  } finally {
    userLoading.value = false
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isAdmin.value) {
      await adminChangePassword(form.targetUserId, form.newPassword)
      ElMessage.success('密码修改成功')
    } else {
      await changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
      ElMessage.success('密码修改成功')
    }
    handleReset()
  } catch (e) {
    // error already handled by interceptor
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formRef.value?.resetFields()
  form.oldPassword = ''
  form.newPassword = ''
  form.confirmPassword = ''
  form.targetUserId = null
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.page-container {
  padding: 8px;
}
</style>
