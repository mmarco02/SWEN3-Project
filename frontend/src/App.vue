<script setup>
import AppHeader from '@/components/AppHeader.vue'
import { auth } from '@/stores/auth'
</script>

<template>
  <AppHeader v-if="auth.user" />
  <main class="container">
    <RouterView />
  </main>
</template>
