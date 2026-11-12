import { reactive } from 'vue'

// The backend has no tokens: login just returns the user, which we keep in localStorage.
const STORAGE_KEY = 'paperless-user'

function loadUser() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY))
  } catch {
    return null
  }
}

export const auth = reactive({
  user: loadUser(),

  login(user) {
    this.user = user
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  },

  logout() {
    this.user = null
    localStorage.removeItem(STORAGE_KEY)
  },
})
