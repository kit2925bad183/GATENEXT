import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './app/App';
import './styles/index.css';

// Register service worker for PWA static asset caching
if ('serviceWorker' in navigator && (process.env.NODE_ENV === 'production' || window.location.protocol === 'https:')) {
  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('/sw.js')
      .then((reg) => {
        // Check for updates on tab focus
        document.addEventListener('visibilitychange', () => {
          if (document.visibilityState === 'visible') {
            reg.update().catch(() => {});
          }
        });
      })
      .catch((err) => {
        console.warn('PWA service worker registration skipped/failed:', err);
      });
  });
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
