import request from '@/utils/request'

export function getGraphList(params) {
  return request.get('/kg/graph/list', { params })
}

export function getGraphById(id) {
  return request.get(`/kg/graph/${id}`)
}

export function addGraph(data) {
  return request.post('/kg/graph', data)
}

export function updateGraph(id, data) {
  return request.put(`/kg/graph/${id}`, data)
}

export function deleteGraph(id) {
  return request.delete(`/kg/graph/${id}`)
}
