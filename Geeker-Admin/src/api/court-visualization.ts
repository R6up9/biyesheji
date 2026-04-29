import request from '@/utils/request'

export const CourtVisualizationAPI = {
  // 获取场地预约可视化数据
  getData: (date: string) => {
    return request.get('/admin/court-visualization/data', { date })
  }
}
