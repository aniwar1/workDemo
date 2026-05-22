import request from '@/utils/request'

export function kosExtract(data) {
  return request.post('/data/kos/extract', data)
}

export function kosExtractSave(data, graphId) {
  return request.post(`/data/kos/extract-save/${graphId}`, data)
}

export function getKosGraphList(params) {
  return request.get('/data/kos/graph-list', { params })
}

export function getKosGraphData(graphId) {
  return request.get(`/data/kos/graph-data/${graphId}`)
}
