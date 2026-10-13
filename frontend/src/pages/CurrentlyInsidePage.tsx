import React, { useState, useEffect } from 'react';
import { useAuth } from '@/app/AuthContext';
import { apiRequest } from '@/lib/api';
import { Visit } from '@/types';
import { StatusBadge } from '@/components/StatusBadge';
import { LogIn, LogOut, Clock, Printer, Car, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { format } from 'date-fns';

export const CurrentlyInsidePage: React.FC = () => {
  const { user } = useAuth();
  const [visits, setVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [checkingOutId, setCheckingOutId] = useState<number | null>(null);

  const loadInside = async () => {
    try {
      setLoading(true);
      const data = await apiRequest<Visit[]>('security/inside');
      setVisits(data || []);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInside();
    const interval = setInterval(loadInside, 20000);
    return () => clearInterval(interval);
  }, []);

  const handleCheckOut = async (visit: Visit) => {
    try {
      setCheckingOutId(visit.id);
      await apiRequest(`security/check-out/${visit.id}`, {
        method: 'POST',
        body: JSON.stringify({
          gateId: user?.assignedGateId || 1,
          notes: 'Checked out from gate inside roster',
        }),
      });
      loadInside();
    } catch (err: any) {
      alert(err.message || 'Check-out failed');
    } finally {
      setCheckingOutId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <LogIn className="w-5 h-5 text-primary" />
            Visitors Currently Inside Premises
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Real-time physical occupancy log of visitors currently inside the gates.
          </p>
        </div>

        <button
          onClick={() => window.print()}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors self-start shadow-sm no-print"
        >
          <Printer className="w-4 h-4 text-primary" /> Print Occupancy List
        </button>
      </div>

      {/* Grid of Inside Visitors */}
      {loading && visits.length === 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 bg-white border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : visits.length === 0 ? (
        <div className="bg-white border border-border rounded-xl p-12 text-center max-w-md mx-auto shadow-card">
          <div className="w-12 h-12 rounded-full bg-surface text-text-secondary flex items-center justify-center mx-auto mb-3">
            <CheckCircle2 className="w-6 h-6 text-[#137547]" />
          </div>
          <h2 className="text-sm font-bold text-text-main mb-1">Zero Visitors Inside</h2>
          <p className="text-xs text-text-secondary">
            No outside visitors or contractors are currently logged inside the community.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {visits.map((visit) => (
            <div
              key={visit.id}
              className={`bg-white border rounded-xl p-5 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between ${
                visit.overstay ? 'border-status-rejected-border' : 'border-border'
              }`}
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <div>
                    <h3 className="text-sm font-bold text-text-main">{visit.visitorName}</h3>
                    <span className="text-[10px] font-semibold text-text-secondary bg-surface px-1.5 py-0.2 rounded border border-border">
                      {visit.visitorCategory}
                    </span>
                  </div>
                  {visit.overstay ? (
                    <span className="inline-flex items-center gap-1 text-[11px] font-bold text-[#C02B10] bg-[#FDF0ED] px-2 py-0.5 rounded border border-[#F9CECB]">
                      <AlertTriangle className="w-3 h-3" /> Overstay
                    </span>
                  ) : (
                    <StatusBadge status="INSIDE" size="sm" />
                  )}
                </div>

                <div className="p-3 bg-surface rounded-xl border border-border-light text-xs space-y-1.5 my-3">
                  <div>
                    <span className="text-[10px] text-text-muted block">Destination</span>
                    <strong className="text-primary font-bold">
                      {visit.destinationBlockName} — Flat {visit.destinationFlatNumber}
                    </strong>
                  </div>
                  <div className="text-[11px] text-text-secondary">
                    Host: <strong>{visit.hostNameSnapshot}</strong>
                  </div>
                  {visit.vehicleNumber && (
                    <div className="text-[11px] font-mono text-text-muted flex items-center gap-1">
                      <Car className="w-3 h-3" /> {visit.vehicleNumber}
                    </div>
                  )}
                  <div className="text-[11px] text-text-muted pt-1 border-t border-border-light">
                    Entered: {visit.checkInTime ? format(new Date(visit.checkInTime), 'hh:mm a') : '—'}{' '}
                    via {visit.entryGateName || 'Gate'}
                  </div>
                </div>
              </div>

              <div className="pt-3 border-t border-border-light flex items-center justify-between">
                <span className="font-mono text-[10px] text-text-muted">{visit.visitReference}</span>
                <button
                  onClick={() => handleCheckOut(visit)}
                  disabled={checkingOutId === visit.id}
                  className="px-3 py-1.5 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors flex items-center gap-1.5 no-print"
                >
                  <LogOut className="w-3.5 h-3.5" /> Check Out
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
