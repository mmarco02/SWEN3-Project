<script setup>
import { ref, useId } from 'vue'
import { ALLOWED_FILE_TYPES } from '@/utils/validation'

defineProps({
  label: { type: String, required: true },
  error: { type: String, default: '' },
})

const model = defineModel({ default: null })
const input = ref(null)
const id = useId()

function onChange(event) {
  model.value = event.target.files[0] ?? null
}

// a file input can't be bound with v-model, so clearing has to touch the element
function reset() {
  input.value.value = ''
  model.value = null
}

defineExpose({ reset })
</script>

<template>
  <div class="field">
    <label :for="id">{{ label }}</label>
    <input
      :id="id"
      ref="input"
      type="file"
      :accept="ALLOWED_FILE_TYPES.join(',')"
      :aria-invalid="!!error"
      :class="{ invalid: error }"
      @change="onChange"
    />
    <small v-if="error" class="field-error">{{ error }}</small>
  </div>
</template>