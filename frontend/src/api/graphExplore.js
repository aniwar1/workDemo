import request from '@/utils/request'

export function getGraphData(graphId, relationType) {
  return request.get(`/explore/graph/${graphId}`, { params: { relationType } })
}

export function searchNodes(keyword, graphId) {
  return request.get('/explore/node/search', { params: { keyword, graphId } })
}

export function getNodeDetail(id) {
  return request.get(`/explore/node/${id}`)
}

export function getGraphs() {
  return request.get('/explore/graphs')
}

export function getGraphStats(graphId) {
  return request.get(`/explore/stats/${graphId}`)
}

export function findPath(startId, endId, graphId) {
  return request.post('/explore/path', { startId, endId, graphId })
}

export function getRelationTypes(graphId) {
  return request.get(`/explore/relation-types/${graphId}`)
}
