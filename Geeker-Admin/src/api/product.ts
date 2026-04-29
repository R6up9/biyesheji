import request from '@/utils/request'

export interface ProductVO {
  id?: number
  name: string
  type: number
  price: number
  stock: number
  image?: string
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface ProductQuery {
  pageNum: number
  pageSize: number
  name?: string
  status?: number
  type?: number
}

export const ProductAPI = {
  getList: (params: ProductQuery) => {
    return request.get('/admin/product/list', params)
  },
  
  getAll: () => {
    return request.get('/admin/product/all')
  },
  
  getDetail: (id: number) => {
    return request.get(`/admin/product/${id}`)
  },
  
  add: (data: ProductVO) => {
    return request.post('/admin/product', data)
  },
  
  update: (data: ProductVO) => {
    return request.put('/admin/product', data)
  },
  
  delete: (id: number) => {
    return request.delete(`/admin/product/${id}`)
  }
}
