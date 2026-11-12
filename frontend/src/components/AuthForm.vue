<script setup>
// Shared card layout for the login and register screens.
defineProps({
  title: { type: String, required: true },
})
</script>

<template>
  <div class="auth">
    <h1>Paperless</h1>
    <div class="card">
      <h2>{{ title }}</h2>
      <slot />
    </div>
    <p class="footer"><slot name="footer" /></p>
  </div>
</template>

<style scoped>
.auth {
  max-width: 360px;
  margin: 3rem auto 0;
}

h1 {
  text-align: center;
}

.footer {
  text-align: center;
}

:slotted(button[type='submit']) {
  width: 100%;
}
</style>
