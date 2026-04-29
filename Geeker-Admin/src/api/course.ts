import request from '@/utils/request'

export interface CourseVO {
  id?: number
  name: string
  coachId: number
  courtId?: number
  startTime: string
  endTime: string
  maxCount: number
  currentCount?: number
  price: number
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface CourseQuery {
  pageNum: number
  pageSize: number
  name?: string
  status?: number
}

export const CourseAPI = {
  getList: (params: CourseQuery) => {
    return request.get('/admin/course/list', params)
  },

  getAll: () => {
    return request.get('/admin/course/all')
  },

  getDetail: (id: number) => {
    return request.get(`/admin/course/${id}`)
  },

  add: (data: CourseVO) => {
    return request.post('/admin/course', data)
  },

  update: (data: CourseVO) => {
    return request.put('/admin/course', data)
  },

  delete: (id: number) => {
    return request.delete(`/admin/course/${id}`)
  },

  enroll: (id: number) => {
    return request.post(`/course/${id}/enroll`)
  },
}

export const CourseClientAPI = {
  getAll: () => {
    return request.get('/courses')
  },

  getById: (id: number) => {
    return request.get(`/courses/${id}`)
  },

  enroll: (id: number) => {
    return request.post(`/courses/${id}/enroll`)
  },
}
