import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';
// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    server: {
        port: 5173,
        host: 'localhost',
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:8080',
                changeOrigin: true,
                secure: false,
            },
        },
    },
    build: {
        chunkSizeWarningLimit: 1000,
        rollupOptions: {
            output: {
                manualChunks: {
                    'vendor-three': ['three', '@react-three/fiber', '@react-three/drei'],
                    'vendor-react': ['react', 'react-dom', 'react-router-dom'],
                    'vendor-ui': ['lucide-react', 'date-fns', 'clsx', 'tailwind-merge', 'qrcode.react'],
                },
            },
        },
    },
});
