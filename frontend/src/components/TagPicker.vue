<script setup>
import { ref } from 'vue'

defineProps({
  // tags that can still be added
  tags: { type: Array, required: true },
})

const emit = defineEmits(['add'])
const selected = ref('')

function add() {
  if (!selected.value) return
  emit('add', selected.value)
  selected.value = ''
}
</script>

<template>
  <div class="inline-form">
    <select v-model="selected" aria-label="Tag" :disabled="!tags.length">
      <option value="" disabled>{{ tags.length ? 'Select tag' : 'No tags available' }}</option>
      <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
    </select>
    <button :disabled="!selected" @click="add">Add tag</button>
  </div>
</template>
