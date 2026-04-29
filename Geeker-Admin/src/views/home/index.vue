<template>
  <div class="home">
    <DataVisualize />
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'Home' })
import DataVisualize from '@/views/dashboard/dataVisualize/index.vue'
</script>

<style scoped lang="scss">
.home {
  width: 100%;
  height: 100%;
}
</style>
