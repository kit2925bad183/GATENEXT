import React from 'react';
import { VisitEvent } from '@/types';
import { format } from 'date-fns';
import {
  Clock,
  CheckCircle,
  XCircle,
  LogIn,
  LogOut,
  Ban,
  AlertTriangle,
  RefreshCw,
  ShieldAlert,
  FileText,
} from 'lucide-react';

interface TimelineProps {
  events: VisitEvent[];
}

export const Timeline: React.FC<TimelineProps> = ({ events }) => {
  if (!events || events.length === 0) {
    return (
      <div className="text-center py-6 text-xs text-text-secondary bg-surface rounded-xl border border-border">
        No event history recorded yet.
      </div>
    );
  }

  const getEventIcon = (type: string) => {
    switch (type) {
      case 'CREATED':
        return <Clock className="w-4 h-4 text-primary" />;
      case 'APPROVED':
        return <CheckCircle className="w-4 h-4 text-[#137547]" />;
      case 'REJECTED':
        return <XCircle className="w-4 h-4 text-[#C02B10]" />;
      case 'CHECKED_IN':
        return <LogIn className="w-4 h-4 text-primary" />;
      case 'CHECKED_OUT':
        return <LogOut className="w-4 h-4 text-[#58677D]" />;
      case 'CANCELLED':
        return <Ban className="w-4 h-4 text-[#687385]" />;
      case 'EXPIRED':
        return <AlertTriangle className="w-4 h-4 text-[#B31B1B]" />;
      case 'PASS_REGENERATED':
        return <RefreshCw className="w-4 h-4 text-teal" />;
      case 'OVERRIDDEN':
        return <ShieldAlert className="w-4 h-4 text-[#995B00]" />;
      default:
        return <FileText className="w-4 h-4 text-text-secondary" />;
    }
  };

  return (
    <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-0.5 before:bg-border">
      {events.map((ev, index) => (
        <div key={ev.id || index} className="relative group">
          {/* Node Icon */}
          <div className="absolute -left-6 top-0.5 w-5 h-5 rounded-full bg-white border border-border flex items-center justify-center shadow-sm">
            {getEventIcon(ev.eventType)}
          </div>

          <div className="bg-surface rounded-xl border border-border p-3.5 shadow-sm transition-all hover:shadow-card">
            <div className="flex items-center justify-between gap-2 mb-1">
              <span className="text-xs font-bold text-text-main">
                {ev.eventType.replace('_', ' ')}
              </span>
              <span className="text-[11px] text-text-muted">
                {format(new Date(ev.createdAt), 'dd MMM yyyy • hh:mm a')}
              </span>
            </div>

            {ev.notes && (
              <p className="text-xs text-text-secondary leading-relaxed mb-1.5">
                {ev.notes}
              </p>
            )}

            <div className="flex items-center gap-3 text-[11px] text-text-secondary font-medium">
              {ev.actorName && (
                <span>
                  Actor: <strong className="text-text-main">{ev.actorName}</strong> ({ev.actorRole})
                </span>
              )}
              {ev.gateName && <span>• Gate: <strong>{ev.gateName}</strong></span>}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};
