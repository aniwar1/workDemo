import request from '@/utils/request'

export function getTransformList(params) {
  return request.get('/data/transform/list', { params })
}

export function getTransformById(id) {
  return request.get(`/data/transform/${id}`)
}

export function addTransform(data) {
  return request.post('/data/transform', data)
}

export function executeTransform(id) {
  return request.post(`/data/transform/${id}/execute`)
}

export function deleteTransform(id) {
  return request.delete(`/data/transform/${id}`)
}
