import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { apiRequest } from '@/lib/api';
import { DashboardStats, Visit } from '@/types';
import { MiniApartmentBanner } from '@/scenes/MiniApartmentBanner';
import { StatusBadge } from '@/components/StatusBadge';
import {
  DoorOpen,
  UserPlus,
  QrCode,
  LogIn,
  LogOut,
  Clock,
  AlertTriangle,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
} from 'lucide-react';

export const GateDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [waitingApprovals, setWaitingApprovals] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadGateData = async () => {
      try {
        setLoading(true);
        const [statsData, waitingData] = await Promise.all([
          apiRequest<DashboardStats>('visits/dashboard-stats'),
          apiRequest<Visit[]>('security/waiting-approvals'),
        ]);
        setStats(statsData);
        setWaitingApprovals(waitingData || []);
      } catch (err) {
      } finally {
        setLoading(false);
      }
    };
    loadGateData();
  }, []);

  return (
    <div className="space-y-6">
      {/* Banner */}
      <MiniApartmentBanner
        title={`Gate Security Desk — ${user?.assignedGateName || 'Perimeter Control'}`}
        subtitle={`Logged in Officer: ${user?.fullName}. Station Code: ${user?.assignedGateCode || 'GATE-01'}. Record admissions and process physical entries.`}
        badge="Active Security Terminal"
      />

      {/* Prominent Quick Gate Actions */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
        <Link
          to="/security/register-walk-in"
          className="bg-primary hover:bg-primary-hover text-white rounded-xl p-4 shadow-sm transition-all flex flex-col justify-between group"
        >
          <div className="w-9 h-9 rounded-lg bg-white/20 flex items-center justify-center mb-3">
            <UserPlus className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="text-sm font-bold">Register Walk-In</div>
            <div className="text-[11px] text-white/80 mt-0.5 flex items-center justify-between">
              <span>Notify resident</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </div>
          </div>
        </Link>

        <Link
          to="/security/verify-pass"
          className="bg-white hover:bg-surface border border-border rounded-xl p-4 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between group"
        >
          <div className="w-9 h-9 rounded-lg bg-primary-light text-primary flex items-center justify-center mb-3">
            <QrCode className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold text-text-main">Verify Pass / QR</div>
            <div className="text-[11px] text-text-secondary mt-0.5 flex items-center justify-between group-hover:text-primary">
              <span>Manual or scan</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </div>
          </div>
        </Link>

        <Link
          to="/security/waiting-approvals"
          className="bg-white hover:bg-surface border border-border rounded-xl p-4 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between group"
        >
          <div className="w-9 h-9 rounded-lg bg-[#FFF8E6] text-[#995B00] flex items-center justify-center mb-3">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold text-text-main">Waiting Approvals</div>
            <div className="text-[11px] text-[#995B00] font-semibold mt-0.5 flex items-center justify-between">
              <span>{stats?.waitingApprovals ?? 0} awaiting resident</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </div>
          </div>
        </Link>

        <Link
          to="/security/inside"
          className="bg-white hover:bg-surface border border-border rounded-xl p-4 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between group"
        >
          <div className="w-9 h-9 rounded-lg bg-accent-blue text-primary flex items-center justify-center mb-3">
            <LogIn className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold text-text-main">Currently Inside</div>
            <div className="text-[11px] text-primary font-semibold mt-0.5 flex items-center justify-between">
              <span>{stats?.currentVisitors ?? 0} active inside</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </div>
          </div>
        </Link>
      </div>

      {/* Waiting Walk-in Approvals Live Queue */}
      <div className="bg-white border border-border rounded-xl p-5 shadow-card">
        <div className="flex items-center justify-between mb-4 pb-3 border-b border-border">
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-[#995B00]" />
            <h2 className="text-sm font-bold text-text-main">Walk-In Visitors Awaiting Resident Response</h2>
          </div>
          <Link to="/security/waiting-approvals" className="text-xs font-semibold text-primary hover:underline">
            View full list
          </Link>
        </div>

        {waitingApprovals.length === 0 ? (
          <div className="text-center py-8 text-xs text-text-secondary">
            <CheckCircle2 className="w-8 h-8 text-[#137547] mx-auto mb-1.5" />
            No visitors currently waiting for resident approval at the gate.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {waitingApprovals.slice(0, 6).map((visit) => (
              <div
                key={visit.id}
                className="p-3.5 bg-surface rounded-xl border border-border flex flex-col justify-between gap-3 text-xs hover:border-primary transition-colors"
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <span className="font-bold text-text-main">{visit.visitorName}</span>
                    <StatusBadge status={visit.status} size="sm" />
                  </div>
                  <div className="text-[11px] text-text-secondary mt-1">
                    Destination: <strong className="text-primary font-bold">{visit.destinationBlockName} — Flat {visit.destinationFlatNumber}</strong>
                  </div>
                  <div className="text-[11px] text-text-muted mt-0.5">
                    Host: {visit.hostNameSnapshot} ({visit.hostMobileSnapshot})
                  </div>
                  <div className="text-[11px] text-text-secondary bg-white p-1.5 rounded border border-border-light mt-2">
                    Purpose: {visit.purpose}
                  </div>
                </div>

                <div className="pt-2 border-t border-border-light flex justify-between items-center text-[10px] text-text-muted">
                  <span>Ref: {visit.visitReference}</span>
                  <Link
                    to={`/visits/${visit.id}`}
                    className="text-primary font-bold hover:underline"
                  >
                    View Status →
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
