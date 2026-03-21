<script setup lang="ts">
import { RouterView } from 'vue-router'
import TheNavbar from '@/components/layout/TheNavbar.vue'
</script>

<template>
  <div class="relative min-h-screen overflow-hidden">
    <div class="pointer-events-none absolute inset-x-0 top-0 h-72 bg-[radial-gradient(circle_at_top,rgba(37,99,235,0.22),transparent_58%)]" />
    <div class="pointer-events-none absolute inset-y-0 right-[-12rem] w-[32rem] bg-[radial-gradient(circle,rgba(14,165,233,0.12),transparent_62%)] blur-3xl" />

    <div class="relative flex min-h-screen flex-col">
      <TheNavbar />
      <main class="mx-auto flex w-full max-w-7xl flex-1 flex-col px-4 pb-10 pt-6 sm:px-6 lg:px-8 lg:pt-8">
        <RouterView />
      </main>
    </div>
  </div>
</template>
