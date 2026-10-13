import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { Visit } from '@/types';
import { Modal } from '@/components/Modal';
import { StatusBadge } from '@/components/StatusBadge';
import { Inbox, Check, X, Clock, MapPin, Phone, Car, CheckCircle2 } from 'lucide-react';
import { format } from 'date-fns';

export const ApprovalInboxPage: React.FC = () => {
  const [visits, setVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedVisit, setSelectedVisit] = useState<Visit | null>(null);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  const loadInbox = async () => {
    try {
      setLoading(true);
      const data = await apiRequest<Visit[]>('resident/approval-inbox');
      setVisits(data || []);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInbox();
    const interval = setInterval(loadInbox, 15000); // 15s live polling
    return () => clearInterval(interval);
  }, []);

  const handleApprove = async (visit: Visit) => {
    try {
      setActionLoading(true);
      await apiRequest(`resident/visits/${visit.id}/approve`, {
        method: 'POST',
        body: JSON.stringify({ notes: 'Approved by resident host' }),
      });
      loadInbox();
    } catch (err: any) {
      alert(err.message || 'Approval failed');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRejectSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedVisit || !rejectReason.trim()) return;

    try {
      setActionLoading(true);
      await apiRequest(`resident/visits/${selectedVisit.id}/reject`, {
        method: 'POST',
        body: JSON.stringify({ reason: rejectReason.trim() }),
      });
      setRejectModalOpen(false);
      setRejectReason('');
      setSelectedVisit(null);
      loadInbox();
    } catch (err: any) {
      alert(err.message || 'Rejection failed');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="pb-4 border-b border-border">
        <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
          <Inbox className="w-5 h-5 text-primary" />
          Visitor Arrival Approval Inbox
        </h1>
        <p className="text-xs text-text-secondary mt-1">
          Review unexpected walk-in visitors currently waiting at security gate checkpoints.
        </p>
      </div>

      {/* Grid of Pending Requests */}
      {loading && visits.length === 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[1, 2].map((i) => (
            <div key={i} className="h-44 bg-white border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : visits.length === 0 ? (
        <div className="bg-white border border-border rounded-xl p-12 text-center max-w-md mx-auto shadow-card">
          <div className="w-12 h-12 rounded-full bg-status-approved-bg text-[#137547] flex items-center justify-center mx-auto mb-3">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <h2 className="text-sm font-bold text-text-main mb-1">Inbox Zero</h2>
          <p className="text-xs text-text-secondary">
            You have no pending walk-in visitors waiting for gate entry approval.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {visits.map((visit) => (
            <div
              key={visit.id}
              className="bg-white border-2 border-primary/30 rounded-2xl p-5 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <div>
                    <span className="text-[10px] font-bold uppercase tracking-wider text-primary bg-primary-light px-2 py-0.5 rounded-full">
                      {visit.visitorCategory}
                    </span>
                    <h3 className="text-base font-bold text-text-main mt-1.5">{visit.visitorName}</h3>
                    <div className="font-mono text-xs text-text-muted">{visit.visitorMobile}</div>
                  </div>
                  <StatusBadge status="PENDING_APPROVAL" size="sm" />
                </div>

                <div className="p-3.5 bg-surface rounded-xl border border-border text-xs space-y-1.5 my-3">
                  <div className="flex items-center gap-1.5 text-text-secondary">
                    <MapPin className="w-3.5 h-3.5 text-primary flex-shrink-0" />
                    <span>
                      Waiting at: <strong>{visit.entryGateName || 'Perimeter Gate'}</strong>
                    </span>
                  </div>
                  <div className="text-text-secondary">
                    Stated Purpose: <strong className="text-text-main">{visit.purpose}</strong>
                  </div>
                  {visit.vehicleNumber && (
                    <div className="text-[11px] font-mono text-text-muted flex items-center gap-1">
                      <Car className="w-3.5 h-3.5" /> Vehicle: {visit.vehicleNumber}
                    </div>
                  )}
                  <div className="text-[10px] text-text-muted pt-1 border-t border-border-light flex items-center gap-1">
                    <Clock className="w-3 h-3" /> Requested at{' '}
                    {format(new Date(visit.createdAt), 'hh:mm:ss a')}
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="pt-3 border-t border-border-light flex justify-end gap-2">
                <button
                  onClick={() => {
                    setSelectedVisit(visit);
                    setRejectModalOpen(true);
                  }}
                  disabled={actionLoading}
                  className="px-4 py-2 bg-white hover:bg-status-rejected-bg text-status-rejected-text border border-border text-xs font-semibold rounded-xl transition-colors flex items-center gap-1"
                >
                  <X className="w-4 h-4" /> Reject Entry
                </button>

                <button
                  onClick={() => handleApprove(visit)}
                  disabled={actionLoading}
                  className="px-5 py-2 bg-[#137547] hover:bg-[#0f5c38] text-white text-xs font-bold rounded-xl shadow-sm transition-colors flex items-center gap-1.5"
                >
                  <Check className="w-4 h-4" /> Approve Admission
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Rejection Reason Modal */}
      <Modal
        isOpen={rejectModalOpen}
        onClose={() => setRejectModalOpen(false)}
        title={`Reject Visitor: ${selectedVisit?.visitorName}`}
      >
        <form onSubmit={handleRejectSubmit} className="space-y-4 text-xs">
          <p className="text-text-secondary leading-relaxed">
            Please enter a reason for declining entry to <strong>{selectedVisit?.visitorName}</strong>. This note will be sent directly to gate security.
          </p>

          <div>
            <label className="block font-semibold text-text-main mb-1.5">Reason for Declining *</label>
            <textarea
              required
              rows={3}
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="E.g. Not expecting any deliveries / Resident not available..."
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
