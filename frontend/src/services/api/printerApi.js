import api from './apiClient'

export async function listPrinters() {
    const { data } = await api.get('/printers')
    return data
}

export async function fetchHistory(id) {
    const { data } = await api.get(`/printers/${id}/history`)
    return data
}

export async function sendCommand(id, command) {
    return api.post(`/printers/${id}/command`, { command })
}