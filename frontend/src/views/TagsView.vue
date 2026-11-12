<script setup>
import { onMounted, ref } from 'vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import DocumentTable from '@/components/DocumentTable.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import FormField from '@/components/FormField.vue'
import { tagsApi } from '@/api'
import { auth } from '@/stores/auth'
import { rules } from '@/utils/validation'

const tags = ref([])
const newName = ref('')
const nameError = ref('')
const error = ref('')
const selected = ref(null)
const selectedDocuments = ref([])
const toDelete = ref(null)

async function load() {
  try {
    tags.value = await tagsApi.getAll()
  } catch (e) {
    error.value = e.message
  }
}

async function create() {
  nameError.value = rules.tagName(newName.value)
  if (nameError.value) return
  error.value = ''
  try {
    await tagsApi.create(newName.value.trim())
    newName.value = ''
    await load()
  } catch (e) {
    error.value = e.message
  }
}

async function select(tag) {
  error.value = ''
  try {
    const documents = await tagsApi.documents(tag.id)
    // the endpoint returns documents of all users, only show our own
    selectedDocuments.value = documents.filter((doc) => doc.ownerId === auth.user.id)
    selected.value = tag
  } catch (e) {
    error.value = e.message
  }
}

async function confirmDelete() {
  try {
    await tagsApi.delete(toDelete.value.id)
    if (selected.value?.id === toDelete.value.id) selected.value = null
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
  <h1>Tags</h1>
  <ErrorMessage :message="error" />

  <section class="card">
    <form class="inline-form" novalidate @submit.prevent="create">
      <FormField v-model="newName" label="New tag" :error="nameError" />
      <button type="submit" class="align-input">Create</button>
    </form>
  </section>

  <section class="card">
    <p v-if="!tags.length" class="muted">No tags</p>
    <table v-else>
      <tbody>
        <tr v-for="tag in tags" :key="tag.id" :class="{ selected: selected?.id === tag.id }">
          <td>
            <button class="link" @click="select(tag)">{{ tag.name }}</button>
          </td>
          <td class="actions">
            <button class="danger" @click="toDelete = tag">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>

  <section v-if="selected" class="card">
    <h2>Documents tagged “{{ selected.name }}”</h2>
    <DocumentTable :documents="selectedDocuments" />
  </section>

  <ConfirmDialog
    :open="!!toDelete"
    :message="`Delete tag “${toDelete?.name}”?`"
    @confirm="confirmDelete"
    @cancel="toDelete = null"
  />
</template>

<style scoped>
.link {
  padding: 0;
  border: none;
  background: transparent;
  text-decoration: underline;
}

.link:hover {
  background: transparent;
}

tr.selected td {
  background: var(--beige);
}
</style>
