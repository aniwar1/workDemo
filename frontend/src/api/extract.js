import request from '@/utils/request'

// LLM 知识抽取
export function extractDirect(data) {
  return request.post('/data/extract/direct', data)
}

export function extractDirectSave(data) {
  return request.post('/data/extract/direct-save', data)
}

// 深度学习知识抽取（任务管理）
export function getExtractList(params) {
  return request.get('/data/extract/task/list', { params })
}

export function createDlExtract(data) {
  return request.post('/data/extract/task', data)
}

export function startExtract(id) {
  return request.post(`/data/extract/task/${id}/start`)
}

export function stopExtract(id) {
  return request.post(`/data/extract/task/${id}/stop`)
}

export function getExtractResult(id) {
  return request.get(`/data/extract/task/${id}/result`)
}

export function deleteExtract(id) {
  return request.delete(`/data/extract/task/${id}`)
}
