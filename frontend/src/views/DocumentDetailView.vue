<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import FilePreview from '@/components/FilePreview.vue'
import FormField from '@/components/FormField.vue'
import TagChip from '@/components/TagChip.vue'
import TagPicker from '@/components/TagPicker.vue'
import { documentsApi, tagsApi } from '@/api'
import { auth } from '@/stores/auth'
import { formatDate, formatSize } from '@/utils/format'
import { rules } from '@/utils/validation'

const props = defineProps({
  id: { type: String, required: true },
})

const router = useRouter()
const document = ref(null)
const allTags = ref([])
const title = ref('')
const titleError = ref('')
const error = ref('')
const saved = ref(false)
const confirmOpen = ref(false)

const fileUrl = computed(() => documentsApi.fileUrl(props.id))

// DocumentDTO only carries tag names, but the tag endpoints need ids
const assignedTags = computed(() => allTags.value.filter((tag) => document.value.tags.includes(tag.name)))
const availableTags = computed(() => allTags.value.filter((tag) => !document.value.tags.includes(tag.name)))

function show(doc) {
  document.value = doc
  title.value = doc.title
}

async function load() {
  try {
    const [doc, tags] = await Promise.all([documentsApi.getById(props.id), tagsApi.getAll()])
    // only the owner may see a document
    if (doc.ownerId !== auth.user.id) {
      error.value = 'Document not found'
      return
    }
    show(doc)
    allTags.value = tags
  } catch (e) {
    error.value = e.message
  }
}

async function run(action) {
  error.value = ''
  saved.value = false
  try {
    show(await action())
    return true
  } catch (e) {
    error.value = e.message
    return false
  }
}

async function save() {
  titleError.value = rules.documentTitle(title.value)
  if (titleError.value) return
  saved.value = await run(() => documentsApi.update(props.id, title.value.trim(), auth.user.id))
}

const addTag = (tagId) => run(() => documentsApi.addTag(props.id, tagId))
const removeTag = (tagId) => run(() => documentsApi.removeTag(props.id, tagId))

async function deleteDocument() {
  try {
    await documentsApi.delete(props.id)
    router.push({ name: 'dashboard' })
  } catch (e) {
    error.value = e.message
    confirmOpen.value = false
  }
}

onMounted(load)
</script>

<template>
  <p><RouterLink to="/">← Documents</RouterLink></p>
  <ErrorMessage :message="error" />

  <template v-if="document">
    <div class="row-between">
      <h1>{{ document.title }}</h1>
      <button class="danger" @click="confirmOpen = true">Delete</button>
    </div>

    <section class="card">
      <form class="inline-form" novalidate @submit.prevent="save">
        <FormField v-model="title" label="Title" :error="titleError" />
        <button type="submit" class="align-input">Save</button>
      </form>
      <p v-if="saved" class="muted">Saved</p>
      <dl class="details">
        <dt>Created</dt>
        <dd>{{ formatDate(document.createdAt) }}</dd>
        <dt>Updated</dt>
        <dd>{{ formatDate(document.updatedAt) }}</dd>
      </dl>
    </section>

    <section v-if="document.fileName" class="card">
      <div class="row-between">
        <h2>File</h2>
        <a :href="fileUrl" target="_blank" rel="noopener">Open</a>
      </div>
      <dl class="details">
        <dt>Name</dt>
        <dd>{{ document.fileName }}</dd>
        <dt>Size</dt>
        <dd>{{ formatSize(document.fileSize) }}</dd>
      </dl>
      <FilePreview :url="fileUrl" :content-type="document.contentType" :name="document.fileName" />
    </section>

    <section class="card">
      <h2>Tags</h2>
      <p v-if="!assignedTags.length" class="muted">No tags</p>
      <p v-else>
        <TagChip v-for="tag in assignedTags" :key="tag.id" :name="tag.name" removable @remove="removeTag(tag.id)" />
      </p>
      <TagPicker :tags="availableTags" @add="addTag" />
    </section>

    <ConfirmDialog
      :open="confirmOpen"
      :message="`Delete “${document.title}”?`"
      @confirm="deleteDocument"
      @cancel="confirmOpen = false"
    />
  </template>
</template>

<style scoped>
.details {
  margin-top: 1rem;
}
</style>
