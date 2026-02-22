import api from './api';

// Toggle this to switch between test endpoints and authenticated endpoints
const USE_TEST_ENDPOINTS = true;

export const fetchPrinters = async () => {
    if (USE_TEST_ENDPOINTS) {
        // Use test endpoint (no auth required)
        try {
            const response = await fetch('http://localhost:8080/api/test/printers');
            const data = await response.json();
            return { data };
        } catch (error) {
            console.error('Failed to fetch printers:', error);
            return { data: [] };
        }
    } else {
        // Use authenticated endpoint (when it exists)
        return api.get('/api/printers');
    }
};

export const fetchPrinterState = async (id) => {
    if (USE_TEST_ENDPOINTS) {
        // Use test endpoint (no auth required)
        try {
            const response = await fetch(`http://localhost:8080/api/test/printers/${id}/fetch`);
            const data = await response.json();
            return { data };
        } catch (error) {
            console.error('Failed to fetch printer state:', error);
            return { data: null };
        }
    } else {
        // Use authenticated endpoint (when it exists)
        return api.get(`/api/printers/${id}/state`);
    }
};

export const fetchPrinterHistory = (id, params = {}) => {
    // History endpoint doesn't exist yet, return empty for now
    console.warn('History endpoint not implemented yet');
    return Promise.resolve({ data: [] });
};

export const sendPrinterCommand = (id, command) => {
    // Command endpoint doesn't exist yet
    console.warn('Command endpoint not implemented yet');
    return Promise.resolve({ data: { success: true } });
};

export const createPrinter = async (payload) => {
    if (USE_TEST_ENDPOINTS) {
        const response = await fetch('http://localhost:8080/api/test/printers', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to create printer');
        }

        const data = await response.json();
        return { data };
    }

    return api.post('/api/printers', payload);
};