import request from '@/utils/request'

export function getTrainList(params) {
  return request.get('/train/list', { params })
}

export function getTrainById(id) {
  return request.get(`/train/${id}`)
}

export function addTrain(data) {
  return request.post('/train', data)
}

export function startTrain(id) {
  return request.post(`/train/${id}/start`)
}

export function stopTrain(id) {
  return request.post(`/train/${id}/stop`)
}

export function getMetrics(id) {
  return request.get(`/train/${id}/metrics`)
}

export function getTrainLogs(id) {
  return request.get(`/train/${id}/logs`)
}

export function deleteTrain(id) {
  return request.delete(`/train/${id}`)
}
