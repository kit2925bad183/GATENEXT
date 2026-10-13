import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '@/lib/api';
import { DashboardStats, ResidentProfile } from '@/types';
import { MiniApartmentBanner } from '@/scenes/MiniApartmentBanner';
import { StatusBadge } from '@/components/StatusBadge';
import {
  Users,
  UserCheck,
  LogIn,
  LogOut,
  Clock,
  ArrowRight,
  ShieldAlert,
  BarChart3,
  Calendar,
} from 'lucide-react';

export const AdminDashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [pendingResidents, setPendingResidents] = useState<ResidentProfile[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        setLoading(true);
        const [statsData, pendingData] = await Promise.all([
          apiRequest<DashboardStats>('visits/dashboard-stats'),
          apiRequest<ResidentProfile[]>('admin/residents/pending?limit=5'),
        ]);
        setStats(statsData);
        setPendingResidents(pendingData || []);
      } catch (err) {
      } finally {
        setLoading(false);
      }
    };
    loadDashboard();
  }, []);

  const statCards = [
    {
      title: 'Active Residents',
      value: stats?.activeResidents ?? 0,
      icon: Users,
      color: 'text-primary',
      bg: 'bg-primary-light',
      link: '/admin/residents',
    },
    {
      title: 'Pending Registrations',
      value: stats?.pendingRegistrations ?? 0,
      icon: UserCheck,
      color: 'text-[#995B00]',
      bg: 'bg-status-pending-bg',
      link: '/admin/resident-approvals',
      badge: (stats?.pendingRegistrations || 0) > 0 ? 'Requires Action' : undefined,
    },
    {
      title: 'Visitors Inside Now',
      value: stats?.visitorsInside ?? 0,
      icon: LogIn,
      color: 'text-primary',
      bg: 'bg-accent-blue',
      link: '/admin/visits?status=INSIDE',
    },
    {
      title: 'Today Entries',
      value: stats?.entriesToday ?? 0,
      icon: LogIn,
      color: 'text-[#137547]',
      bg: 'bg-status-approved-bg',
      link: '/admin/visits',
    },
    {
      title: 'Today Exits',
      value: stats?.exitsToday ?? 0,
      icon: LogOut,
      color: 'text-text-secondary',
      bg: 'bg-surface',
      link: '/admin/visits?status=EXITED',
    },
    {
      title: 'Pending Visitor Approvals',
      value: stats?.pendingVisitorApprovals ?? 0,
      icon: Clock,
      color: 'text-[#995B00]',
      bg: 'bg-status-pending-bg',
      link: '/admin/visits?status=PENDING_APPROVAL',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Top Welcome Banner */}
      <MiniApartmentBanner
        title="Community Management Hub"
        subtitle="Full real-time oversight of apartment blocks, verified residents, security gates, and visitor traffic."
        badge="Administrator Portal"
      />

      {/* Metrics Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3.5">
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

      {/* Center Layout: Daily Entries Chart & Pending Registrations Queue */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Daily Entries Activity (2 cols) */}
        <div className="lg:col-span-2 bg-white border border-border rounded-xl p-5 shadow-card flex flex-col justify-between">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-border">
            <div className="flex items-center gap-2">
              <BarChart3 className="w-4 h-4 text-primary" />
              <h2 className="text-sm font-bold text-text-main">Daily Visitor Admissions (Past 7 Days)</h2>
            </div>
            <span className="text-[11px] text-text-secondary">Authoritative Check-in Logs</span>
          </div>

          {stats?.dailyEntries && stats.dailyEntries.length > 0 ? (
            <div className="h-44 flex items-end justify-between gap-2 pt-6 px-2">
              {stats.dailyEntries.map((day, dIdx) => {
                const maxCount = Math.max(...stats.dailyEntries!.map((d) => d.total_count), 1);
                const heightPercent = Math.max(12, Math.round((day.total_count / maxCount) * 100));

                return (
                  <div key={dIdx} className="flex-1 flex flex-col items-center gap-2 group">
                    <span className="text-[10px] font-bold text-text-main opacity-0 group-hover:opacity-100 transition-opacity">
                      {day.total_count}
                    </span>
                    <div
                      style={{ height: `${heightPercent}%` }}
                      className="w-full max-w-[36px] bg-primary group-hover:bg-primary-hover rounded-t-md transition-all shadow-sm"
                    />
                    <span className="text-[10px] text-text-secondary font-medium truncate w-full text-center">
                      {day.entry_date}
                    </span>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="h-44 flex flex-col items-center justify-center text-center p-6 bg-surface rounded-xl border border-border">
              <Calendar className="w-8 h-8 text-text-muted mb-2" />
              <div className="text-xs font-semibold text-text-main">No visitor admissions recorded this week</div>
              <p className="text-[11px] text-text-secondary mt-0.5">
                Physical check-ins from the gate desk will populate this chart.
              </p>
            </div>
          )}
        </div>

        {/* Pending Resident Approvals Queue (1 col) */}
        <div className="bg-white border border-border rounded-xl p-5 shadow-card flex flex-col justify-between">
          <div className="flex items-center justify-between mb-3 pb-3 border-b border-border">
            <div className="flex items-center gap-2">
              <UserCheck className="w-4 h-4 text-primary" />
              <h2 className="text-sm font-bold text-text-main">Pending Resident Queue</h2>
            </div>
            <Link
              to="/admin/resident-approvals"
              className="text-xs font-semibold text-primary hover:underline"
            >
              View all
            </Link>
          </div>

          <div className="space-y-3 flex-1 overflow-y-auto max-h-56">
            {pendingResidents.length === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-center py-8 text-xs text-text-secondary">
                <div className="w-10 h-10 rounded-full bg-status-approved-bg text-[#137547] flex items-center justify-center mb-2">
                  <UserCheck className="w-5 h-5" />
                </div>
                <span>All resident registrations verified!</span>
              </div>
            ) : (
              pendingResidents.map((pr) => (
                <div
                  key={pr.id}
                  className="p-3 bg-surface rounded-xl border border-border flex items-center justify-between gap-3 text-xs"
                >
                  <div>
                    <div className="font-bold text-text-main">{pr.userFullName}</div>
                    <div className="text-[11px] text-text-secondary">
                      {pr.blockName} • Flat {pr.flatNumber} ({pr.occupancyType})
                    </div>
                  </div>
                  <Link
                    to="/admin/resident-approvals"
                    className="px-2.5 py-1 bg-white border border-border rounded-lg font-semibold text-primary hover:bg-primary-light transition-colors text-[11px]"
                  >
                    Review
                  </Link>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
