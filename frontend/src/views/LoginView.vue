<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AuthForm from '@/components/AuthForm.vue'
import FormField from '@/components/FormField.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import { usersApi } from '@/api'
import { auth } from '@/stores/auth'
import { rules } from '@/utils/validation'

const router = useRouter()
const form = reactive({ username: '', password: '' })
const errors = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

async function submit() {
  errors.username = rules.username(form.username)
  errors.password = rules.password(form.password)
  if (errors.username || errors.password) return

  error.value = ''
  loading.value = true
  try {
    const user = await usersApi.login(form.username.trim(), form.password)
    auth.login(user)
    router.push({ name: 'dashboard' })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AuthForm title="Login">
    <ErrorMessage :message="error" />
    <form novalidate @submit.prevent="submit">
      <FormField v-model="form.username" label="Username" autocomplete="username" :error="errors.username" />
      <FormField
        v-model="form.password"
        label="Password"
        type="password"
        autocomplete="current-password"
        :error="errors.password"
      />
      <button type="submit" :disabled="loading">Login</button>
    </form>
    <template #footer>
      <RouterLink to="/register">Create an account</RouterLink>
    </template>
  </AuthForm>
</template>
