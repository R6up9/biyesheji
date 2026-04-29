import { defineStore } from 'pinia'
import type { UserInfo } from '@/stores/interface/store'
import piniaPersistConfig from '@/stores/helper/persist'
import { reactive, computed } from 'vue'

const STORE_NAME = 'geeker-user'

export const useUserStore = defineStore(
  STORE_NAME,
  () => {
    const state = reactive({
      accessToken: '',
      refreshToken: '',
      userInfo: { name: 'Geeker', isLoggedIn: false } as UserInfo,
      role: 0,
      username: ''
    })

    const userInfo = computed(() => state.userInfo)
    const accessToken = computed(() => state.accessToken)
    const refreshToken = computed(() => state.refreshToken)
    const role = computed(() => state.role)
    const username = computed(() => state.username)

    function setToken(token: string) {
      state.accessToken = token
      state.refreshToken = token
      state.userInfo.isLoggedIn = true
    }

    function setUserAndRole(data: any) {
      state.accessToken = data.token
      state.refreshToken = data.token
      state.userInfo.isLoggedIn = true
      state.username = data.username
      state.role = data.role
      state.userInfo.name = data.username
      // 同时把role存到userInfo对象中，这样auth store可以正确访问
      state.userInfo.role = data.role
    }

    function setRefreshToken(token: string) {
      state.refreshToken = token
    }

    function clearUserInfo() {
      state.userInfo = { name: 'Geeker', isLoggedIn: false } as UserInfo
      state.accessToken = ''
      state.refreshToken = ''
      state.role = 0
      state.username = ''
    }

    function setUserInfo(info: UserInfo) {
      state.userInfo = info
    }

    function getUserToken() {
      return state.refreshToken || state.accessToken
    }

    function initUserState() {
      // 确保userInfo存在
      if (!state.userInfo) {
        state.userInfo = { name: 'Geeker', isLoggedIn: false } as UserInfo
      }
      if (!state.accessToken && !state.refreshToken) {
        state.userInfo.isLoggedIn = false
      }
      if (state.accessToken === '' || state.refreshToken === '') {
        clearUserInfo()
      }
    }

    function logout() {
      clearUserInfo()
    }

    return {
      userInfo,
      accessToken,
      refreshToken,
      role,
      username,
      setToken,
      setUserAndRole,
      setRefreshToken,
      clearUserInfo,
      setUserInfo,
      getUserToken,
      initUserState,
      logout,
    }
  },
  {
    persist: piniaPersistConfig(STORE_NAME, ['accessToken', 'refreshToken', 'userInfo', 'role', 'username']),
  }
)
