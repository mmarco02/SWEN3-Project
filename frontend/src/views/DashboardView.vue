<script setup>
import { onMounted, ref, watch } from 'vue'
import DocumentTable from '@/components/DocumentTable.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import FormField from '@/components/FormField.vue'
import { documentsApi, usersApi } from '@/api'
import { auth } from '@/stores/auth'
import { rules } from '@/utils/validation'

const documents = ref([])
const filter = ref('')
const newTitle = ref('')
const titleError = ref('')
const error = ref('')
const toDelete = ref(null)

async function load() {
  try {
    documents.value = await usersApi.documents(auth.user.id, filter.value.trim())
    error.value = ''
  } catch (e) {
    error.value = e.message
  }
}

// reload while typing in the filter, but not on every keystroke
let filterTimeout
watch(filter, () => {
  clearTimeout(filterTimeout)
  filterTimeout = setTimeout(load, 300)
})

async function create() {
  titleError.value = rules.documentTitle(newTitle.value)
  if (titleError.value) return
  try {
    await documentsApi.create(newTitle.value.trim(), auth.user.id)
    newTitle.value = ''
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function confirmDelete() {
  try {
    await documentsApi.delete(toDelete.value.id)
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    toDelete.value = null
  }
}

onMounted(load)
</script>

<template>
  <h1>My documents</h1>
  <ErrorMessage :message="error" />

  <section class="card">
    <form class="inline-form" novalidate @submit.prevent="create">
      <FormField v-model="newTitle" label="New document" :error="titleError" />
      <button type="submit" class="align-input">Create</button>
    </form>
  </section>

  <section class="card">
    <FormField v-model="filter" label="Filter by title" type="search" />
    <DocumentTable :documents="documents" deletable @delete="toDelete = $event" />
  </section>

  <ConfirmDialog
    :open="!!toDelete"
    :message="`Delete “${toDelete?.title}”?`"
    @confirm="confirmDelete"
    @cancel="toDelete = null"
  />
</template>