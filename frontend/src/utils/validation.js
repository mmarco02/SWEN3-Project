// Same limits as the backend DTOs (CredentialsDTO, DocumentRequestDTO, TagRequestDTO).
export function validateLength(value, { min = 1, max }) {
  const length = value.trim().length
  if (length === 0) return 'Required'
  if (length < min) return `At least ${min} characters`
  if (length > max) return `At most ${max} characters`
  return ''
}

// Same as DocumentService.ALLOWED_CONTENT_TYPES and the multipart limit in application.properties.
export const ALLOWED_FILE_TYPES = ['application/pdf', 'image/png', 'image/jpeg', 'image/gif', 'image/webp']
export const MAX_FILE_SIZE = 20 * 1024 * 1024

export function validateFile(file) {
  if (!file) return 'Required'
  if (!ALLOWED_FILE_TYPES.includes(file.type)) return 'Only PDF or image files'
  if (file.size === 0) return 'File is empty'
  if (file.size > MAX_FILE_SIZE) return 'At most 20 MB'
  return ''
}

export const rules = {
  username: (value) => validateLength(value, { min: 3, max: 50 }),
  password: (value) => validateLength(value, { min: 8, max: 72 }),
  documentTitle: (value) => validateLength(value, { max: 255 }),
  tagName: (value) => validateLength(value, { max: 50 }),
}
