<script setup>
defineProps({
  message: { type: String, default: '' },
})
</script>

<template>
  <p v-if="message" class="error-message" role="alert">{{ message }}</p>
</template>

<style scoped>
.error-message {
  margin: 0 0 1rem;
  padding: 0.6rem 0.8rem;
  border: 1px solid var(--danger);
  border-radius: var(--radius);
  background: #fbeaea;
  color: var(--danger);
}
</style>
