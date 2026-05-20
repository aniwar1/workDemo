<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据标注</span>
          <el-select v-model="selectedTaskId" placeholder="选择标注任务" clearable @change="onTaskChange" style="width: 240px">
            <el-option v-for="t in tasks" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </div>
      </template>

      <div v-if="!selectedTaskId" class="empty-state">
        <el-empty description="请先选择一个标注任务">
          <el-button type="primary" @click="$router.push('/dl/annotation-auth')">去分配任务</el-button>
        </el-empty>
      </div>

      <div v-else class="annotate-workspace">
        <div class="annotation-toolbar">
          <div class="toolbar-left">
            <el-tag v-if="currentRecord">记录 #{{ currentRecord.id }}</el-tag>
            <el-tag v-if="annotationMode === 'entity'" type="primary">实体标注模式</el-tag>
            <el-tag v-if="annotationMode === 'relation'" type="warning">关系标注模式</el-tag>
          </div>
          <div class="toolbar-right">
            <el-button size="small" @click="annotationMode = 'entity'" :type="annotationMode === 'entity' ? 'primary' : ''">实体标注</el-button>
            <el-button size="small" @click="annotationMode = 'relation'" :type="annotationMode === 'relation' ? 'warning' : ''">关系标注</el-button>
          </div>
        </div>

        <div class="annotation-content">
          <div class="text-panel" @mouseup="onTextSelect" :class="{ 'relation-mode': annotationMode === 'relation' }">
            <div class="text-display" v-if="currentRecord" ref="textDisplayRef">
              <span
                v-for="(span, idx) in textSpans" :key="idx"
                :class="getSpanClass(span)"
                :data-start="span.start"
                :data-end="span.end"
                @click="onSpanClick(span)"
              >{{ span.text }}</span>
            </div>
            <div v-else class="no-record">
              <el-empty description="该任务暂无待标注数据" :image-size="80" />
              <el-button type="primary" @click="generateRecords" :loading="generating" style="margin-top:12px">生成示例数据</el-button>
            </div>
          </div>

          <div class="side-panel">
            <el-card shadow="never" class="entity-panel">
              <template #header>实体列表</template>
              <el-tag
                v-for="e in entities" :key="e.id"
                closable @close="removeEntity(e)"
                :type="e.type" style="margin: 4px"
              >
                {{ e.text }} ({{ e.label }})
              </el-tag>
              <div v-if="entities.length === 0" class="hint-text">选中文本来创建实体</div>
            </el-card>

            <el-card shadow="never" class="relation-panel">
              <template #header>关系列表</template>
              <div v-for="r in relations" :key="r.id" class="relation-item">
                <span class="rel-source">{{ getEntityText(r.sourceId) }}</span>
                <span class="rel-arrow">{{ r.type }}</span>
                <span class="rel-target">{{ getEntityText(r.targetId) }}</span>
                <el-icon class="rel-del" @click="removeRelation(r)"><Close /></el-icon>
              </div>
              <div v-if="relations.length === 0" class="hint-text">点击两个实体来创建关系</div>
            </el-card>

            <el-card shadow="never" class="entity-type-panel">
              <template #header>实体类型</template>
              <el-button
                v-for="t in entityTypes" :key="t.value"
                size="small" :type="selectedEntityType === t.value ? 'primary' : 'default'"
                style="margin: 3px" @click="selectedEntityType = t.value"
              >{{ t.label }}</el-button>
            </el-card>
          </div>
        </div>

        <div class="annotation-footer">
          <el-button @click="skipRecord" :disabled="!currentRecord">跳过</el-button>
          <el-button type="primary" @click="submitAnnotation" :disabled="!currentRecord || entities.length === 0" :loading="submitting">
            提交标注
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTaskList, getNextRecord, saveRecord, generateRecords as apiGenerate } from '@/api/annotation'

const selectedTaskId = ref(null)
const tasks = ref([])
const currentRecord = ref(null)
const entities = ref([])
const relations = ref([])
const annotationMode = ref('entity')
const selectedEntityType = ref('PERSON')
const textSpans = ref([])
const textDisplayRef = ref(null)
const submitting = ref(false)
const generating = ref(false)
const lastSelectedEntityId = ref(null)

const entityTypes = [
  { label: '人物', value: 'PERSON' },
  { label: '地点', value: 'LOCATION' },
  { label: '组织', value: 'ORG' },
  { label: '时间', value: 'TIME' },
  { label: '实体', value: 'MISC' }
]

const loadTasks = async () => {
  const res = await getTaskList({ pageNum: 1, pageSize: 100 })
  tasks.value = res.data?.list || []
  if (tasks.value.length > 0 && !selectedTaskId.value) {
    selectedTaskId.value = tasks.value[0].id
    loadNextRecord()
  }
}

const onTaskChange = async () => {
  entities.value = []
  relations.value = []
  currentRecord.value = null
  await loadNextRecord()
}

const loadNextRecord = async () => {
  if (!selectedTaskId.value) return
  try {
    const res = await getNextRecord(selectedTaskId.value)
    currentRecord.value = res.data
    if (currentRecord.value?.id) {
      buildTextSpans(currentRecord.value.content || '')
    }
  } catch (e) {
    currentRecord.value = null
  }
}

const buildTextSpans = (content) => {
  const annotatedRanges = []
  entities.value.forEach(e => {
    annotatedRanges.push({ start: e.start, end: e.end, entity: e })
  })
  annotatedRanges.sort((a, b) => a.start - b.start)

  textSpans.value = []
  let pos = 0
  for (const range of annotatedRanges) {
    if (range.start > pos) {
      textSpans.value.push({ text: content.slice(pos, range.start), start: pos, end: range.start, isEntity: false })
    }
    textSpans.value.push({ text: content.slice(range.start, range.end), start: range.start, end: range.end, isEntity: true, entity: range.entity })
    pos = range.end
  }
  if (pos < content.length) {
    textSpans.value.push({ text: content.slice(pos), start: pos, end: content.length, isEntity: false })
  }
  if (textSpans.value.length === 0 && content) {
    textSpans.value = [{ text: content, start: 0, end: content.length, isEntity: false }]
  }
}

const getSpanClass = (span) => {
  if (span.isEntity) {
    const colorMap = { PERSON: 'entity-person', LOCATION: 'entity-location', ORG: 'entity-org', TIME: 'entity-time', MISC: 'entity-misc' }
    return colorMap[span.entity?.label] || 'entity-misc'
  }
  return ''
}

const onTextSelect = () => {
  if (annotationMode.value !== 'entity') return
  const selection = window.getSelection()
  if (!selection || selection.isCollapsed) return
  const selectedText = selection.toString().trim()
  if (!selectedText) return
  const range = selection.getRangeAt(0)
  const container = textDisplayRef.value
  if (!container || !container.contains(range.commonAncestorContainer)) return
}

const onSpanClick = (span) => {
  if (annotationMode.value === 'entity' && !span.isEntity) {
    const text = span.text.trim()
    if (!text) return
    const entityStart = currentRecord.value.content.indexOf(text)
    if (entityStart === -1) return
    entities.value.push({
      id: Date.now(),
      text,
      label: selectedEntityType.value,
      start: entityStart,
      end: entityStart + text.length,
      type: getEntityTagType(selectedEntityType.value)
    })
    buildTextSpans(currentRecord.value.content)
  } else if (annotationMode.value === 'relation' && span.isEntity) {
    if (lastSelectedEntityId.value === null) {
      lastSelectedEntityId.value = span.entity.id
    } else {
      if (lastSelectedEntityId.value !== span.entity.id) {
        relations.value.push({
          id: Date.now(),
          sourceId: lastSelectedEntityId.value,
          targetId: span.entity.id,
          type: '关联'
        })
      }
      lastSelectedEntityId.value = null
    }
  }
}

const getEntityTagType = (label) => {
  const map = { PERSON: '', LOCATION: 'success', ORG: 'warning', TIME: 'info', MISC: '' }
  return map[label] || ''
}

const removeEntity = (e) => {
  entities.value = entities.value.filter(x => x.id !== e.id)
  if (currentRecord.value) buildTextSpans(currentRecord.value.content)
}

const removeRelation = (r) => {
  relations.value = relations.value.filter(x => x.id !== r.id)
}

const getEntityText = (id) => {
  const e = entities.value.find(x => x.id === id)
  return e ? e.text : '?'
}

const skipRecord = () => {
  entities.value = []
  relations.value = []
  loadNextRecord()
}

const submitAnnotation = async () => {
  if (!currentRecord.value) return
  submitting.value = true
  try {
    await saveRecord({
      taskId: selectedTaskId.value,
      corpusId: currentRecord.value.corpusId,
      content: currentRecord.value.content,
      annotation: JSON.stringify({ entities: entities.value, relations: relations.value }),
      entityTypes: entities.value.map(e => e.label).join(','),
      relationTypes: relations.value.map(r => r.type).join(',')
    })
    ElMessage.success('标注已提交')
    entities.value = []
    relations.value = []
    loadNextRecord()
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const generateRecords = async () => {
  if (!selectedTaskId.value) return
  generating.value = true
  try {
    await apiGenerate(selectedTaskId.value)
    ElMessage.success('示例数据已生成')
    loadNextRecord()
  } catch (e) {
    ElMessage.error('生成失败')
  } finally {
    generating.value = false
  }
}

onMounted(() => loadTasks())
</script>

<style scoped>
.page-container { padding: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.empty-state { display: flex; justify-content: center; padding: 60px 0; }
.annotate-workspace { display: flex; flex-direction: column; gap: 12px; }
.annotation-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid #eee; }
.toolbar-left { display: flex; gap: 8px; align-items: center; }
.toolbar-right { display: flex; gap: 8px; }
.annotation-content { display: flex; gap: 16px; min-height: 320px; }
.text-panel { flex: 1; border: 1px solid #e8e8e8; border-radius: 8px; padding: 16px; background: #fafafa; overflow-y: auto; max-height: 400px; cursor: text; }
.text-display { font-size: 15px; line-height: 2; color: #333; user-select: text; }
.entity-person { background: #dce8ff; border-radius: 3px; padding: 1px 2px; cursor: pointer; }
.entity-location { background: #e8ffd8; border-radius: 3px; padding: 1px 2px; cursor: pointer; }
.entity-org { background: #fff3d8; border-radius: 3px; padding: 1px 2px; cursor: pointer; }
.entity-time { background: #f0e8ff; border-radius: 3px; padding: 1px 2px; cursor: pointer; }
.entity-misc { background: #ffe8e8; border-radius: 3px; padding: 1px 2px; cursor: pointer; }
.side-panel { width: 280px; display: flex; flex-direction: column; gap: 10px; }
.entity-panel, .relation-panel, .entity-type-panel { font-size: 13px; }
.relation-item { display: flex; align-items: center; gap: 6px; padding: 4px 0; border-bottom: 1px solid #f0f0f0; }
.rel-source, .rel-target { font-size: 12px; background: #e8f4ff; padding: 2px 6px; border-radius: 3px; }
.rel-arrow { color: #666; font-size: 12px; }
.rel-del { cursor: pointer; color: #999; margin-left: auto; }
.rel-del:hover { color: #f56c6c; }
.hint-text { color: #bbb; font-size: 12px; text-align: center; padding: 8px 0; }
.annotation-footer { display: flex; justify-content: flex-end; gap: 10px; padding-top: 12px; border-top: 1px solid #eee; }
.no-record { display: flex; flex-direction: column; align-items: center; padding: 20px 0; }
</style>
