<script setup>
import { useId } from 'vue'

defineProps({
  label: { type: String, required: true },
  type: { type: String, default: 'text' },
  error: { type: String, default: '' },
  autocomplete: { type: String, default: 'off' },
})

const model = defineModel({ type: String, default: '' })
const id = useId()
</script>

<template>
  <div class="field">
    <label :for="id">{{ label }}</label>
    <input
      :id="id"
      v-model="model"
      :type="type"
      :autocomplete="autocomplete"
      :aria-invalid="!!error"
      :class="{ invalid: error }"
    />
    <small v-if="error" class="field-error">{{ error }}</small>
  </div>
</template>