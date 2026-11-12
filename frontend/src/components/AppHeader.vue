<script setup>
import { useRouter } from 'vue-router'
import { auth } from '@/stores/auth'

const router = useRouter()

function logout() {
  auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header>
    <div class="container header-inner">
      <RouterLink to="/" class="brand">Paperless</RouterLink>
      <nav>
        <RouterLink to="/">Documents</RouterLink>
        <RouterLink to="/tags">Tags</RouterLink>
        <RouterLink to="/account">{{ auth.user.username }}</RouterLink>
        <button class="secondary" @click="logout">Logout</button>
      </nav>
    </div>
  </header>
</template>

<style scoped>
header {
  background: var(--muted-teal);
  border-bottom: 1px solid var(--celadon);
}

.header-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}

.brand {
  font-weight: 700;
  font-size: 1.2rem;
  text-decoration: none;
}

nav {
  display: flex;
  align-items: center;
  gap: 1rem;
}

nav a {
  text-decoration: none;
}

nav a.router-link-exact-active {
  text-decoration: underline;
}

nav button {
  border-color: var(--text);
}
</style>
