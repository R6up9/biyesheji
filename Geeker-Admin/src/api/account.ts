import request from '@/utils/request'

export interface AccountVO {
  id?: number
  username: string
  password?: string
  role?: number
  status?: number
  createTime?: string
  updateTime?: string
}

export interface AccountQuery {
  pageNum: number
  pageSize: number
  username?: string
}

export const AccountApi = {
  getList: (params: AccountQuery) => {
    const query: Record<string, any> = {
      pageNum: params.pageNum,
      pageSize: params.pageSize,
    }
    if (params.username) {
      query.username = params.username
    }
    return request.get('/admin/account/page', query)
  },

  getById: (id: number) => {
    return request.get(`/admin/account/${id}`)
  },

  add: (data: AccountVO) => {
    return request.post('/admin/account', data)
  },

  update: (data: AccountVO) => {
    return request.put('/admin/account', data)
  },

  delete: (id: number) => {
    return request.delete(`/admin/account/${id}`)
  },
}
