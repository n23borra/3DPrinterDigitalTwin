import api from './api';

export const fetchDashboardCounts = () => api.get('/api/dashboard/counts');