import api from './apiClient'

export async function login(credentials) {
    const { data } = await api.post('/auth/login', credentials)
    localStorage.setItem('token', data.token)
    return data
}

export async function getMe() {
    const { data } = await api.get('/users/me')
    return data
}