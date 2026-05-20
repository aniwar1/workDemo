import request from '@/utils/request'

export function getModelList(params) {
  return request.get('/kg/model/list', { params })
}

export function getModelById(id) {
  return request.get(`/kg/model/${id}`)
}

export function addModel(data) {
  return request.post('/kg/model', data)
}

export function updateModel(id, data) {
  return request.put(`/kg/model/${id}`, data)
}

export function deleteModel(id) {
  return request.delete(`/kg/model/${id}`)
}
