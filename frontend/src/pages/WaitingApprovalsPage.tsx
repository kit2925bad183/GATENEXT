import React, { useState, useEffect } from 'react';
import { useAuth } from '@/app/AuthContext';
import { apiRequest } from '@/lib/api';
import { Visit } from '@/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Clock, RefreshCw, LogIn, UserCheck, MapPin, CheckCircle2 } from 'lucide-react';
import { format } from 'date-fns';

export const WaitingApprovalsPage: React.FC = () => {
  const { user } = useAuth();
  const [visits, setVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [checkingInId, setCheckingInId] = useState<number | null>(null);

  const loadWaiting = async () => {
    try {
      setLoading(true);
      const data = await apiRequest<Visit[]>('security/waiting-approvals');
      setVisits(data || []);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWaiting();
    const interval = setInterval(loadWaiting, 15000); // 15s auto refresh
    return () => clearInterval(interval);
  }, []);

  const handleQuickCheckIn = async (visit: Visit) => {
    try {
      setCheckingInId(visit.id);
      await apiRequest(`security/check-in/${visit.id}`, {
        method: 'POST',
        body: JSON.stringify({
          gateId: user?.assignedGateId || 1,
          notes: 'Admitted from waiting approvals desk',
        }),
      });
      loadWaiting();
    } catch (err: any) {
      alert(err.message || 'Check in failed');
    } finally {
      setCheckingInId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <Clock className="w-5 h-5 text-[#995B00]" />
            Walk-In Approvals Awaiting Resident Response
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Live queue of visitors registered at security gates waiting for resident authorization.
          </p>
        </div>

        <button
          onClick={loadWaiting}
          disabled={loading}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Grid of Waiting Visits */}
      {loading && visits.length === 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 bg-white border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : visits.length === 0 ? (
        <div className="bg-white border border-border rounded-xl p-12 text-center max-w-md mx-auto shadow-card">
          <div className="w-12 h-12 rounded-full bg-status-approved-bg text-[#137547] flex items-center justify-center mx-auto mb-3">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <h2 className="text-sm font-bold text-text-main mb-1">No Visitors Waiting</h2>
          <p className="text-xs text-text-secondary">
            All registered walk-in visitors have been approved or processed.
          </p>
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
                  <div className="flex items-center gap-1.5 text-text-secondary">
                    <MapPin className="w-3.5 h-3.5 text-primary flex-shrink-0" />
                    <span>
                      Destination: <strong className="text-text-main">{visit.destinationBlockName} — Flat {visit.destinationFlatNumber}</strong>
                    </span>
                  </div>
                  <div className="text-[11px] text-text-muted">
                    Host: <strong className="text-text-secondary">{visit.hostNameSnapshot}</strong> ({visit.hostMobileSnapshot})
                  </div>
                  <div className="text-[11px] text-text-secondary pt-1 border-t border-border-light">
                    Purpose: <em>{visit.purpose}</em>
                  </div>
                </div>
              </div>

              <div className="pt-3 border-t border-border-light flex items-center justify-between text-[11px]">
                <span className="text-text-muted">
                  Requested: {format(new Date(visit.createdAt), 'hh:mm a')}
                </span>

                {visit.status === 'APPROVED' ? (
                  <button
                    onClick={() => handleQuickCheckIn(visit)}
                    disabled={checkingInId === visit.id}
                    className="px-3 py-1.5 bg-[#137547] hover:bg-[#0f5c38] text-white font-bold rounded-lg text-xs flex items-center gap-1 transition-colors"
                  >
                    <LogIn className="w-3.5 h-3.5" /> Check In Now
                  </button>
                ) : (
                  <span className="text-[10px] font-semibold text-status-pending-text bg-status-pending-bg px-2 py-0.5 rounded">
                    Awaiting Response
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
