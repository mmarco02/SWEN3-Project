<script setup>
defineProps({
  name: { type: String, required: true },
  removable: { type: Boolean, default: false },
})

defineEmits(['remove'])
</script>

<template>
  <span class="chip">
    {{ name }}
    <button v-if="removable" class="remove" :aria-label="`Remove ${name}`" @click="$emit('remove')">×</button>
  </span>
</template>

<style scoped>
.chip {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.1rem 0.55rem;
  margin: 0.1rem 0.25rem 0.1rem 0;
  border-radius: 999px;
  background: var(--tea-green);
  font-size: 0.85rem;
}

.remove {
  padding: 0 0.2rem;
  border: none;
  background: transparent;
  line-height: 1;
}

.remove:hover {
  background: transparent;
  color: var(--danger);
}
</style>
