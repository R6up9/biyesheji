<template>
  <div class="echarts">
    <e-charts :option="option" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Curve' })
import type { ECOption } from '@/components/ECharts/config'
import ECharts from '@/components/ECharts/index.vue'
import { computed } from 'vue'

const props = defineProps<{
  data?: any[]
}>()

const curveData = computed(() => props.data || [])

const option = computed<ECOption>(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'transparent',
    axisPointer: {
      type: 'none',
    },
    padding: 0,
    formatter: (p: any) => {
      let dom = `<div style="width:100%; height: 70px !important; display:flex;flex-direction: column;justify-content: space-between;padding:10px;box-sizing: border-box;
      color:#fff; background: #6B9DFE;border-radius: 4px;font-size:14px; ">
        <div style="display: flex; align-items: center;"> <div style="width:5px;height:5px;background:#ffffff;border-radius: 50%;margin-right:5px"></div>日期 :  ${p[0].name}</div>
        <div style="display: flex;align-items: center;"><div style="width:5px;height:5px;background:#ffffff;border-radius: 50%;margin-right:5px"></div>预订量 :  ${p[0].value}</div>
      </div>`
      return dom
    },
  },
  toolbox: {
    show: true,
    orient: 'horizontal',
  },
  grid: {
    left: '0',
    right: '0',
  },
  dataZoom: [
    {
      show: false,
      height: 10,
      xAxisIndex: [0],
      bottom: 0,
      startValue: 0,
      endValue: 9,
      handleStyle: {
        color: '#6b9dfe',
      },
      textStyle: {
        color: 'transparent',
      },
    },
    {
      type: 'inside',
      show: true,
      height: 0,
      zoomLock: true,
    },
  ],
  xAxis: [
    {
      type: 'category',
      data: curveData.value.map((val: any) => {
        return {
          value: val.date || val.spotName || '',
        }
      }),
      axisTick: {
        show: false,
      },
      axisLabel: {
        margin: 20,
        interval: 0,
        color: '#a1a1a1',
        fontSize: 14,
        formatter: function (name: string) {
          return name.length > 8 ? name.slice(0, 8) + '...' : name
        },
      },
      axisLine: {
        lineStyle: {
          color: '#c0c0c0',
        },
      },
    },
  ],
  yAxis: [
    {
      min: 0,
      axisLine: {
        show: false,
      },
      axisTick: {
        show: false,
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#c0c0c0',
        },
      },
      axisLabel: {
        color: '#a1a1a1',
        fontSize: 16,
        fontWeight: 400,
        formatter: function (value: number) {
          if (value === 0) {
            return value + ''
          } else if (value >= 10000) {
            return value / 10000 + 'w'
          }
          return value + ''
        },
      },
    },
  ],
  series: [
    {
      name: '预订量',
      type: 'bar',
      data: curveData.value.map((val: any) => {
        return {
          value: val.count || val.value || 0,
        }
      }),
      barWidth: '45px',
      itemStyle: {
        color: '#C5D8FF',
        borderRadius: [12, 12, 0, 0],
      },
      emphasis: {
        itemStyle: {
          color: '#6B9DFE',
        },
      },
    },
  ],
}))
</script>

<style lang="scss" scoped>
.echarts {
  width: 100%;
  height: 100%;
}
</style>
