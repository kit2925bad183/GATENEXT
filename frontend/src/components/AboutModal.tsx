import React, { useEffect, useState } from 'react';
import { Modal } from './Modal';
import { usePwa } from '@/lib/pwaContext';
import { apiRequest } from '@/lib/api';
import { Shield, CheckCircle2, Server, Smartphone, Globe, Radio } from 'lucide-react';

interface AboutModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const AboutModal: React.FC<AboutModalProps> = ({ isOpen, onClose }) => {
  const { isInstalled, isOnline } = usePwa();
  const [backendHealth, setBackendHealth] = useState<{ status: string; database?: string } | null>(null);

  useEffect(() => {
    if (isOpen && isOnline) {
      apiRequest<{ status: string; database?: string }>('health')
        .then((data) => setBackendHealth(data))
        .catch(() => setBackendHealth({ status: 'UNREACHABLE', database: 'UNKNOWN' }));
    }
  }, [isOpen, isOnline]);

  if (!isOpen) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="About GateNest" maxWidth="sm">
      <div className="space-y-4">
        <div className="text-center pb-2 border-b border-border">
          <div className="w-12 h-12 bg-primary/10 text-primary rounded-2xl flex items-center justify-center mx-auto mb-2 shadow-sm">
            <Shield className="w-6 h-6 text-primary" />
          </div>
          <h2 className="text-base font-bold text-text-main">GateNest Apartment Security</h2>
          <p className="text-xs text-text-secondary">Enterprise Visitor & Community Management</p>
          <span className="inline-block mt-1 px-2.5 py-0.5 bg-surface text-primary font-mono text-[11px] font-bold rounded-full border border-border">
            v1.2.0-pwa
          </span>
        </div>

        <div className="space-y-2.5 text-xs">
          <div className="flex items-center justify-between p-2.5 bg-surface/50 rounded-lg border border-border">
            <span className="text-text-secondary flex items-center gap-1.5">
              <Smartphone className="w-3.5 h-3.5 text-primary" />
              Runtime Mode
            </span>
            <span className="font-semibold text-text-main">
              {isInstalled ? 'Installed PWA (Standalone)' : 'Web Browser Tab'}
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 bg-surface/50 rounded-lg border border-border">
            <span className="text-text-secondary flex items-center gap-1.5">
              <Radio className="w-3.5 h-3.5 text-primary" />
              Network Connectivity
            </span>
            <span className={`font-semibold flex items-center gap-1 ${isOnline ? 'text-emerald-600' : 'text-amber-600'}`}>
              <span className={`w-2 h-2 rounded-full ${isOnline ? 'bg-emerald-500' : 'bg-amber-500'}`} />
              {isOnline ? 'Live Connection' : 'Offline'}
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 bg-surface/50 rounded-lg border border-border">
            <span className="text-text-secondary flex items-center gap-1.5">
              <Server className="w-3.5 h-3.5 text-primary" />
              Backend API & DB
            </span>
            <span className="font-semibold text-text-main">
              {backendHealth ? `${backendHealth.status} (DB: ${backendHealth.database || 'OK'})` : 'Checking...'}
            </span>
          </div>

          <div className="flex items-center justify-between p-2.5 bg-surface/50 rounded-lg border border-border">
            <span className="text-text-secondary flex items-center gap-1.5">
              <Globe className="w-3.5 h-3.5 text-primary" />
              Localization
            </span>
            <span className="font-semibold text-text-main">English / தமிழ் (Tamil)</span>
          </div>
        </div>

        <div className="p-3 bg-blue-50 border border-blue-100 rounded-xl text-[11px] text-blue-800 space-y-1">
          <p className="font-semibold">Security & Architecture Guarantee</p>
          <p className="text-blue-700 leading-relaxed">
            All database operations utilize pure parameterized SQL. Security sessions, tokens, and records are never cached offline.
          </p>
        </div>

        <div className="flex justify-end pt-1">
          <button
            onClick={onClose}
            className="px-4 py-1.5 bg-surface text-text-main hover:bg-border rounded-lg text-xs font-semibold transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </Modal>
  );
};
