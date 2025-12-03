import react from '@vitejs/plugin-react';
import {defineConfig} from "vite";

export default defineConfig({
    plugins: [react({
        jsxRuntime: 'automatic',
    })],

    server: {
        proxy: {
            '/api': 'http://localhost:8080'
        }
    },

    test: {
        environment: 'jsdom',
        setupFiles: './src/test/setup.js'
    }
});

