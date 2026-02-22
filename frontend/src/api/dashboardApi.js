import api from './api';

export const fetchDashboardCounts = () => api.get('/dashboard/counts');
export const fetchAlertsByPrinter = () => api.get('/dashboard/alerts');