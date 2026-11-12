<script setup>
defineProps({
  url: { type: String, required: true },
  contentType: { type: String, required: true },
  name: { type: String, default: '' },
})
</script>

<template>
  <img v-if="contentType.startsWith('image/')" :src="url" :alt="name" class="preview" />
  <iframe v-else-if="contentType === 'application/pdf'" :src="url" :title="name" class="preview pdf"></iframe>
</template>

<style scoped>
.preview {
  display: block;
  max-width: 100%;
  margin-top: 1rem;
  border: 1px solid var(--tea-green);
  border-radius: var(--radius);
}

.pdf {
  width: 100%;
  height: 70vh;
}
</style>
