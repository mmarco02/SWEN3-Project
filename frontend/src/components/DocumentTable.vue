<script setup>
import TagChip from './TagChip.vue'
import { formatDate } from '@/utils/format'

defineProps({
  documents: { type: Array, required: true },
  deletable: { type: Boolean, default: false },
})

defineEmits(['delete'])
</script>

<template>
  <p v-if="!documents.length" class="muted">No documents</p>
  <table v-else>
    <thead>
      <tr>
        <th>Title</th>
        <th>Tags</th>
        <th>Created</th>
        <th>Updated</th>
        <th v-if="deletable"></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="doc in documents" :key="doc.id">
        <td>
          <RouterLink :to="{ name: 'document', params: { id: doc.id } }">{{ doc.title }}</RouterLink>
        </td>        <td>
          <TagChip v-for="tag in doc.tags" :key="tag" :name="tag" />
        </td>
        <td>{{ formatDate(doc.createdAt) }}</td>
        <td>{{ formatDate(doc.updatedAt) }}</td>
        <td v-if="deletable" class="actions">
          <button class="danger" @click="$emit('delete', doc)">Delete</button>
        </td>
      </tr>
    </tbody>
  </table>
</template>
