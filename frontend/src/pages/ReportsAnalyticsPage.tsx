import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import {
  Download,
  Printer,
  Calendar,
  Building,
  DoorClosed,
  Clock,
  AlertTriangle,
  TrendingUp,
  BarChart3,
  Users,
  ShieldCheck,
  ArrowRight
} from 'lucide-react';

export const ReportsAnalyticsPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();

  const [stats, setStats] = useState<Record<string, number>>({});
  const [categories, setCategories] = useState<Array<{ category: string; count: number }>>([]);
  const [gateTraffic, setGateTraffic] = useState<Array<{ gate_name: string; entry_count: number; exit_count: number }>>([]);
  const [dailyTrends, setDailyTrends] = useState<Array<{ visit_date: string; total_entries: number; total_exits: number }>>([]);
  const [blockTotals, setBlockTotals] = useState<Array<{ block_name: string; total_visits: number }>>([]);
  const [weekTotals, setWeekTotals] = useState<Array<{ visit_year: number; visit_week: number; entry_count: number; exit_count: number }>>([]);
  const [monthTrends, setMonthTrends] = useState<Array<{ visit_year: number; visit_month: number; total_visits: number }>>([]);
  const [avgDuration, setAvgDuration] = useState<{ completedVisitsCount: number; avgDurationMinutes: number }>({ completedVisitsCount: 0, avgDurationMinutes: 0 });
  const [busiestHours, setBusiestHours] = useState<Array<{ arrival_hour: number; count: number }>>([]);
  const [overdueVisits, setOverdueVisits] = useState<Array<any>>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReports = async () => {
    setLoading(true);
    setError(null);
    try {
      const [
        dashRes,
        catRes,
        gateRes,
        trendRes,
        blockRes,
        weekRes,
        monthRes,
        durRes,
        hoursRes,
        overdueRes
      ] = await Promise.all([
        api.get<{ data: Record<string, number> }>('/reports/dashboard'),
        api.get<{ data: Array<{ category: string; count: number }> }>('/reports/visitor-categories'),
        api.get<{ data: Array<{ gate_name: string; entry_count: number; exit_count: number }> }>('/reports/gate-traffic'),
        api.get<{ data: Array<{ visit_date: string; total_entries: number; total_exits: number }> }>('/reports/daily-trends?days=14'),
        api.get<{ data: Array<{ block_name: string; total_visits: number }> }>('/reports/block-wise'),
        api.get<{ data: Array<{ visit_year: number; visit_week: number; entry_count: number; exit_count: number }> }>('/reports/week-wise'),
        api.get<{ data: Array<{ visit_year: number; visit_month: number; total_visits: number }> }>('/reports/month-wise'),
        api.get<{ data: { completedVisitsCount: number; avgDurationMinutes: number } }>('/reports/average-duration'),
        api.get<{ data: Array<{ arrival_hour: number; count: number }> }>('/reports/busiest-hours'),
        api.get<{ data: Array<any> }>('/reports/overdue-visits')
      ]);

      setStats(dashRes.data?.data || {});
      setCategories(catRes.data?.data || []);
      setGateTraffic(gateRes.data?.data || []);
      setDailyTrends(trendRes.data?.data || []);
      setBlockTotals(blockRes.data?.data || []);
      setWeekTotals(weekRes.data?.data || []);
      setMonthTrends(monthRes.data?.data || []);
      setAvgDuration(durRes.data?.data || { completedVisitsCount: 0, avgDurationMinutes: 0 });
      setBusiestHours(hoursRes.data?.data || []);
      setOverdueVisits(overdueRes.data?.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load analytical reports');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleExportCsv = () => {
    window.location.href = '/api/v1/reports/export/visits.csv';
  };

  const handlePrint = () => {
    window.print();
  };

  const navigateToFilteredVisits = (filterKey: string, filterVal: string) => {
    navigate(`/admin/visits?${filterKey}=${encodeURIComponent(filterVal)}`);
  };

  return (
    <div className="p-4 sm:p-6 max-w-7xl mx-auto space-y-6 print:p-0">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-2xl font-bold text-text-main tracking-tight">
            {t('nav.reportsAnalytics')}
          </h1>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Real-time analytics, block breakdown, gate traffic, and completed duration metrics
          </p>
        </div>

        <div className="flex items-center gap-2 print:hidden">
          <button
            type="button"
            onClick={handlePrint}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-white hover:bg-surface border border-border text-text-main rounded-lg text-xs font-semibold shadow-xs transition-colors"
          >
            <Printer className="w-3.5 h-3.5 text-text-secondary" />
            Print Report
          </button>

          <button
            type="button"
            onClick={handleExportCsv}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-[#1E40AF] hover:bg-[#1D4ED8] text-white rounded-lg text-xs font-semibold shadow-xs transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            {t('common.exportCsv')}
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border text-status-rejected-text rounded-xl text-xs flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Top 4 KPI Metrics (Clickable) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div
          onClick={() => navigateToFilteredVisits('datePreset', 'today')}
          className="bg-white border border-border rounded-xl p-5 shadow-xs hover:border-primary cursor-pointer transition-all group"
        >
          <div className="text-xs font-semibold text-text-secondary group-hover:text-primary">
            {t('dashboard.entriesToday')}
          </div>
          <div className="text-3xl font-black text-text-main mt-1">
            {stats.totalVisitsToday || 0}
          </div>
          <div className="text-[11px] text-primary mt-2 flex items-center gap-1 font-medium">
            <span>View Today's Entries</span>
            <ArrowRight className="w-3 h-3 group-hover:translate-x-0.5 transition-transform" />
          </div>
        </div>

        <div
          onClick={() => navigateToFilteredVisits('status', 'INSIDE')}
          className="bg-white border border-border rounded-xl p-5 shadow-xs hover:border-primary cursor-pointer transition-all group"
        >
          <div className="text-xs font-semibold text-text-secondary group-hover:text-primary">
            {t('dashboard.visitorsInside')}
          </div>
          <div className="text-3xl font-black text-[#1E40AF] mt-1">
            {stats.currentlyInside || 0}
          </div>
          <div className="text-[11px] text-[#1E40AF] mt-2 flex items-center gap-1 font-medium">
            <span>Current Occupancy Active</span>
            <ArrowRight className="w-3 h-3 group-hover:translate-x-0.5 transition-transform" />
          </div>
        </div>

        <div
          onClick={() => navigateToFilteredVisits('isOverdue', 'true')}
          className="bg-white border border-border rounded-xl p-5 shadow-xs hover:border-red-500 cursor-pointer transition-all group"
        >
          <div className="text-xs font-semibold text-text-secondary group-hover:text-red-600">
            {t('dashboard.overdueVisits')}
          </div>
          <div className={`text-3xl font-black mt-1 ${(stats.overdueVisits || 0) > 0 ? 'text-red-600' : 'text-text-main'}`}>
            {stats.overdueVisits || 0}
          </div>
          <div className="text-[11px] text-red-600 mt-2 flex items-center gap-1 font-medium">
            <span>Exceeded Duration Limit</span>
            <ArrowRight className="w-3 h-3 group-hover:translate-x-0.5 transition-transform" />
          </div>
        </div>

        {/* Average Visit Duration Card */}
        <div className="bg-white border border-border rounded-xl p-5 shadow-xs">
          <div className="text-xs font-semibold text-text-secondary flex items-center justify-between">
            <span>Average Completed Duration</span>
            <Clock className="w-3.5 h-3.5 text-teal-600" />
          </div>
          <div className="text-3xl font-black text-teal-700 mt-1">
            {Math.round(avgDuration.avgDurationMinutes || 0)} <span className="text-sm font-semibold text-text-muted">mins</span>
          </div>
          <div className="text-[11px] text-text-muted mt-2">
            Based on {avgDuration.completedVisitsCount || 0} completed visits
          </div>
        </div>
      </div>

      {/* Grid: Block Breakdown & Gate Traffic */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Block-Wise Visitor Totals */}
        <div className="bg-white border border-border rounded-xl p-5 shadow-xs">
          <h2 className="text-sm font-bold text-text-main mb-3 flex items-center gap-2">
            <Building className="w-4 h-4 text-primary" />
            Block-Wise Visitor Activity
          </h2>
          <div className="space-y-2">
            {blockTotals.length === 0 ? (
              <p className="text-xs text-text-muted py-4 text-center">No block-wise records yet.</p>
            ) : (
              blockTotals.map((b) => (
                <div
                  key={b.block_name}
                  onClick={() => navigateToFilteredVisits('query', b.block_name)}
                  className="flex items-center justify-between p-2.5 rounded-lg bg-surface hover:bg-accent-blue/30 border border-border cursor-pointer transition-colors"
                >
                  <span className="font-semibold text-xs text-text-main">{b.block_name}</span>
                  <span className="font-bold text-xs text-primary">{b.total_visits} visits</span>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Gate Traffic Distribution */}
        <div className="bg-white border border-border rounded-xl p-5 shadow-xs">
          <h2 className="text-sm font-bold text-text-main mb-3 flex items-center gap-2">
            <DoorClosed className="w-4 h-4 text-teal-600" />
            Gate Traffic Flow (Entry & Exit)
          </h2>
          <div className="space-y-2">
            {gateTraffic.length === 0 ? (
              <p className="text-xs text-text-muted py-4 text-center">No gate traffic recorded.</p>
            ) : (
              gateTraffic.map((g) => (
                <div
                  key={g.gate_name}
                  className="flex items-center justify-between p-2.5 rounded-lg bg-surface border border-border"
                >
                  <span className="font-semibold text-xs text-text-main">{g.gate_name}</span>
                  <div className="flex items-center gap-3 text-xs">
                    <span className="text-[#059669] font-bold">In: {g.entry_count || 0}</span>
                    <span className="text-text-muted font-medium">Out: {g.exit_count || 0}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Grid: Visitor Categories & Busiest Hours */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {/* Category Breakdown */}
        <div className="bg-white border border-border rounded-xl p-5 shadow-xs">
          <h2 className="text-sm font-bold text-text-main mb-3 flex items-center gap-2">
            <Users className="w-4 h-4 text-indigo-600" />
            Visitor Purpose & Categories
          </h2>
          <div className="space-y-2">
            {categories.map((c) => (
              <div
                key={c.category}
                onClick={() => navigateToFilteredVisits('category', c.category)}
                className="flex items-center justify-between p-2.5 rounded-lg bg-surface hover:bg-accent-blue/30 border border-border cursor-pointer transition-colors"
              >
                <span className="font-semibold text-xs text-text-main">{c.category}</span>
                <span className="font-bold text-xs text-indigo-600">{c.count} visits</span>
              </div>
            ))}
          </div>
        </div>

        {/* Busiest Arrival Hours */}
        <div className="bg-white border border-border rounded-xl p-5 shadow-xs">
          <h2 className="text-sm font-bold text-text-main mb-3 flex items-center gap-2">
            <TrendingUp className="w-4 h-4 text-amber-600" />
            Peak Arrival Hours (24-Hour Distribution)
          </h2>
          <div className="space-y-1.5 max-h-64 overflow-y-auto pr-1">
            {busiestHours.length === 0 ? (
              <p className="text-xs text-text-muted py-4 text-center">No hourly data available.</p>
            ) : (
              busiestHours.map((h) => (
                <div key={h.arrival_hour} className="flex items-center justify-between text-xs py-1 px-2 border-b border-border/40">
                  <span className="font-mono text-text-secondary">{String(h.arrival_hour).padStart(2, '0')}:00 - {String(h.arrival_hour).padStart(2, '0')}:59</span>
                  <div className="flex items-center gap-2">
                    <div className="w-24 bg-surface rounded-full h-2 overflow-hidden border border-border">
                      <div
                        className="bg-amber-500 h-full rounded-full"
                        style={{ width: `${Math.min(100, (h.count / 20) * 100)}%` }}
                      />
                    </div>
                    <span className="font-bold text-text-main w-8 text-right">{h.count}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Overdue Visitors Table */}
      {overdueVisits.length > 0 && (
        <div className="bg-white border border-red-200 rounded-xl overflow-hidden shadow-xs">
          <div className="p-4 bg-red-50/70 border-b border-red-200 flex items-center justify-between">
            <div className="flex items-center gap-2 text-red-900 font-bold text-sm">
              <AlertTriangle className="w-4 h-4 text-red-600" />
              <span>Current Overdue Visitors Inside Complex</span>
            </div>
            <span className="text-xs text-red-700 font-semibold">{overdueVisits.length} Active Overstays</span>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface text-text-secondary border-b border-border">
                <tr>
                  <th className="p-3">Reference</th>
                  <th className="p-3">Visitor Name</th>
                  <th className="p-3">Destination</th>
                  <th className="p-3">Check-In Time</th>
                  <th className="p-3">Expected Exit</th>
                  <th className="p-3">Entry Gate</th>
                  <th className="p-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {overdueVisits.map((v) => (
                  <tr key={v.id} className="hover:bg-red-50/30 transition-colors">
                    <td className="p-3 font-mono font-bold text-primary">{v.visit_reference}</td>
                    <td className="p-3 font-semibold text-text-main">{v.visitor_name}</td>
                    <td className="p-3">{v.destination_block_name} - {v.destination_flat_number}</td>
                    <td className="p-3 text-text-muted">{v.check_in_time ? new Date(v.check_in_time).toLocaleString() : '-'}</td>
                    <td className="p-3 text-red-600 font-semibold">{v.arrival_window_end ? new Date(v.arrival_window_end).toLocaleString() : '-'}</td>
                    <td className="p-3">{v.entry_gate_name || '-'}</td>
                    <td className="p-3 text-right">
                      <button
                        onClick={() => navigate(`/visits/${v.id}`)}
                        className="px-2.5 py-1 text-[11px] font-semibold bg-white hover:bg-surface border border-border rounded-md text-primary"
                      >
                        Inspect
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Daily Activity Breakdown Table */}
      <div className="bg-white border border-border rounded-xl overflow-hidden shadow-xs">
        <div className="p-4 bg-surface border-b border-border flex items-center justify-between">
          <h2 className="text-sm font-bold text-text-main">
            Daily Entry & Exit Trend Log (Last 14 Days)
          </h2>
          <span className="text-xs text-text-muted">Filtered by actual timestamp</span>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface text-text-secondary border-b border-border">
              <tr>
                <th className="p-3">Date</th>
                <th className="p-3">Total Admitted (Entries)</th>
                <th className="p-3">Total Departed (Exits)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {dailyTrends.length === 0 ? (
                <tr>
                  <td colSpan={3} className="p-4 text-center text-text-muted">No recorded visits in period.</td>
                </tr>
              ) : (
                dailyTrends.map((d, i) => (
                  <tr key={i} className="hover:bg-surface/50 transition-colors">
                    <td className="p-3 font-semibold text-text-main">{d.visit_date}</td>
                    <td className="p-3 font-bold text-[#059669]">{d.total_entries || 0}</td>
                    <td className="p-3 font-medium text-text-secondary">{d.total_exits || 0}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
