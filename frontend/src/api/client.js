export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

const FALLBACK_MESSAGES = {
  400: 'Invalid input',
  401: 'Not authorized',
  404: 'Not found',
  409: 'Already exists',
  413: 'File is too large',
  503: 'Service not available',
}

// The backend answers errors with a ProblemDetail ({ detail: "..." }); use that text if present.
async function errorMessage(response) {
  try {
    const body = await response.json()
    if (body.detail) return body.detail
  } catch {
    // no JSON body
  }
  return FALLBACK_MESSAGES[response.status] ?? 'Server error'
}

async function request(method, path, body) {
  // FormData (file uploads) is sent as multipart, the browser sets that header itself
  const isForm = body instanceof FormData
  let response
  try {
    response = await fetch(`/api${path}`, {
      method,
      headers: body && !isForm ? { 'Content-Type': 'application/json' } : {},
      body: body && !isForm ? JSON.stringify(body) : body,
    })
  } catch {
    throw new ApiError('Server not reachable', 0)
  }

  if (!response.ok) {
    throw new ApiError(await errorMessage(response), response.status)
  }
  return response.status === 204 ? null : response.json()
}

export const get = (path) => request('GET', path)
export const post = (path, body) => request('POST', path, body)
export const put = (path, body) => request('PUT', path, body)
export const del = (path) => request('DELETE', path)
