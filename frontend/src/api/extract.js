import request from '@/utils/request'

export function getExtractList(params) {
  return request.get('/extract/list', { params })
}

export function createDlExtract(data) {
  return request.post('/extract/dl', data)
}

export function createLlmExtract(data) {
  return request.post('/extract/llm', data)
}

export function startExtract(id) {
  return request.post(`/extract/${id}/start`)
}

export function getExtractResult(id) {
  return request.get(`/extract/${id}/result`)
}

export function deleteExtract(id) {
  return request.delete(`/extract/${id}`)
}
