import request from '@/utils/request'

export function getRoleList(params) {
  return request.get('/system/role/list', { params })
}

export function getRoleById(id) {
  return request.get(`/system/role/${id}`)
}

export function addRole(data) {
  return request.post('/system/role', data)
}

export function updateRole(id, data) {
  return request.put(`/system/role/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/system/role/${id}`)
}

export function getAllRoles() {
  return request.get('/system/role/all')
}
