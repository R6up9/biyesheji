import { defineStore } from 'pinia'
import type { Dict } from '@/api/system/dict'
import { useStorage } from '@vueuse/core'
import { DictAPI } from '@/api/system/dict'
import { DICT_CACHE_TIME } from '@/constants'

export const useDictStore = defineStore('geeker-dict', () => {
  const dict = useStorage<Record<string, { list: Dict[]; __cache_time: number }>>('geeker-dict', {}, localStorage, {
    mergeDefaults: true,
  })

  const getDict = (code: string) => {
    const cached = dict.value[code]
    if (cached && cached.__cache_time && Date.now() - cached.__cache_time < DICT_CACHE_TIME) {
      return Promise.resolve(cached.list)
    }
    return DictAPI.getDictData(code).then(data => {
      dict.value[code] = { list: data, __cache_time: Date.now() }
      return data
    })
  }

  const getAllDict = () => {
    // 暂时不调用后端 API，后端还未实现
    return Promise.resolve([])
  }

  const clearDict = () => {
    dict.value = {}
  }

  return { getDict, clearDict, getAllDict }
})
