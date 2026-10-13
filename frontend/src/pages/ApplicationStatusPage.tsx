import React from 'react';
import { useAuth } from '@/app/AuthContext';
import { Logo } from '@/components/Logo';
import { Clock, XCircle, LogOut, RefreshCw } from 'lucide-react';

export const ApplicationStatusPage: React.FC = () => {
  const { user, refreshSession, logout } = useAuth();

  const isPending = user?.verificationStatus === 'PENDING' || user?.status === 'PENDING';
  const isRejected = user?.verificationStatus === 'REJECTED';

  return (
    <div className="min-h-screen bg-surface flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-md bg-white border border-border rounded-2xl p-8 shadow-card text-center">
        <div className="flex justify-center mb-6">
          <Logo size="md" />
        </div>

        {isRejected ? (
          <div className="space-y-4">
            <div className="w-14 h-14 rounded-full bg-status-rejected-bg border border-status-rejected-border text-status-rejected-text flex items-center justify-center mx-auto">
              <XCircle className="w-8 h-8" />
            </div>
            <h1 className="text-xl font-bold text-text-main">Registration Not Approved</h1>
            <p className="text-xs text-text-secondary leading-relaxed">
              Your resident registration could not be verified by the community management.
            </p>
            {user?.rejectionReason && (
              <div className="p-3 bg-status-rejected-bg rounded-xl border border-status-rejected-border text-xs text-status-rejected-text text-left">
                <strong>Reason:</strong> {user.rejectionReason}
              </div>
            )}
            <p className="text-[11px] text-text-muted">
              Please contact the community security office or property management if you believe this was an error.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="w-14 h-14 rounded-full bg-status-pending-bg border border-status-pending-border text-status-pending-text flex items-center justify-center mx-auto">
              <Clock className="w-8 h-8 animate-pulse" />
            </div>
            <h1 className="text-xl font-bold text-text-main">Application Pending Review</h1>
            <p className="text-xs text-text-secondary leading-relaxed">
              Welcome, <strong className="text-text-main">{user?.fullName}</strong>! Your resident verification for{' '}
              <strong className="text-text-main">
                {user?.blockName} {user?.flatNumber}
              </strong>{' '}
              is currently under review by the estate manager.
            </p>
            <div className="p-3.5 bg-surface rounded-xl border border-border text-xs text-left space-y-1.5">
              <div className="flex justify-between">
                <span className="text-text-secondary">Registered Email:</span>
                <span className="font-semibold text-text-main">{user?.email}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-secondary">Mobile:</span>
                <span className="font-semibold text-text-main">{user?.mobile}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-text-secondary">Status:</span>
                <span className="font-bold text-status-pending-text">Pending Admin Approval</span>
              </div>
            </div>
          </div>
        )}

        <div className="mt-8 pt-6 border-t border-border flex items-center justify-between gap-3">
          <button
            onClick={() => refreshSession()}
            className="flex-1 py-2 px-3 bg-surface hover:bg-border text-text-main border border-border rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" /> Check Status
          </button>
          <button
            onClick={logout}
            className="py-2 px-4 text-xs font-semibold text-status-rejected-text hover:bg-status-rejected-bg border border-border rounded-lg flex items-center gap-1.5 transition-colors"
          >
            <LogOut className="w-3.5 h-3.5" /> Sign Out
          </button>
        </div>
      </div>
    </div>
  );
};
