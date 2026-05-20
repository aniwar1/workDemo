import request from '@/utils/request'

export function getGraphData(graphId) {
  return request.get(`/explore/graph/${graphId}`)
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
