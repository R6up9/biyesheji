import http from '@/utils/request'

export const DashboardAPI = {
  getStats: () => {
    return http.get('/dashboard/stats')
  },

  getBookingTrend: () => {
    return http.get('/dashboard/booking/trend')
  },

  getBookingByType: () => {
    return http.get('/dashboard/booking/type')
  },

  getOrderTrend: () => {
    return http.get('/dashboard/order/trend')
  }
}
