import { get, post, put, del } from './client'

const query = (params) => {
  const search = new URLSearchParams(Object.entries(params).filter(([, value]) => value))
  return search.size ? `?${search}` : ''
}

export const usersApi = {
  register: (username, password) => post('/users/register', { username, password }),
  login: (username, password) => post('/users/login', { username, password }),
  getById: (id) => get(`/users/${id}`),
  documents: (id, title) => get(`/users/${id}/documents${query({ title })}`),
  delete: (id) => del(`/users/${id}`),
}

export const documentsApi = {
  getById: (id) => get(`/documents/${id}`),
  create: (title, ownerId, file) => {
    const form = new FormData()
    form.append('title', title)
    form.append('ownerId', ownerId)
    form.append('file', file)
    return post('/documents', form)
  },
  // used directly as href/src, the browser loads the file itself
  fileUrl: (id) => `/api/documents/${id}/file`,
  update: (id, title, ownerId) => put(`/documents/${id}`, { title, ownerId }),
  delete: (id) => del(`/documents/${id}`),
  addTag: (id, tagId) => post(`/documents/${id}/tags/${tagId}`),
  removeTag: (id, tagId) => del(`/documents/${id}/tags/${tagId}`),
}

export const tagsApi = {
  getAll: () => get('/tags'),
  create: (name) => post('/tags', { name }),
  delete: (id) => del(`/tags/${id}`),
  documents: (id) => get(`/tags/${id}/documents`),
}
