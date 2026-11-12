<script setup>
import { onMounted, ref, watch } from 'vue'
import DocumentTable from '@/components/DocumentTable.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import FileField from '@/components/FileField.vue'
import FormField from '@/components/FormField.vue'
import { documentsApi, usersApi } from '@/api'
import { auth } from '@/stores/auth'
import { rules, validateFile } from '@/utils/validation'

const documents = ref([])
const filter = ref('')
const newTitle = ref('')
const titleError = ref('')
const newFile = ref(null)
const fileError = ref('')
const fileField = ref(null)
const uploading = ref(false)
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

// use the file name as title if none was typed yet
watch(newFile, (file) => {
  if (file && !newTitle.value.trim()) newTitle.value = file.name.replace(/\.[^.]+$/, '')
})

async function upload() {
  titleError.value = rules.documentTitle(newTitle.value)
  fileError.value = validateFile(newFile.value)
  if (titleError.value || fileError.value) return

  uploading.value = true
  try {
    await documentsApi.create(newTitle.value.trim(), auth.user.id, newFile.value)
    newTitle.value = ''
    fileField.value.reset()
    error.value = ''
    await load()
  } catch (e) {
    error.value = e.message
  } finally {
    uploading.value = false
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
    <h2>Upload document</h2>
    <form novalidate @submit.prevent="upload">
      <FileField ref="fileField" v-model="newFile" label="File (PDF or image)" :error="fileError" />
      <FormField v-model="newTitle" label="Title" :error="titleError" />
      <button type="submit" :disabled="uploading">{{ uploading ? 'Uploading…' : 'Upload' }}</button>
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