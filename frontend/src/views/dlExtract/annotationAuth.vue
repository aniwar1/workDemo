<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>标注授权</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>创建标注任务
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="任务名称" />
        <el-table-column prop="corpusId" label="语料ID" />
        <el-table-column prop="annotationType" label="标注类型" />
        <el-table-column prop="assigneeId" label="分配给">
          <template #default="{ row }">
            {{ row.assigneeId ? '用户#' + row.assigneeId : '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总数量" />
        <el-table-column prop="completedCount" label="已完成">
          <template #default="{ row }">
            <el-progress :percentage="row.totalCount ? Math.round(row.completedCount / row.totalCount * 100) : 0" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button link type="warning" @click="handleAssign(row)">分配</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="语料" prop="corpusId">
          <el-input-number v-model="form.corpusId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="标注类型" prop="annotationType">
          <el-select v-model="form.annotationType" placeholder="请选择标注类型" style="width: 100%">
            <el-option label="实体标注" value="entity" />
            <el-option label="关系标注" value="relation" />
            <el-option label="联合标注" value="joint" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配标注任务" width="400px">
      <el-form :model="assignForm" label-width="80">
        <el-form-item label="任务">
          <span>{{ assignForm.taskName }}</span>
        </el-form-item>
        <el-form-item label="分配给">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择用户" style="width: 100%">
            <el-option label="管理员" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTaskList, createTask, assignTask, deleteTask } from '@/api/annotation'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const assignDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const form = reactive({ name: '', corpusId: null, annotationType: 'entity' })
const assignForm = reactive({ id: null, taskName: '', assigneeId: null })

const rules = { name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }] }
const statusType = (s) => ({ pending: 'info', assigned: 'warning', in_progress: 'primary', completed: 'success' }[s] || 'info')
const statusText = (s) => ({ pending: '待分配', assigned: '已分配', in_progress: '进行中', completed: '已完成' }[s] || s)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTaskList(query)
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, { name: '', corpusId: null, annotationType: 'entity' })
  dialogTitle.value = '创建标注任务'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  await createTask(form)
  ElMessage.success('创建成功')
  dialogVisible.value = false
  loadData()
}

const handleAssign = (row) => {
  Object.assign(assignForm, { id: row.id, taskName: row.name, assigneeId: null })
  assignDialogVisible.value = true
}

const handleAssignSubmit = async () => {
  await assignTask(assignForm.id, assignForm.assigneeId)
  ElMessage.success('分配成功')
  assignDialogVisible.value = false
  loadData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该任务吗？', '提示')
  await deleteTask(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => loadData())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
