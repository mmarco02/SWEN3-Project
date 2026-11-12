// Same limits as the backend DTOs (CredentialsDTO, DocumentRequestDTO, TagRequestDTO).
export function validateLength(value, { min = 1, max }) {
  const length = value.trim().length
  if (length === 0) return 'Required'
  if (length < min) return `At least ${min} characters`
  if (length > max) return `At most ${max} characters`
  return ''
}

export const rules = {
  username: (value) => validateLength(value, { min: 3, max: 50 }),
  password: (value) => validateLength(value, { min: 8, max: 72 }),
  documentTitle: (value) => validateLength(value, { max: 255 }),
  tagName: (value) => validateLength(value, { max: 50 }),
}
