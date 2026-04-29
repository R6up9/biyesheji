import { defineStore } from 'pinia'
import type { AuthState } from '@/stores/interface/store'
import { getFlatMenuList, getShowMenuList, getAllBreadcrumbList } from '@/utils'
import { computed, reactive, toRefs } from 'vue'
import type { MenuOptions } from '@/api/system/menu'
import { useUserStore } from '@/stores/modules/user'

// 管理端菜单数据
const adminMenuList: MenuOptions[] = [
  {
    path: '/home',
    name: 'Home',
    component: '/home/index',
    meta: {
      icon: 'HomeFilled',
      title: '首页',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: true,
      isKeepAlive: true,
    },
  },
  {
    path: '/member',
    name: 'Member',
    component: '/member/list/index',
    meta: {
      icon: 'User',
      title: '会员管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/coach',
    name: 'Coach',
    component: '/coach/list/index',
    meta: {
      icon: 'UserFilled',
      title: '教练管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/court',
    name: 'Court',
    component: '/court/list/index',
    meta: {
      icon: 'Location',
      title: '场地管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/course',
    name: 'Course',
    component: '/course/list/index',
    meta: {
      icon: 'Reading',
      title: '课程管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/activity',
    name: 'Activity',
    component: '/activity/list/index',
    meta: {
      icon: 'Calendar',
      title: '活动管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/product',
    name: 'Product',
    component: '/product/list/index',
    meta: {
      icon: 'Goods',
      title: '商品管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/booking',
    name: 'Booking',
    component: '/booking/list/index',
    meta: {
      icon: 'Calendar',
      title: '预约管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/account',
    name: 'Account',
    component: '/account/list/index',
    meta: {
      icon: 'Setting',
      title: '账号管理',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
]

// 用户端菜单数据
const clientMenuList: MenuOptions[] = [
  {
    path: '/client/home',
    name: 'ClientHome',
    component: '/client/home/index',
    meta: {
      icon: 'HomeFilled',
      title: '首页',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: true,
      isKeepAlive: true,
    },
  },
  {
    path: '/client/court',
    name: 'ClientCourt',
    component: '/client/court/index',
    meta: {
      icon: 'Location',
      title: '场地预约',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/client/course',
    name: 'ClientCourse',
    component: '/client/course/index',
    meta: {
      icon: 'Reading',
      title: '课程报名',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/client/coach',
    name: 'ClientCoach',
    component: '/client/coach/index',
    meta: {
      icon: 'UserFilled',
      title: '教练信息',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/client/activity',
    name: 'ClientActivity',
    component: '/client/activity/index',
    meta: {
      icon: 'Calendar',
      title: '活动报名',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/client/product',
    name: 'ClientProduct',
    component: '/client/product/index',
    meta: {
      icon: 'Goods',
      title: '商品查看',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/client/booking',
    name: 'ClientBooking',
    component: '/client/booking/index',
    meta: {
      icon: 'Calendar',
      title: '我的预约',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
  {
    path: '/client/profile',
    name: 'ClientProfile',
    component: '/client/profile/index',
    meta: {
      icon: 'User',
      title: '个人中心',
      isLink: '',
      isHide: false,
      isFull: false,
      isAffix: false,
      isKeepAlive: true,
    },
  },
]

export const useAuthStore = defineStore('geeker-auth', () => {
  const state = reactive<AuthState>({
    allAuthButtonList: {},
    authMenuList: [],
    routeName: '',
  })

  const authButtonList = computed(() => {
    return state.allAuthButtonList[state.routeName as string] || []
  })

  const showMenuListGet = computed(() => getShowMenuList(state.authMenuList))
  const flatMenuListGet = computed(() => getFlatMenuList(state.authMenuList))
  const breadcrumbListGet = computed(() => getAllBreadcrumbList(state.authMenuList))
  const authMenuListGet = computed(() => state.authMenuList)

  const actions = {
    async getAuthButtonList() {
      try {
        state.allAuthButtonList = {}
      } catch {
        state.allAuthButtonList = {}
      }
    },
    async getAuthMenuList() {
      try {
        const userStore = useUserStore()
        console.log('User info in getAuthMenuList:', userStore.userInfo)
        console.log('User role:', userStore.userInfo?.role)
        // 根据用户角色显示不同菜单
        if (userStore.userInfo?.role === 0) {
          console.log('Loading admin menu list')
          state.authMenuList = adminMenuList
        } else {
          console.log('Loading client menu list')
          state.authMenuList = clientMenuList
        }
        console.log('Current authMenuList:', state.authMenuList)
      } catch (error) {
        console.error('Error in getAuthMenuList:', error)
        const userStore = useUserStore()
        if (userStore.userInfo?.role === 0) {
          state.authMenuList = adminMenuList
        } else {
          state.authMenuList = clientMenuList
        }
      }
    },
    async setRouteName(name: string) {
      state.routeName = name
    },
  }

  return {
    ...toRefs(state),
    authButtonList,
    showMenuListGet,
    flatMenuListGet,
    breadcrumbListGet,
    authMenuListGet,
    ...actions,
  }
})
