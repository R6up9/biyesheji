import request from '@/utils/request'

export interface CourtVO {
  id?: number
  name: string
  location: string
  capacity: number
  status: number
  price?: number
  pricePerHour: number
  description?: string
  createTime?: string
  updateTime?: string
}

export interface CourtQuery {
  pageNum: number
  pageSize: number
  name?: string
  status?: number
}

export interface TimeSlotVO {
  startTime: string
  endTime: string
  available: boolean
  booking?: any
}

export const CourtAPI = {
  // 获取场地列表
  getList: (params: CourtQuery) => {
    return request.get('/admin/court/list', params)
  },
  
  // 获取所有可用场地
  getAll: () => {
    return request.get('/admin/court/all')
  },
  
  // 获取场地详情
  getDetail: (id: number) => {
    return request.get(`/admin/court/${id}`)
  },
  
  // 新增场地
  add: (data: CourtVO) => {
    return request.post('/admin/court', data)
  },
  
  // 更新场地
  update: (data: CourtVO) => {
    return request.put('/admin/court', data)
  },
  
  // 删除场地
  delete: (id: number) => {
    return request.delete(`/admin/court/${id}`)
  },
  
  // 获取场地可预约时段
  getTimeSlots: (id: number, date: string) => {
    return request.get(`/admin/court/${id}/time-slots`, { date })
  },
  
  // 用户端获取所有场地
  getAllCourts: () => {
    return request.get('/courts')
  },
  
  // 用户端获取场地预约情况
  getBookings: (courtId: number, date: string) => {
    return request.get(`/courts/${courtId}/bookings`, { date })
  },
  
  // 用户端预约场地
  book: (data: any) => {
    return request.post('/user/booking', data)
  }
}
