import React from 'react';
import { usePwa } from '@/lib/pwaContext';
import { WifiOff, RefreshCw, AlertTriangle } from 'lucide-react';

export const OfflineBanner: React.FC = () => {
  const { isOnline } = usePwa();

  if (isOnline) return null;

  return (
    <div
      role="alert"
      className="bg-amber-500 text-white px-4 py-2.5 flex items-center justify-between text-xs sm:text-sm font-medium shadow-md sticky top-0 z-50 border-b border-amber-600 animate-in slide-in-from-top duration-200"
    >
      <div className="flex items-center gap-2 max-w-4xl mx-auto flex-1">
        <WifiOff className="w-4 h-4 flex-shrink-0 animate-pulse" />
        <span>
          <strong>Offline Mode:</strong> You are currently disconnected. Gate check-ins, approvals, and mutations are disabled to maintain security integrity.
        </span>
      </div>
      <button
        onClick={() => window.location.reload()}
        className="ml-3 px-3 py-1 bg-white/20 hover:bg-white/30 rounded text-xs font-semibold flex items-center gap-1.5 transition-colors flex-shrink-0"
        aria-label="Retry network connection"
      >
        <RefreshCw className="w-3.5 h-3.5" />
        Retry
      </button>
    </div>
  );
};
