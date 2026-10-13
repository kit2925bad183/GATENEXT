import React from 'react';
import { usePwa } from '@/lib/pwaContext';
import { Sparkles, RefreshCw } from 'lucide-react';

export const PwaUpdateToast: React.FC = () => {
  const { hasUpdate, updateApp } = usePwa();

  if (!hasUpdate) return null;

  return (
    <div
      role="status"
      className="fixed bottom-20 sm:bottom-6 right-4 sm:right-6 bg-slate-900 text-white px-4 py-3 rounded-xl shadow-2xl border border-slate-700 z-50 flex items-center gap-3 animate-in fade-in slide-in-from-bottom-4 duration-300 max-w-sm"
    >
      <div className="w-8 h-8 rounded-lg bg-primary/30 text-primary flex items-center justify-center flex-shrink-0">
        <Sparkles className="w-4 h-4 text-primary-light" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-xs font-bold text-white">Update Available</p>
        <p className="text-[11px] text-slate-300 truncate">A new version of GateNest is ready.</p>
      </div>
      <button
        onClick={updateApp}
        className="px-3 py-1.5 bg-primary hover:bg-primary-hover text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors shadow-sm flex-shrink-0"
      >
        <RefreshCw className="w-3 h-3" />
        Update
      </button>
    </div>
  );
};
