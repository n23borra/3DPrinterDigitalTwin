import axios from 'axios';

/**
 * Preconfigured Axios instance targeting the backend API and adding auth headers when available.
 * @type {import('axios').AxiosInstance}
 */
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
});

api.interceptors.request.use((config) => {
    /**
     * Injects the persisted JWT into outgoing requests for authenticated endpoints.
     * @param {import('axios').InternalAxiosRequestConfig} config - Axios request configuration.
     * @returns {import('axios').InternalAxiosRequestConfig} Updated configuration including the Authorization header.
     */
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;