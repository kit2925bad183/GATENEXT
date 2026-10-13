import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { ResidentProfile } from '@/types';
import { Modal } from '@/components/Modal';
import { StatusBadge } from '@/components/StatusBadge';
import { UserCheck, Check, X, Phone, Mail, Building2, AlertCircle, RefreshCw } from 'lucide-react';
import { format } from 'date-fns';

export const ResidentApprovalQueuePage: React.FC = () => {
  const [profiles, setProfiles] = useState<ResidentProfile[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedProfile, setSelectedProfile] = useState<ResidentProfile | null>(null);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadPending = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiRequest<ResidentProfile[]>('admin/residents/pending');
      setProfiles(data || []);
    } catch (err: any) {
      setError(err.message || 'Failed to load pending queue');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPending();
  }, []);

  const handleApprove = async (profile: ResidentProfile) => {
    try {
      setActionLoading(true);
      await apiRequest(`admin/residents/${profile.id}/approve`, { method: 'POST' });
      setProfiles((prev) => prev.filter((p) => p.id !== profile.id));
    } catch (err: any) {
      alert(err.message || 'Approval failed');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProfile || !rejectReason.trim()) return;

    try {
      setActionLoading(true);
      await apiRequest(`admin/residents/${selectedProfile.id}/reject`, {
        method: 'POST',
        body: JSON.stringify({ reason: rejectReason.trim() }),
      });
      setProfiles((prev) => prev.filter((p) => p.id !== selectedProfile.id));
      setRejectModalOpen(false);
      setRejectReason('');
      setSelectedProfile(null);
    } catch (err: any) {
      alert(err.message || 'Rejection failed');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <UserCheck className="w-5 h-5 text-primary" />
            Resident Registration Queue
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Review and verify resident applicants against community allotment and tenancy records.
          </p>
        </div>

        <button
          onClick={loadPending}
          disabled={loading}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Queue List */}
      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-24 bg-white border border-border rounded-xl animate-pulse p-4" />
          ))}
        </div>
      ) : profiles.length === 0 ? (
        <div className="bg-white border border-border rounded-xl p-12 text-center max-w-md mx-auto shadow-card">
          <div className="w-12 h-12 rounded-full bg-status-approved-bg text-[#137547] flex items-center justify-center mx-auto mb-3">
            <UserCheck className="w-6 h-6" />
          </div>
          <h2 className="text-sm font-bold text-text-main mb-1">Queue is Empty</h2>
          <p className="text-xs text-text-secondary">
            There are no pending resident registration applications requiring review at this time.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {profiles.map((profile) => (
            <div
              key={profile.id}
              className="bg-white border border-border rounded-xl p-5 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-3">
                  <div>
                    <h3 className="text-sm font-bold text-text-main">{profile.userFullName}</h3>
                    <span className="text-[11px] font-semibold text-primary bg-primary-light px-2 py-0.5 rounded-full inline-block mt-0.5">
                      {profile.occupancyType}
                    </span>
                  </div>
                  <StatusBadge status="PENDING" size="sm" />
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs text-text-secondary bg-surface p-3 rounded-lg border border-border-light mb-4">
                  <div>
                    <span className="text-[10px] text-text-muted block">Destination Flat</span>
                    <strong className="text-text-main text-xs">
                      {profile.blockName} — Flat {profile.flatNumber}
                    </strong>
                  </div>
                  <div>
                    <span className="text-[10px] text-text-muted block">Floor</span>
                    <span className="font-medium text-text-main">Floor {profile.floorNumber}</span>
                  </div>
                  <div className="flex items-center gap-1.5 truncate">
                    <Mail className="w-3.5 h-3.5 text-text-muted flex-shrink-0" />
                    <span className="truncate">{profile.userEmail}</span>
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Phone className="w-3.5 h-3.5 text-text-muted flex-shrink-0" />
                    <span>{profile.userMobile}</span>
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between pt-3 border-t border-border-light gap-2">
                <span className="text-[10px] text-text-muted">
                  Applied: {format(new Date(profile.createdAt), 'dd MMM, hh:mm a')}
                </span>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      setSelectedProfile(profile);
                      setRejectModalOpen(true);
                    }}
                    disabled={actionLoading}
                    className="px-3 py-1.5 bg-white border border-border hover:bg-status-rejected-bg text-status-rejected-text text-xs font-semibold rounded-lg transition-colors flex items-center gap-1"
                  >
                    <X className="w-3.5 h-3.5" /> Reject
                  </button>

                  <button
                    onClick={() => handleApprove(profile)}
                    disabled={actionLoading}
                    className="px-3 py-1.5 bg-[#137547] hover:bg-[#0f5c38] text-white text-xs font-semibold rounded-lg shadow-sm transition-colors flex items-center gap-1"
                  >
                    <Check className="w-3.5 h-3.5" /> Approve
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Reject Reason Modal */}
      <Modal
        isOpen={rejectModalOpen}
        onClose={() => setRejectModalOpen(false)}
        title={`Reject Resident Application: ${selectedProfile?.userFullName}`}
      >
        <form onSubmit={handleRejectSubmit} className="space-y-4 text-xs">
          <p className="text-text-secondary leading-relaxed">
            Please specify a clear reason for rejecting the registration application for Flat{' '}
            <strong>
              {selectedProfile?.blockName} {selectedProfile?.flatNumber}
            </strong>
            . This reason will be recorded and shown to the resident.
          </p>

          <div>
            <label className="block font-semibold text-text-main mb-1.5">Rejection Reason *</label>
            <textarea
              required
              rows={3}
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="E.g. Flat ownership documents mismatch with estate records..."
              className="w-full p-2.5 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-border">
            <button
              type="button"
              onClick={() => setRejectModalOpen(false)}
              className="px-3 py-1.5 border border-border rounded-lg font-semibold hover:bg-surface"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={actionLoading || !rejectReason.trim()}
              className="px-4 py-1.5 bg-status-rejected-text text-white font-semibold rounded-lg hover:opacity-90 transition-opacity disabled:opacity-50"
            >
              Confirm Rejection
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
