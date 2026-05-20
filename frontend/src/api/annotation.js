import request from '@/utils/request'

export function getTaskList(params) {
  return request.get('/annotation/task/list', { params })
}

export function createTask(data) {
  return request.post('/annotation/task', data)
}

export function assignTask(id, assigneeId) {
  return request.post(`/annotation/task/${id}/assign?assigneeId=${assigneeId}`)
}

export function getRecordList(params) {
  return request.get('/annotation/record/list', { params })
}

export function saveRecord(data) {
  return request.post('/annotation/record', data)
}

export function getNextRecord(taskId) {
  return request.get(`/annotation/task/${taskId}/next`)
}

export function deleteTask(id) {
  return request.delete(`/annotation/task/${id}`)
}
