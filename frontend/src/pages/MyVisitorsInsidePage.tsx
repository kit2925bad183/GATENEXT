import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { Visit } from '@/types';
import { StatusBadge } from '@/components/StatusBadge';
import { LogIn, Clock, Car, CheckCircle2 } from 'lucide-react';
import { format } from 'date-fns';

export const MyVisitorsInsidePage: React.FC = () => {
  const [visits, setVisitors] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);

  const loadInside = async () => {
    try {
      setLoading(true);
      const data = await apiRequest<Visit[]>('resident/my-visitors-inside');
      setVisitors(data || []);
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

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="pb-4 border-b border-border">
        <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
          <LogIn className="w-5 h-5 text-primary" />
          Visitors Inside My Residence
        </h1>
        <p className="text-xs text-text-secondary mt-1">
          Active visitors, delivery contractors, and guests currently checked into your flat.
        </p>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[1, 2].map((i) => (
            <div key={i} className="h-36 bg-white border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : visits.length === 0 ? (
        <div className="bg-white border border-border rounded-xl p-12 text-center max-w-md mx-auto shadow-card">
          <CheckCircle2 className="w-8 h-8 text-text-muted mx-auto mb-2" />
          <h2 className="text-sm font-bold text-text-main mb-1">No Visitors Inside</h2>
          <p className="text-xs text-text-secondary">
            No outside visitors are currently logged inside your apartment.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {visits.map((v) => (
            <div key={v.id} className="bg-white border border-border rounded-xl p-5 shadow-card space-y-3">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="text-sm font-bold text-text-main">{v.visitorName}</h3>
                  <span className="text-[10px] text-text-secondary font-medium bg-surface px-1.5 py-0.2 rounded border border-border">
                    {v.visitorCategory}
                  </span>
                </div>
                <StatusBadge status="INSIDE" size="sm" />
              </div>

              <div className="p-3 bg-surface rounded-xl border border-border-light text-xs space-y-1">
                <div>Purpose: <strong>{v.purpose}</strong></div>
                {v.vehicleNumber && <div>Vehicle: <span className="font-mono">{v.vehicleNumber}</span></div>}
                <div className="text-[11px] text-text-muted pt-1 border-t border-border-light">
                  Entered: {v.checkInTime ? format(new Date(v.checkInTime), 'hh:mm a') : '—'} via {v.entryGateName}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
