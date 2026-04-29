import request from '@/utils/request'

export interface OrderVO {
  id?: number
  orderNo?: string
  userId?: number
  userName?: string
  productId?: number
  productName?: string
  productImage?: string
  quantity?: number
  price?: number
  totalAmount?: number
  status?: number
  address?: string
  phone?: string
  receiver?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface OrderQuery {
  pageNum: number
  pageSize: number
  status?: number
  orderNo?: string
}

export const OrderAPI = {
  // 客户端API
  createOrder: (data: { productId: number; quantity: number; address?: string; phone?: string; receiver?: string; remark?: string }) => {
    return request.post('/client/order', data)
  },

  getMyOrders: (params: OrderQuery) => {
    return request.get('/client/order/list', params)
  },

  cancelOrder: (orderId: number) => {
    return request.put(`/client/order/${orderId}/cancel`)
  },

  completeOrder: (orderId: number) => {
    return request.put(`/client/order/${orderId}/complete`)
  },

  // 管理端API
  getList: (params: OrderQuery) => {
    return request.get('/admin/order/list', params)
  },

  getDetail: (orderId: number) => {
    return request.get(`/admin/order/${orderId}`)
  },

  updateStatus: (orderId: number, status: number) => {
    return request.put(`/admin/order/${orderId}/status`, { status })
  }
}
