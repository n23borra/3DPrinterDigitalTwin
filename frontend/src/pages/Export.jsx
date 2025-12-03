import React from 'react';
import Button from '../components/Button';
import api from '../api/api';

/**
 * Presents export options that trigger file downloads for the currently selected format.
 * @returns {JSX.Element} Page with buttons to initiate PDF, CSV or JSON exports.
 */
export default function Export() {
    /**
     * Opens the backend export endpoint in a new window to start a download.
     * @param {'pdf'|'csv'|'json'} format - Requested export format.
     * @returns {void}
     */
    const handleExport = (format) => {
        window.open(`${api.defaults.baseURL}/export/${format}`);
    };

    return (
        <div>
            <h2 className="text-2xl font-semibold mb-4">Export</h2>
            <div className="space-x-2">
                <Button onClick={() => handleExport('pdf')}>PDF</Button>
                <Button onClick={() => handleExport('csv')}>CSV</Button>
                <Button onClick={() => handleExport('json')}>JSON</Button>
            </div>
        </div>
    );
}