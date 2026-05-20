import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 通用 CRUD 列表页逻辑，消除重复模板代码。
 *
 * 用法:
 *   const {
 *     tableData, total, loading, query, form, dialogVisible,
 *     loadData, handleAdd, handleEdit, handleSubmit, handleDelete
 *   } = useCrudList({
 *     api: { list, add, update, remove, getById },
 *     formDefaults: { name: '', status: '1' },
 *     listFilter: row => row.filterCondition,
 *   })
 */
export function useCrudList({ api, formDefaults = {}, listFilter = null }) {
  const tableData = ref([])
  const total = ref(0)
  const loading = ref(false)
  const dialogVisible = ref(false)
  const formRef = ref(null)
  const isEdit = ref(false)
  const editingId = ref(null)

  const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

  const form = reactive({ ...formDefaults })

  const loadData = async () => {
    if (!api.list) return
    loading.value = true
    try {
      const res = await api.list(query)
      let records = res.data?.list || res.data?.records || []
      if (listFilter) records = records.filter(listFilter)
      tableData.value = records
      total.value = res.data?.total || records.length
    } catch {
      ElMessage.error('加载数据失败')
    } finally {
      loading.value = false
    }
  }

  const handleAdd = () => {
    Object.assign(form, { ...formDefaults })
    editingId.value = null
    isEdit.value = false
    dialogVisible.value = true
  }

  const handleEdit = (row) => {
    Object.assign(form, { ...row })
    editingId.value = row.id
    isEdit.value = true
    dialogVisible.value = true
  }

  const handleSubmit = async () => {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    try {
      if (isEdit.value) {
        await api.update(editingId.value, form)
        ElMessage.success('更新成功')
      } else {
        await api.add(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '操作失败')
    }
  }

  const handleDelete = async (row) => {
    await ElMessageBox.confirm('确定删除该项吗？', '提示')
    await api.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  }

  return {
    tableData, total, loading, query, form, dialogVisible,
    formRef, isEdit, editingId,
    loadData, handleAdd, handleEdit, handleSubmit, handleDelete,
  }
}

/**
 * 通用分页加载逻辑（仅数据，不含表单）。
 */
export function usePagedList(api) {
  const tableData = ref([])
  const total = ref(0)
  const loading = ref(false)
  const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

  const loadData = async () => {
    loading.value = true
    try {
      const res = await api(query)
      tableData.value = res.data?.list || res.data?.records || []
      total.value = res.data?.total || tableData.value.length
    } catch {
      ElMessage.error('加载数据失败')
    } finally {
      loading.value = false
    }
  }

  return { tableData, total, loading, query, loadData }
}
