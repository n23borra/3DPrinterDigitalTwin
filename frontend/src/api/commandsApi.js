import api from './api';

export const getAvailableCommands = (printerId) => api.get('/api/commands/available', {
    params: { printerId },
});

export const executeCommand = (printerId, commandKey) => api.post('/api/commands/execute', {
    printerId,
    commandKey,
});