import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { apiRequest } from '@/lib/api';
import { DashboardStats, Visit } from '@/types';
import { MiniApartmentBanner } from '@/scenes/MiniApartmentBanner';
import { StatusBadge } from '@/components/StatusBadge';
import {
  Send,
  Calendar,
  Inbox,
  LogIn,
  History,
  ArrowRight,
  Clock,
  AlertCircle,
  CheckCircle2,
} from 'lucide-react';
import { format } from 'date-fns';

export const ResidentDashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [pendingApprovals, setPendingApprovals] = useState<Visit[]>([]);
  const [visitorsInside, setVisitorsInside] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadResidentData = async () => {
      try {
        setLoading(true);
        const [statsData, pendingData, insideData] = await Promise.all([
          apiRequest<DashboardStats>('visits/dashboard-stats'),
          apiRequest<Visit[]>('resident/approval-inbox'),
          apiRequest<Visit[]>('resident/my-visitors-inside'),
        ]);
        setStats(statsData);
        setPendingApprovals(pendingData || []);
        setVisitorsInside(insideData || []);
      } catch (err) {
      } finally {
        setLoading(false);
      }
    };
    loadResidentData();
  }, []);

  const statCards = [
    {
      title: 'Upcoming Invitations',
      value: stats?.upcomingInvitations ?? 0,
      icon: Calendar,
      color: 'text-primary',
      bg: 'bg-primary-light',
      link: '/resident/upcoming',
    },
    {
      title: 'Awaiting Your Approval',
      value: stats?.requestsAwaitingApproval ?? 0,
      icon: Inbox,
      color: 'text-[#995B00]',
      bg: 'bg-status-pending-bg',
      link: '/resident/approval-inbox',
      badge: (stats?.requestsAwaitingApproval || 0) > 0 ? 'Urgent' : undefined,
    },
    {
      title: 'My Visitors Inside',
      value: stats?.ownVisitorsInside ?? 0,
      icon: LogIn,
      color: 'text-primary',
      bg: 'bg-accent-blue',
      link: '/resident/visitors-inside',
    },
    {
      title: 'Completed Visits',
      value: stats?.ownCompletedVisits ?? 0,
      icon: History,
      color: 'text-text-secondary',
      bg: 'bg-surface',
      link: '/resident/history',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Banner */}
      <MiniApartmentBanner
        title={`Welcome Home, ${user?.fullName}!`}
        subtitle={`Your Residence: ${user?.blockName || 'Tower'} — Flat ${user?.flatNumber || ''}. Manage guest passes, approve gate arrivals, and view entry history.`}
        badge="Resident Household Portal"
      />

      {/* Urgent Approval Banner if any walk-in is waiting */}
      {pendingApprovals.length > 0 && (
        <div className="bg-[#FFF8E6] border border-[#FFE3A3] rounded-2xl p-4 flex flex-col sm:flex-row items-center justify-between gap-3 shadow-sm animate-in fade-in">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-[#FFE3A3] text-[#995B00] flex items-center justify-center flex-shrink-0">
              <Clock className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <div className="text-xs font-bold text-text-main">
                {pendingApprovals.length} Visitor Waiting at Gate for Approval
              </div>
              <p className="text-[11px] text-text-secondary">
                {pendingApprovals[0].visitorName} ({pendingApprovals[0].visitorCategory}) is at {pendingApprovals[0].entryGateName || 'Gate'}.
              </p>
            </div>
          </div>

          <Link
            to="/resident/approval-inbox"
            className="px-4 py-2 bg-[#995B00] hover:bg-[#804c00] text-white text-xs font-bold rounded-xl shadow-sm transition-colors flex-shrink-0 flex items-center gap-1.5"
          >
            Review & Respond <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      )}

      {/* Primary Action & Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3.5">
        {/* Quick Invite Button */}
        <Link
          to="/resident/invite"
          className="bg-primary hover:bg-primary-hover text-white rounded-xl p-4 shadow-sm transition-all flex flex-col justify-between group lg:col-span-1"
        >
          <div className="w-9 h-9 rounded-lg bg-white/20 flex items-center justify-center mb-3">
            <Send className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="text-sm font-bold">Invite Visitor</div>
            <div className="text-[11px] text-white/80 mt-0.5 flex items-center justify-between">
              <span>Generate QR pass</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </div>
          </div>
        </Link>

        {statCards.map((card, idx) => {
          const Icon = card.icon;
          return (
            <Link
              key={idx}
              to={card.link}
              className="bg-white border border-border rounded-xl p-4 shadow-card hover:shadow-card-hover transition-all flex flex-col justify-between group"
            >
              <div className="flex items-center justify-between mb-2">
                <div className={`w-8 h-8 rounded-lg ${card.bg} ${card.color} flex items-center justify-center`}>
                  <Icon className="w-4 h-4" />
                </div>
                {card.badge && (
                  <span className="text-[10px] font-bold px-1.5 py-0.5 bg-[#FFF8E6] text-[#995B00] border border-[#FFE3A3] rounded">
                    {card.badge}
                  </span>
                )}
              </div>
              <div>
                <div className="text-xl font-bold text-text-main tracking-tight">
                  {loading ? '—' : card.value}
                </div>
                <div className="text-[11px] font-medium text-text-secondary mt-0.5 group-hover:text-primary transition-colors flex items-center justify-between">
                  <span>{card.title}</span>
                  <ArrowRight className="w-3 h-3 opacity-0 group-hover:opacity-100 transition-opacity" />
                </div>
              </div>
            </Link>
          );
        })}
      </div>

      {/* Visitors Currently Inside Resident Flat */}
      <div className="bg-white border border-border rounded-xl p-5 shadow-card">
        <div className="flex items-center justify-between mb-4 pb-3 border-b border-border">
          <div className="flex items-center gap-2">
            <LogIn className="w-4 h-4 text-primary" />
            <h2 className="text-sm font-bold text-text-main">Visitors Inside Your Flat Right Now</h2>
          </div>
          <Link to="/resident/visitors-inside" className="text-xs font-semibold text-primary hover:underline">
            View full roster
          </Link>
        </div>

        {visitorsInside.length === 0 ? (
          <div className="text-center py-8 text-xs text-text-secondary">
            <CheckCircle2 className="w-8 h-8 text-text-muted mx-auto mb-1.5" />
            No visitors are currently logged inside your residence.
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {visitorsInside.map((v) => (
              <div key={v.id} className="p-3.5 bg-surface rounded-xl border border-border text-xs">
                <div className="flex items-center justify-between mb-1">
                  <span className="font-bold text-text-main">{v.visitorName}</span>
                  <StatusBadge status="INSIDE" size="sm" />
                </div>
                <div className="text-[11px] text-text-secondary mb-1">
                  {v.visitorCategory} • {v.purpose}
                </div>
                <div className="text-[10px] text-text-muted">
                  Entered: {v.checkInTime ? format(new Date(v.checkInTime), 'hh:mm a') : '—'} via {v.entryGateName}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
