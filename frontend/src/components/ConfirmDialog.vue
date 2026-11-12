<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  message: { type: String, required: true },
  confirmLabel: { type: String, default: 'Delete' },
})

const emit = defineEmits(['confirm', 'cancel'])
const dialog = ref(null)

watch(
  () => props.open,
  (open) => (open ? dialog.value.showModal() : dialog.value.close()),
)
</script>

<template>
  <!-- the native <dialog> also closes on Escape, which fires "cancel" -->
  <dialog ref="dialog" @cancel.prevent="emit('cancel')">
    <p>{{ message }}</p>
    <div class="actions">
      <button class="secondary" @click="emit('cancel')">Cancel</button>
      <button class="danger" @click="emit('confirm')">{{ confirmLabel }}</button>
    </div>
  </dialog>
</template>

<style scoped>
dialog {
  border: 1px solid var(--tea-green);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
  min-width: 300px;
}

dialog::backdrop {
  background: rgb(35 53 44 / 0.35);
}

p {
  margin-top: 0;
}
</style>
