import request from '@/utils/request'

export interface ActivityVO {
  id?: number
  title: string
  content?: string
  image?: string
  startTime: string
  endTime: string
  location: string
  maxParticipants: number
  currentParticipants?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface ActivityQuery {
  pageNum: number
  pageSize: number
  title?: string
  status?: number
}

export const ActivityAPI = {
  getList: (params: ActivityQuery) => {
    return request.get('/admin/activity/list', params)
  },

  getAll: () => {
    return request.get('/admin/activity/all')
  },

  getDetail: (id: number) => {
    return request.get(`/admin/activity/${id}`)
  },

  add: (data: ActivityVO) => {
    return request.post('/admin/activity', data)
  },

  update: (data: ActivityVO) => {
    return request.put('/admin/activity', data)
  },

  delete: (id: number) => {
    return request.delete(`/admin/activity/${id}`)
  },
}

export const ActivityClientAPI = {
  getAll: () => {
    return request.get('/activities')
  },

  getById: (id: number) => {
    return request.get(`/activities/${id}`)
  },

  enroll: (id: number) => {
    return request.post(`/activities/${id}/enroll`)
  },
}
