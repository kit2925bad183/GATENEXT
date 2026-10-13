import React from 'react';
import { Link } from 'react-router-dom';
import { Logo } from '@/components/Logo';
import { ShieldX, Home } from 'lucide-react';

export const AccessDeniedPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-surface flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-md bg-white border border-border rounded-2xl p-8 shadow-card text-center space-y-4">
        <div className="flex justify-center mb-2">
          <Logo size="md" />
        </div>

        <div className="w-14 h-14 rounded-full bg-status-rejected-bg border border-status-rejected-border text-status-rejected-text flex items-center justify-center mx-auto">
          <ShieldX className="w-8 h-8" />
        </div>

        <h1 className="text-xl font-bold text-text-main">Access Restricted (403)</h1>
        <p className="text-xs text-text-secondary leading-relaxed">
          You do not have the required role permissions to access this community area.
        </p>

        <div className="pt-4 border-t border-border flex justify-center">
          <Link
            to="/login"
            className="px-4 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors flex items-center gap-1.5"
          >
            <Home className="w-4 h-4" /> Go to Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
};
