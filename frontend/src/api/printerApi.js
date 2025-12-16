import api from './api';

export const fetchPrinters = () => api.get('/printers');

export const fetchPrinterState = (id) => api.get(`/printers/${id}/state`);

export const fetchPrinterHistory = (id, params = {}) => api.get(`/printers/${id}/history`, {params});

export const sendPrinterCommand = (id, command) => api.post(`/printers/${id}/command`, {command});