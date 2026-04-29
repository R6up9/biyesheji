import request from '@/utils/request'

export interface BookingVO {
  id?: number
  type?: number
  status?: number
  bookingTime?: string
  createTime?: string
  updateTime?: string
}

export interface BookingQuery {
  pageNum: number
  pageSize: number
  type?: number
  status?: number
}

export const BookingAPI = {
  // 获取预约列表
  getList: (params: BookingQuery) => {
    return request.get('/admin/booking/list', params)
  },
  
  // 获取我的预约（用户端）
  getMyList: (params?: BookingQuery) => {
    return request.get('/user/booking/my', params)
  },
  
  // 获取预约详情
  getDetail: (id: number) => {
    return request.get(`/admin/booking/${id}`)
  },
  
  // 新增预约
  add: (data: BookingVO) => {
    return request.post('/admin/booking', data)
  },
  
  // 更新预约
  update: (data: BookingVO) => {
    return request.put('/admin/booking', data)
  },
  
  // 删除预约
  delete: (id: number) => {
    return request.delete(`/admin/booking/${id}`)
  },
  
  // 取消预约
  cancel: (id: number) => {
    return request.put(`/user/booking/${id}/cancel`)
  }
}
