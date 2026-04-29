<template>
  <div class="echarts">
    <e-charts :option="option" />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Pie' })
import type { ECOption } from '@/components/ECharts/config'
import ECharts from '@/components/ECharts/index.vue'
import { watch, ref, computed } from 'vue'

const props = defineProps<{
  data?: any[]
}>()

const pieData = computed(() => props.data || [
  { value: 0, name: '场地预订' },
  { value: 0, name: '课程报名' },
  { value: 0, name: '活动报名' }
])

const option = computed<ECOption>(() => ({
  title: {
    text: '预订类型',
    subtext: '占比统计',
    left: '56%',
    top: '45%',
    textAlign: 'center',
    textStyle: {
      fontSize: 18,
      color: '#767676',
    },
    subtextStyle: {
      fontSize: 15,
      color: '#a1a1a1',
    },
  },
  tooltip: {
    trigger: 'item',
  },
  legend: {
    top: '4%',
    left: '2%',
    orient: 'vertical',
    icon: 'circle',
    align: 'left',
    itemGap: 20,
    textStyle: {
      fontSize: 13,
      color: '#a1a1a1',
      fontWeight: 500,
    },
    formatter: function (name: string) {
      let dataCopy = ''
      for (let i = 0; i < pieData.value.length; i++) {
        if (pieData.value[i].name == name && pieData.value[i].value >= 10000) {
          dataCopy = (pieData.value[i].value / 10000).toFixed(2)
          return name + '      ' + dataCopy + 'w'
        } else if (pieData.value[i].name == name) {
          dataCopy = pieData.value[i].value + ''
          return name + '      ' + dataCopy
        }
      }
      return ''
    },
  },
  series: [
    {
      type: 'pie',
      radius: ['70%', '40%'],
      center: ['57%', '52%'],
      silent: true,
      clockwise: true,
      startAngle: 150,
      data: pieData.value,
      labelLine: {
        length: 80,
        length2: 30,
        lineStyle: {
          width: 1,
        },
      },
      label: {
        position: 'outside',
        show: true,
        formatter: '{d}%',
        fontWeight: 400,
        fontSize: 19,
        color: '#a1a1a1',
      },
      color: [
        {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0.5,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: '#feb791',
            },
            {
              offset: 1,
              color: '#fe8b4c',
            },
          ],
        },
        {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 1,
          y2: 0.5,
          colorStops: [
            {
              offset: 0,
              color: '#b898fd',
            },
            {
              offset: 1,
              color: '#8347fd',
            },
          ],
        },
        {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0.5,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: '#98f5d8',
            },
            {
              offset: 1,
              color: '#47fdb5',
            },
          ],
        },
      ],
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
