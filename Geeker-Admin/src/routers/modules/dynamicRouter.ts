import router from '@/routers/index'
import { LOGIN_URL } from '@/config'
import type { RouteRecordRaw } from 'vue-router'
import { ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import { useAuthStore } from '@/stores/modules/auth'

// 引入 views 文件夹下所有 vue 文件 - 使用绝对路径从 src 开始
const modules: Record<string, () => Promise<any>> = import.meta.glob('/src/views/**/*.vue')
console.log('Available modules:', Object.keys(modules))

/**
 * @description 初始化动态路由
 */
export const initDynamicRouter = async () => {
  const userStore = useUserStore()
  const authStore = useAuthStore()

  try {
    // 1.获取菜单列表 && 按钮权限列表
    await authStore.getAuthMenuList()
    await authStore.getAuthButtonList()

    // 2.判断当前用户有没有菜单权限
    if (!authStore.authMenuList.length) {
      ElNotification({
        title: '无权限访问',
        message: '当前账号无任何菜单权限，请联系系统管理员！',
        type: 'warning',
        duration: 3000,
      })
      userStore.setToken('')
      void router.replace(LOGIN_URL)
      return Promise.reject(new Error('No menu permission'))
    }

    // 3.添加动态路由
    const menuList = authStore.flatMenuListGet
    console.log('Adding dynamic routes:', menuList)
    
    menuList.forEach(item => {
      // 使用从 /src/views/ 开始的绝对路径
      const componentPath = '/src/views' + item.component + '.vue'
      console.log('Trying to load component:', componentPath)
      console.log('Module exists:', !!modules[componentPath])
      
      const routeItem: RouteRecordRaw = {
        path: item.path,
        name: item.name,
        component: item.component && typeof item.component === 'string' 
          ? modules[componentPath]
          : undefined,
        meta: item.meta
      }
      
      console.log('Adding route:', routeItem)
      
      if (item.meta.isFull) {
        router.addRoute(routeItem)
      } else {
        router.addRoute('layout', routeItem)
      }
    })
    
    console.log('Current routes:', router.getRoutes())
  } catch (error) {
    // 当按钮 || 菜单请求出错时，重定向到登陆页
    console.error('Error initializing dynamic router:', error)
    userStore.setToken('')
    void router.replace(LOGIN_URL)
    return Promise.reject(error)
  }
}
