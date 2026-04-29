import http from '@/utils/request'

export interface ReqLoginForm {
  username: string
  password: string
}

export interface ReqRegisterForm {
  username: string
  password: string
  confirmPassword?: string
  realName?: string
  gender?: number
  age?: number
  phone?: string
}

export interface ResLogin {
  token: string
  userId: number
  username: string
  role: number
}

export interface UserVO {
  id?: number
  username?: string
  role?: number
  status?: number
}

export interface MemberVO {
  id?: number
  userId?: number
  realName?: string
  gender?: number
  age?: number
  phone?: string
  avatar?: string
  introduction?: string
  level?: number
  points?: number
  balance?: number
}

export const AuthApi = {
  login: (params: ReqLoginForm) => {
    return http.post<ResLogin>(`/auth/login`, params, { loading: false })
  },

  register: (params: ReqRegisterForm) => {
    return http.post(`/auth/register`, params)
  },

  logout: () => {
    return http.post(`/auth/logout`)
  },
  
  getCurrentUser: () => {
    return http.get<UserVO>(`/auth/currentUser`)
  },
  
  getMemberInfo: () => {
    return http.get<MemberVO>(`/auth/memberInfo`)
  },
  
  updateMember: (data: MemberVO) => {
    return http.put(`/auth/memberInfo`, data)
  },
  
  updateAvatar: (avatar: string) => {
    return http.put(`/auth/updateAvatar`, { avatar })
  }
}

export const AuthAPI = AuthApi

