import request from '@/utils/request'

export function getCorpusList(params) {
  return request.get('/corpus/list', { params })
}

export function getCorpusById(id) {
  return request.get(`/corpus/${id}`)
}

export function addCorpus(data) {
  return request.post('/corpus', data)
}

export function updateCorpus(id, data) {
  return request.put(`/corpus/${id}`, data)
}

export function deleteCorpus(id) {
  return request.delete(`/corpus/${id}`)
}

export function uploadCorpusFile(formData) {
  return request.post('/corpus/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
