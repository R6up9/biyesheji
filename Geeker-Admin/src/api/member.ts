import request from '@/utils/request'

export namespace MemberAPI {
  export interface MemberVO {
    id?: number
    userId?: number
    username?: string
    password?: string
    role?: number
    status?: number
    realName?: string
    gender?: number
    age?: number
    phone?: string
    avatar?: string
    introduction?: string
    level?: number
    points?: number
    balance?: number
    createTime?: string
    updateTime?: string
  }

  export interface MemberQuery {
    pageNum: number
    pageSize: number
    realName?: string
    gender?: number
    level?: number
    sortProp?: string
    sortOrder?: string
  }
}

export const MemberApi = {
  getList: (params: MemberAPI.MemberQuery) => {
    const query: Record<string, any> = {
      pageNum: params.pageNum,
      pageSize: params.pageSize,
    }
    if (params.realName) {
      query.realName = params.realName
    }
    if (params.gender !== undefined) {
      query.gender = params.gender
    }
    if (params.level !== undefined) {
      query.level = params.level
    }
    if (params.sortProp) {
      query.sortProp = params.sortProp
    }
    if (params.sortOrder) {
      query.sortOrder = params.sortOrder
    }
    return request.get('/admin/member/page', query)
  },

  getById: (id: number) => {
    return request.get(`/admin/member/${id}`)
  },

  add: (data: MemberAPI.MemberVO) => {
    return request.post('/admin/member', data)
  },

  update: (data: MemberAPI.MemberVO) => {
    return request.put('/admin/member', data)
  },

  delete: (id: number) => {
    return request.delete(`/admin/member/${id}`)
  },
}
