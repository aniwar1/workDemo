import request from '@/utils/request'

export function getUserList(params) {
  return request.get('/system/user/list', { params })
}

export function getUserById(id) {
  return request.get(`/system/user/${id}`)
}

export function addUser(data) {
  return request.post('/system/user', data)
}

export function updateUser(id, data) {
  return request.put(`/system/user/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/system/user/${id}`)
}

export function resetPassword(id) {
  return request.post(`/system/user/${id}/reset-password`)
}
