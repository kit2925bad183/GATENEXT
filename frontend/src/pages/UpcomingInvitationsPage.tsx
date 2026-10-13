import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '@/lib/api';
import { Visit, PassCredential } from '@/types';
import { Modal } from '@/components/Modal';
import { PrintablePass } from '@/components/PrintablePass';
import { StatusBadge } from '@/components/StatusBadge';
import { Calendar, Plus, RefreshCw, XCircle, QrCode, Clock, Eye, AlertCircle } from 'lucide-react';
import { format } from 'date-fns';

export const UpcomingInvitationsPage: React.FC = () => {
  const [visits, setVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedPass, setSelectedPass] = useState<PassCredential | null>(null);
  const [passModalOpen, setPassModalOpen] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const loadUpcoming = async () => {
    try {
      setLoading(true);
      const data = await apiRequest<Visit[]>('resident/invitations/upcoming');
      setVisits(data || []);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUpcoming();
  }, []);

  const handleCancel = async (visit: Visit) => {
    if (!window.confirm(`Are you sure you want to cancel the invitation for ${visit.visitorName}?`)) {
      return;
    }
    try {
      setActionLoading(true);
      await apiRequest(`resident/visits/${visit.id}/cancel`, { method: 'POST' });
      loadUpcoming();
    } catch (err: any) {
      alert(err.message || 'Cancellation failed');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRegenerate = async (visit: Visit) => {
    try {
      setActionLoading(true);
      const cred = await apiRequest<PassCredential>(`resident/visits/${visit.id}/regenerate-pass`, {
        method: 'POST',
      });
      setSelectedPass(cred);
      setPassModalOpen(true);
      loadUpcoming();
    } catch (err: any) {
      alert(err.message || 'Pass regeneration failed');
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
            <Calendar className="w-5 h-5 text-primary" />
            Upcoming Visitor Invitations & Passes
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Active pre-approved passes for expected family, deliveries, services, and cabs.
          </p>
        </div>

        <Link
          to="/resident/invite"
          className="inline-flex items-center gap-2 px-3.5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors self-start"
        >
          <Plus className="w-4 h-4" /> Invite New Visitor
        </Link>
      </div>

      {/* Grid of Invitations */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 bg-white border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : visits.length === 0 ? (
        <div className="bg-white border border-border rounded-xl p-12 text-center max-w-md mx-auto shadow-card">
          <Calendar className="w-8 h-8 text-text-muted mx-auto mb-2" />
          <h2 className="text-sm font-bold text-text-main mb-1">No Upcoming Passes</h2>
          <p className="text-xs text-text-secondary mb-4">
            You have no active visitor passes scheduled for arrival.
          </p>
          <Link
            to="/resident/invite"
            className="px-4 py-2 bg-primary text-white text-xs font-semibold rounded-lg shadow-sm hover:bg-primary-hover transition-colors"
          >
            Create Visitor Pass
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {visits.map((visit) => (
            <div
              key={visit.id}
              className="bg-white border border-border rounded-xl p-5 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <div>
                    <h3 className="text-sm font-bold text-text-main">{visit.visitorName}</h3>
                    <div className="font-mono text-[10px] text-text-muted">{visit.visitorMobile}</div>
                  </div>
                  <StatusBadge status={visit.status} size="sm" />
                </div>

                <div className="p-3 bg-surface rounded-xl border border-border-light text-xs space-y-1.5 my-3">
                  <div className="text-text-secondary">
                    Category: <strong className="text-text-main">{visit.visitorCategory}</strong>
                  </div>
                  <div className="text-text-secondary">
                    Purpose: <em>{visit.purpose}</em>
                  </div>
                  <div className="flex items-center gap-1.5 text-[11px] text-primary font-medium pt-1 border-t border-border-light">
                    <Clock className="w-3.5 h-3.5" />
                    <span>
                      {format(new Date(visit.arrivalWindowStart), 'dd MMM, hh:mm a')} –{' '}
                      {format(new Date(visit.arrivalWindowEnd), 'hh:mm a')}
                    </span>
                  </div>
                </div>
              </div>

              <div className="pt-3 border-t border-border-light flex items-center justify-between gap-2 text-xs">
                <span className="font-mono text-[10px] text-text-muted truncate">
                  {visit.visitReference}
                </span>

                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => handleRegenerate(visit)}
                    disabled={actionLoading}
                    className="p-1.5 bg-surface hover:bg-border text-text-secondary rounded-lg transition-colors"
                    title="Regenerate QR Pass"
                  >
                    <RefreshCw className="w-3.5 h-3.5" />
                  </button>

                  <button
                    onClick={() => handleCancel(visit)}
                    disabled={actionLoading}
                    className="p-1.5 bg-surface hover:bg-status-rejected-bg text-status-rejected-text rounded-lg transition-colors"
                    title="Cancel Invitation"
                  >
                    <XCircle className="w-3.5 h-3.5" />
                  </button>

                  <Link
                    to={`/visits/${visit.id}`}
                    className="px-2.5 py-1 bg-surface hover:bg-border text-text-main font-semibold rounded-lg text-[11px] flex items-center gap-1"
                  >
                    <Eye className="w-3 h-3" /> Details
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Printable Pass Modal */}
      <Modal
        isOpen={passModalOpen}
        onClose={() => setPassModalOpen(false)}
        title="Visitor Entry Pass Issued"
        maxWidth="md"
      >
        {selectedPass && (
          <PrintablePass
            visitReference={selectedPass.visitReference}
            visitorName={selectedPass.visitorName}
            blockName={selectedPass.destinationBlockName}
            flatNumber={selectedPass.destinationFlatNumber}
            hostName={selectedPass.hostName}
            arrivalWindowStart={selectedPass.arrivalWindowStart}
            arrivalWindowEnd={selectedPass.arrivalWindowEnd}
            passToken={selectedPass.passToken}
            qrCodeDataUrl={selectedPass.qrCodeDataUrl}
          />
        )}
      </Modal>
    </div>
  );
};
