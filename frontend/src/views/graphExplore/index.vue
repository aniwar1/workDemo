<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">知识图谱探索</span>
          <div class="header-actions">
            <el-button @click="showPathDialog = true" :disabled="!currentGraphId" size="small" plain>
              <el-icon><Connection /></el-icon> 路径分析
            </el-button>
            <el-button @click="exportGraph" :disabled="!currentGraphId" size="small" plain>
              <el-icon><Download /></el-icon> 导出PNG
            </el-button>
            <el-button @click="toggleFullscreen" :disabled="!currentGraphId" size="small" plain>
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="explore-toolbar">
        <el-select v-model="currentGraphId" placeholder="请选择图谱" @change="onGraphChange" style="width: 220px">
          <el-option v-for="g in graphs" :key="g.id" :label="g.name" :value="g.id" />
        </el-select>
        <el-input v-model="searchKeyword" placeholder="搜索节点" clearable style="width: 200px" @keyup.enter="searchNodes">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="searchNodes" :loading="loading">查询</el-button>
        <el-select v-model="selectedRelationType" placeholder="关系类型过滤" clearable style="width: 160px" @change="loadGraphData">
          <el-option v-for="t in relationTypes" :key="t" :label="t" :value="t" />
        </el-select>
        <div class="toolbar-right">
          <el-tooltip content="适应画布">
            <el-button size="small" @click="chart && chart.resize()"><el-icon><FullScreen /></el-icon></el-button>
          </el-tooltip>
        </div>
      </div>

      <div class="graph-display" ref="graphContainer">
        <div v-if="loading" class="loading-overlay">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else-if="!currentGraphId" class="empty-state">
          <el-empty description="请先选择一个图谱" />
        </div>
        <div v-else-if="nodes.length === 0" class="empty-state">
          <el-empty description="该图谱暂无数据" />
        </div>
        <div v-else ref="chartRef" class="echarts-wrapper"></div>
      </div>

      <div class="graph-info-bar">
        <span class="stat-item">
          <el-icon><Coin /></el-icon> 节点: <b>{{ totalNodeCount }}</b>
        </span>
        <span class="stat-item">
          <el-icon><Connection /></el-icon> 边: <b>{{ totalEdgeCount }}</b>
        </span>
        <span class="stat-item" v-if="selectedRelationType">
          <el-icon><Filter /></el-icon> 过滤: {{ selectedRelationType }}
        </span>
        <span class="stat-item" v-if="highlightedNodes.size > 0">
          <el-icon><Aim /></el-icon> 匹配: <b>{{ highlightedNodes.size }}</b> 个节点
        </span>
      </div>
    </el-card>

    <!-- 节点详情 -->
    <el-drawer v-model="showNodeDetail" :title="selectedNode?.nodeName || '节点详情'" size="380px">
      <el-descriptions :column="1" border v-if="selectedNode">
        <el-descriptions-item label="节点名称">{{ selectedNode.nodeName }}</el-descriptions-item>
        <el-descriptions-item label="节点类型">{{ selectedNode.nodeType }}</el-descriptions-item>
        <el-descriptions-item label="图谱ID">{{ selectedNode.graphId }}</el-descriptions-item>
        <el-descriptions-item label="属性">
          <pre class="props-pre">{{ formatProps(selectedNode.properties) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>

    <!-- 路径分析 -->
    <el-dialog v-model="showPathDialog" title="路径分析" width="500px">
      <el-form label-width="80px">
        <el-form-item label="起始节点">
          <el-select v-model="pathStartId" filterable placeholder="选择起始节点" style="width:100%">
            <el-option v-for="n in nodes" :key="n.id" :label="n.nodeName" :value="n.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标节点">
          <el-select v-model="pathEndId" filterable placeholder="选择目标节点" style="width:100%">
            <el-option v-for="n in nodes" :key="n.id" :label="n.nodeName" :value="n.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPathDialog = false">取消</el-button>
        <el-button type="primary" @click="doPathAnalysis" :loading="pathLoading">分析</el-button>
      </template>
    </el-dialog>

    <!-- 路径结果 -->
    <el-dialog v-model="showPathResult" title="路径分析结果" width="600px">
      <el-alert v-if="pathResult.length === 0" type="info" description="未找到路径" :closable="false" />
      <div v-else>
        <el-steps :space="80" direction="horizontal" finish-status="success">
          <el-step v-for="(node, idx) in pathResult" :key="node.id || idx"
            :title="node.name || node.nodeName || `节点${idx+1}`" />
        </el-steps>
        <div class="path-nodes">
          <el-tag v-for="(node, idx) in pathResult" :key="node.id || idx" type="primary" style="margin: 4px">
            {{ node.name || node.nodeName }}
          </el-tag>
          <span v-if="pathResult.length > 0" style="margin-left:8px;color:#999">
            (共 {{ pathResult.length }} 个节点)
          </span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import {
  getGraphs, getGraphData, searchNodes as apiSearchNodes,
  getGraphStats, findPath, getRelationTypes
} from '@/api/graphExplore'

const currentGraphId = ref(null)
const searchKeyword = ref('')
const selectedRelationType = ref('')
const graphs = ref([])
const nodes = ref([])
const edges = ref([])
const relationTypes = ref([])
const totalNodeCount = ref(0)
const totalEdgeCount = ref(0)
const loading = ref(false)
const highlightedNodes = ref(new Set())

const chartRef = ref(null)
const chart = ref(null)
const showNodeDetail = ref(false)
const selectedNode = ref(null)
const showPathDialog = ref(false)
const showPathResult = ref(false)
const pathStartId = ref(null)
const pathEndId = ref(null)
const pathResult = ref([])
const pathLoading = ref(false)

const ENTITY_TYPE_COLORS = {
  '人物': '#5470c8', '地点': '#91cc75', '组织': '#fac858',
  '事件': '#ee6666', '概念': '#73c0de', '产品': '#3ba272',
  '公司': '#fc8452', '品牌': '#9a60b4', '实体': '#48b8d0'
}

const getNodeColor = (nodeType) => ENTITY_TYPE_COLORS[nodeType] || '#48b8d0'

const loadGraphs = async () => {
  try {
    const res = await getGraphs()
    graphs.value = res.data || []
    if (graphs.value.length > 0 && !currentGraphId.value) {
      currentGraphId.value = graphs.value[0].id
      await loadGraphData()
    }
  } catch (e) {
    ElMessage.error('加载图谱列表失败')
  }
}

const onGraphChange = async () => {
  searchKeyword.value = ''
  selectedRelationType.value = ''
  highlightedNodes.value = new Set()
  await loadGraphData()
}

const loadGraphData = async () => {
  if (!currentGraphId.value) return
  loading.value = true
  highlightedNodes.value = new Set()
  try {
    const res = await getGraphData(currentGraphId.value, selectedRelationType.value || undefined)
    nodes.value = res.data?.nodes || []
    edges.value = res.data?.edges || []
    totalNodeCount.value = res.data?.totalNodeCount || nodes.value.length
    totalEdgeCount.value = res.data?.totalEdgeCount || edges.value.length

    const typesRes = await getRelationTypes(currentGraphId.value)
    relationTypes.value = typesRes.data || []
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error('加载图谱数据失败')
  } finally {
    loading.value = false
  }
}

const searchNodes = async () => {
  if (!searchKeyword.value.trim()) {
    highlightedNodes.value = new Set()
    await loadGraphData()
    return
  }
  highlightedNodes.value = new Set()
  try {
    const res = await apiSearchNodes(searchKeyword.value, currentGraphId.value)
    const found = res.data || []
    highlightedNodes.value = new Set(found.map(n => n.id))
    nodes.value = found
    edges.value = []
    if (found.length === 0) {
      ElMessage.warning('未找到匹配的节点')
    }
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

const buildChartData = () => {
  const chartNodes = nodes.value.map(n => ({
    id: n.id,
    name: n.nodeName || n.name || `Node-${n.id}`,
    nodeType: n.nodeType || n.entityType || '实体',
    itemStyle: {
      color: highlightedNodes.value.has(n.id) ? '#ff6600' : getNodeColor(n.nodeType || n.entityType)
    },
    symbolSize: highlightedNodes.value.has(n.id) ? 50 : 38
  }))

  const nodeIdSet = new Set(chartNodes.map(n => n.id))
  const chartEdges = edges.value
    .filter(e => nodeIdSet.has(e.sourceNodeId) && nodeIdSet.has(e.targetNodeId))
    .map(e => ({
      source: e.sourceNodeId,
      target: e.targetNodeId,
      name: e.relationType || '',
      lineStyle: { color: '#aaa', width: 1.5 }
    }))

  return { chartNodes, chartEdges }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (chart.value) {
    chart.value.dispose()
  }

  chart.value = echarts.init(chartRef.value)
  const { chartNodes, chartEdges } = buildChartData()

  const option = {
    backgroundColor: '#fafafa',
    tooltip: {
      trigger: 'item',
      formatter: (p) => {
        if (p.dataType === 'node') {
          return `<b>${p.data.name}</b><br/>类型: ${p.data.nodeType}`
        }
        return p.data.name || ''
      }
    },
    legend: {
      data: [...new Set(nodes.value.map(n => n.nodeType || n.entityType || '实体'))],
      top: 10, right: 10
    },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'right',
        fontSize: 11,
        color: '#333',
        formatter: (p) => p.data.name.length > 12 ? p.data.name.slice(0, 12) + '...' : p.data.name
      },
      lineStyle: { curveness: 0.3 },
      emphasis: {
        focus: 'adjacency',
        lineStyle: { width: 3 }
      },
      categories: [...new Set(nodes.value.map(n => n.nodeType || n.entityType || '实体'))].map(t => ({ name: t })),
      data: chartNodes,
      links: chartEdges
    }]
  }

  chart.value.setOption(option, true)

  chart.value.on('click', (params) => {
    if (params.dataType === 'node') {
      const node = nodes.value.find(n => n.id === params.data.id)
      if (node) {
        selectedNode.value = node
        showNodeDetail.value = true
      }
    }
  })
}

const doPathAnalysis = async () => {
  if (!pathStartId.value || !pathEndId.value) {
    ElMessage.warning('请选择起始和目标节点')
    return
  }
  if (pathStartId.value === pathEndId.value) {
    ElMessage.warning('起始和目标节点不能相同')
    return
  }
  pathLoading.value = true
  try {
    const res = await findPath(pathStartId.value, pathEndId.value, currentGraphId.value)
    pathResult.value = res.data || []
    showPathDialog.value = false
    showPathResult.value = true
    if (pathResult.value.length === 0) {
      ElMessage.info('未找到两点之间的路径')
    }
  } catch (e) {
    ElMessage.error('路径分析失败')
  } finally {
    pathLoading.value = false
  }
}

const exportGraph = () => {
  if (!chart.value) return
  const url = chart.value.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#fafafa' })
  const a = document.createElement('a')
  a.href = url
  a.download = `graph-${currentGraphId.value}-${Date.now()}.png`
  a.click()
  ElMessage.success('导出成功')
}

const toggleFullscreen = () => {
  if (!chartRef.value) return
  if (!document.fullscreenElement) {
    chartRef.value.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const formatProps = (props) => {
  if (!props) return '{}'
  try {
    return JSON.stringify(typeof props === 'string' ? JSON.parse(props) : props, null, 2)
  } catch {
    return props
  }
}

let resizeObserver = null
onMounted(async () => {
  await loadGraphs()
  resizeObserver = new ResizeObserver(() => {
    chart.value?.resize()
  })
  if (chartRef.value) {
    resizeObserver.observe(chartRef.value)
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  chart.value?.dispose()
})
</script>

<style scoped>
.page-container { padding: 8px; height: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; }
.header-actions { display: flex; gap: 6px; }
.explore-toolbar { display: flex; gap: 10px; margin-bottom: 12px; align-items: center; flex-wrap: wrap; }
.toolbar-right { margin-left: auto; }
.graph-display { border: 1px solid #e8e8e8; border-radius: 8px; min-height: 500px; position: relative; background: #fafafa; }
.echarts-wrapper { width: 100%; height: 520px; }
.loading-overlay { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: #999; z-index: 10; background: rgba(250,250,250,0.8); }
.empty-state { display: flex; align-items: center; justify-content: center; min-height: 400px; }
.graph-info-bar { display: flex; gap: 20px; padding: 10px 14px; background: #f5f7fa; border-top: 1px solid #eee; border-radius: 0 0 8px 8px; font-size: 13px; color: #666; }
.stat-item { display: flex; align-items: center; gap: 4px; }
.props-pre { font-size: 12px; background: #f5f7fa; padding: 8px; border-radius: 4px; margin: 4px 0; white-space: pre-wrap; word-break: break-all; }
.path-nodes { margin-top: 16px; display: flex; flex-wrap: wrap; gap: 6px; align-items: center; }
</style>
