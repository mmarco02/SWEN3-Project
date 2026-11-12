<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { usersApi } from '@/api'
import { auth } from '@/stores/auth'
import { formatDate } from '@/utils/format'

const router = useRouter()
const user = ref(auth.user)
const error = ref('')
const confirmOpen = ref(false)

async function deleteAccount() {
  try {
    await usersApi.delete(auth.user.id)
    auth.logout()
    router.push({ name: 'login' })
  } catch (e) {
    error.value = e.message
    confirmOpen.value = false
  }
}

onMounted(async () => {
  try {
    user.value = await usersApi.getById(auth.user.id)
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <h1>Account</h1>
  <ErrorMessage :message="error" />

  <section class="card">
    <dl class="details">
      <dt>Username</dt>
      <dd>{{ user.username }}</dd>
      <dt>Member since</dt>
      <dd>{{ formatDate(user.createdAt) }}</dd>
    </dl>
  </section>

  <section class="card">
    <button class="danger" @click="confirmOpen = true">Delete account</button>
  </section>

  <ConfirmDialog
    :open="confirmOpen"
    message="Delete your account and all your documents?"
    @confirm="deleteAccount"
    @cancel="confirmOpen = false"
  />
</template>