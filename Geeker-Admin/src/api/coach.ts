import request from '@/utils/request'

export namespace CoachAPI {
  export interface CoachVO {
    id?: number
    realName: string
    gender?: number
    age?: number
    phone?: string
    avatar?: string
    introduction?: string
    skills?: string
    hourlyRate?: number
    status?: number
    createTime?: string
    updateTime?: string
  }

  export interface CoachQuery {
    pageNum: number
    pageSize: number
    realName?: string
    status?: number
  }
}

export const CoachApi = {
  getList: (params: CoachAPI.CoachQuery) => {
    const query: Record<string, any> = {
      pageNum: params.pageNum,
      pageSize: params.pageSize,
    }
    if (params.realName) {
      query.realName = params.realName
    }
    if (params.status !== undefined && params.status !== null) {
      query.status = params.status
    }
    return request.get('/admin/coach/page', query)
  },

  getAll: () => {
    return request.get('/admin/coach/page', {
      pageNum: 1,
      pageSize: 100,
    })
  },

  getById: (id: number) => {
    return request.get(`/admin/coach/${id}`)
  },

  add: (data: CoachAPI.CoachVO) => {
    return request.post('/admin/coach', data)
  },

  update: (data: CoachAPI.CoachVO) => {
    return request.put('/admin/coach', data)
  },

  delete: (id: number) => {
    return request.delete(`/admin/coach/${id}`)
  },

  getAllClient: () => {
    return request.get('/coaches')
  },

  getByIdClient: (id: number) => {
    return request.get(`/coaches/${id}`)
  },
}

export const CoachClientAPI = {
  getAll: () => {
    return request.get('/coaches')
  },

  getById: (id: number) => {
    return request.get(`/coaches/${id}`)
  },

  book: (id: number) => {
    return request.post(`/coaches/${id}/book`)
  },
}
