import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiRequest, api } from '@/lib/api';
import { Visit, PagedResponse } from '@/types';
import { DataTable } from '@/components/DataTable';
import { StatusBadge } from '@/components/StatusBadge';
import { Modal } from '@/components/Modal';
import { VisitorFilterBar, FilterState } from '@/components/VisitorFilterBar';
import {
  Calendar,
  Download,
  Eye,
  ShieldAlert,
  Clock,
  Car,
  AlertCircle,
  Edit3,
  CheckCircle2
} from 'lucide-react';
import { format } from 'date-fns';

export const AllVisitsPage: React.FC = () => {
  const navigate = useNavigate();

  const [visits, setVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(15);
  const [error, setError] = useState<string | null>(null);

  // Active Filter state
  const [filterState, setFilterState] = useState<FilterState>({
    dateBasis: 'ACTUAL_CHECK_IN',
    sortBy: 'id',
    sortDirection: 'DESC'
  });

  // Admin Override Modal
  const [overrideModalOpen, setOverrideModalOpen] = useState(false);
  const [selectedVisit, setSelectedVisit] = useState<Visit | null>(null);
  const [overrideReason, setOverrideReason] = useState('');
  const [overriding, setOverriding] = useState(false);

  // Audited Time Correction Modal
  const [correctionModalOpen, setCorrectionModalOpen] = useState(false);
  const [correctionField, setCorrectionField] = useState<'check_in_time' | 'check_out_time'>('check_in_time');
  const [correctedTime, setCorrectedTime] = useState('');
  const [correctionReason, setCorrectionReason] = useState('');
  const [correcting, setCorrecting] = useState(false);

  const loadVisits = async () => {
    try {
      setLoading(true);
      setError(null);

      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('limit', String(limit));

      if (filterState.query) params.set('query', filterState.query);
      if (filterState.blockId) params.set('blockId', filterState.blockId);
      if (filterState.floorNumber) params.set('floorNumber', filterState.floorNumber);
      if (filterState.flatId) params.set('flatId', filterState.flatId);
      if (filterState.entryGateId) params.set('entryGateId', filterState.entryGateId);
      if (filterState.exitGateId) params.set('exitGateId', filterState.exitGateId);
      if (filterState.securityStaffId) params.set('securityStaffId', filterState.securityStaffId);
      if (filterState.status) params.set('status', filterState.status);
      if (filterState.category) params.set('category', filterState.category);
      if (filterState.isWalkIn) params.set('isWalkIn', filterState.isWalkIn);
      if (filterState.isOverdue) params.set('isOverdue', filterState.isOverdue);
      if (filterState.dateBasis) params.set('dateBasis', filterState.dateBasis);
      if (filterState.fromDate) params.set('fromDate', filterState.fromDate);
      if (filterState.toDate) params.set('toDate', filterState.toDate);
      if (filterState.sortBy) params.set('sortBy', filterState.sortBy);
      if (filterState.sortDirection) params.set('sortDirection', filterState.sortDirection);

      const res = await apiRequest<PagedResponse<Visit>>(`visits?${params.toString()}`);
      setVisits(res.items || []);
      setTotal(res.total || 0);
    } catch (err: any) {
      setError(err.message || 'Failed to load visits');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVisits();
  }, [page, limit, filterState]);

  const handleFilterChange = (newFilters: FilterState) => {
    setFilterState(newFilters);
    setPage(1); // Reset to page 1 on filter change
  };

  const handleExportCsv = () => {
    const params = new URLSearchParams();
    if (filterState.query) params.set('query', filterState.query);
    if (filterState.blockId) params.set('blockId', filterState.blockId);
    if (filterState.floorNumber) params.set('floorNumber', filterState.floorNumber);
    if (filterState.flatId) params.set('flatId', filterState.flatId);
    if (filterState.entryGateId) params.set('entryGateId', filterState.entryGateId);
    if (filterState.exitGateId) params.set('exitGateId', filterState.exitGateId);
    if (filterState.status) params.set('status', filterState.status);
    if (filterState.category) params.set('category', filterState.category);
    if (filterState.isWalkIn) params.set('isWalkIn', filterState.isWalkIn);
    if (filterState.isOverdue) params.set('isOverdue', filterState.isOverdue);
    if (filterState.dateBasis) params.set('dateBasis', filterState.dateBasis);
    if (filterState.fromDate) params.set('fromDate', filterState.fromDate);
    if (filterState.toDate) params.set('toDate', filterState.toDate);
    params.set('filtersSummary', `Basis=${filterState.dateBasis || 'ACTUAL_CHECK_IN'}, Status=${filterState.status || 'ALL'}`);

    window.open(`/api/v1/reports/export/visits.csv?${params.toString()}`, '_blank');
  };

  const handleOverrideSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedVisit || !overrideReason.trim()) return;

    try {
      setOverriding(true);
      await apiRequest(`visits/${selectedVisit.id}/override`, {
        method: 'POST',
        body: JSON.stringify({ overrideReason: overrideReason.trim() }),
      });
      setOverrideModalOpen(false);
      setOverrideReason('');
      setSelectedVisit(null);
      loadVisits();
    } catch (err: any) {
      alert(err.message || 'Override failed');
    } finally {
      setOverriding(false);
    }
  };

  const handleCorrectionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedVisit || !correctedTime || !correctionReason.trim()) return;

    try {
      setCorrecting(true);
      await api.post(`/visits/${selectedVisit.id}/correct-times`, {
        fieldName: correctionField,
        correctedTime: new Date(correctedTime).toISOString(),
        reason: correctionReason.trim()
      });
      setCorrectionModalOpen(false);
      setSelectedVisit(null);
      setCorrectionReason('');
      setCorrectedTime('');
      loadVisits();
      alert('Time correction recorded and logged to audit trail.');
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Failed to apply correction');
    } finally {
      setCorrecting(false);
    }
  };

  const columns = [
    {
      header: 'Reference & Visitor',
      render: (v: Visit) => (
        <div>
          <div className="font-mono text-[11px] font-bold text-primary">{v.visitReference}</div>
          <div className="font-bold text-text-main text-xs">{v.visitorName}</div>
          <div className="text-[10px] text-text-muted">{v.visitorMobile}</div>
        </div>
      ),
    },
    {
      header: 'Destination & Host',
      render: (v: Visit) => (
        <div>
          <div className="text-xs font-semibold text-text-main">
            {v.destinationBlockName} - {v.destinationFlatNumber}
          </div>
          <div className="text-[10px] text-text-secondary font-medium">Host: {v.hostNameSnapshot}</div>
        </div>
      ),
    },
    {
      header: 'Category & Purpose',
      render: (v: Visit) => (
        <div>
          <span className="inline-block px-1.5 py-0.5 rounded text-[10px] font-bold bg-accent-blue/30 text-primary uppercase">
            {v.visitorCategory}
          </span>
          <div className="text-[11px] text-text-secondary mt-0.5 max-w-[140px] truncate" title={v.purpose}>
            {v.purpose}
          </div>
        </div>
      ),
    },
    {
      header: 'Status & Gates',
      render: (v: Visit) => (
        <div>
          <div className="flex items-center gap-1.5 flex-wrap">
            <StatusBadge status={v.status} size="sm" />
            {v.overstay && (
              <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-800 border border-amber-300">
                Overdue
              </span>
            )}
          </div>
          {v.entryGateName && (
            <div className="text-[10px] text-text-muted mt-1">
              In: {v.entryGateName} {v.exitGateName ? `• Out: ${v.exitGateName}` : ''}
            </div>
          )}
        </div>
      ),
    },
    {
      header: `Timestamps (Basis: ${filterState.dateBasis || 'CHECK-IN'})`,
      render: (v: Visit) => (
        <div className="text-[11px] space-y-0.5">
          <div className="text-text-secondary flex items-center gap-1">
            <span className="text-[10px] text-text-muted">Sched:</span>
            {v.arrivalWindowStart ? format(new Date(v.arrivalWindowStart), 'dd MMM, HH:mm') : '-'}
          </div>
          {v.checkInTime && (
            <div className="text-[#059669] font-medium flex items-center gap-1">
              <span className="text-[10px] text-text-muted">In:</span>
              {format(new Date(v.checkInTime), 'dd MMM, HH:mm')}
            </div>
          )}
          {v.checkOutTime && (
            <div className="text-text-muted flex items-center gap-1">
              <span className="text-[10px] text-text-muted">Out:</span>
              {format(new Date(v.checkOutTime), 'dd MMM, HH:mm')}
            </div>
          )}
        </div>
      ),
    },
    {
      header: 'Actions',
      align: 'right' as const,
      render: (v: Visit) => (
        <div className="flex items-center justify-end gap-1.5">
          <button
            onClick={() => navigate(`/visits/${v.id}`)}
            className="p-1.5 text-text-muted hover:text-primary hover:bg-surface rounded-lg transition-colors"
            title="View Visit Details & Pass"
          >
            <Eye className="w-4 h-4" />
          </button>

          {(v.status === 'INSIDE' || v.status === 'EXITED') && (
            <button
              onClick={() => {
                setSelectedVisit(v);
                setCorrectionField(v.checkOutTime ? 'check_out_time' : 'check_in_time');
                setCorrectedTime(
                  v.checkOutTime
                    ? new Date(v.checkOutTime).toISOString().slice(0, 16)
                    : v.checkInTime
                    ? new Date(v.checkInTime).toISOString().slice(0, 16)
                    : ''
                );
                setCorrectionModalOpen(true);
              }}
              className="p-1.5 text-text-muted hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
              title="Audited Time Correction"
            >
              <Edit3 className="w-4 h-4" />
            </button>
          )}

          {v.status === 'PENDING_APPROVAL' && (
            <button
              onClick={() => {
                setSelectedVisit(v);
                setOverrideModalOpen(true);
              }}
              className="p-1.5 text-amber-600 hover:bg-amber-50 rounded-lg transition-colors"
              title="Emergency Admin Override"
            >
              <ShieldAlert className="w-4 h-4" />
            </button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white p-5 rounded-xl border border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main tracking-tight">Community Visit Directory</h1>
          <p className="text-xs text-text-secondary mt-0.5">
            Complete database of visitor passes, entry records, approvals, and movement history
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleExportCsv}
            className="px-3 py-2 bg-surface hover:bg-surface-hover text-text-main border border-border rounded-lg text-xs font-semibold flex items-center gap-1.5 transition-colors"
          >
            <Download className="w-3.5 h-3.5" />
            Export Filtered CSV
          </button>
        </div>
      </div>

      {/* Advanced Filter Bar Component */}
      <VisitorFilterBar
        onFilterChange={handleFilterChange}
        showApartmentFilters={true}
        showGateFilters={true}
        defaultDateBasis="ACTUAL_CHECK_IN"
        onExportCsv={handleExportCsv}
      />

      {error && (
        <div className="p-3 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Main Table */}
      <DataTable
        columns={columns}
        data={visits}
        loading={loading}
        emptyTitle="No visitor records match the selected filters."
        total={total}
        page={page}
        limit={limit}
        onPageChange={setPage}
      />

      {/* Admin Override Modal */}
      <Modal
        isOpen={overrideModalOpen}
        onClose={() => setOverrideModalOpen(false)}
        title="Admin Security Override"
      >
        <form onSubmit={handleOverrideSubmit} className="space-y-4">
          <p className="text-xs text-text-secondary">
            You are approving visit <strong className="text-text-main">{selectedVisit?.visitReference}</strong> for{' '}
            <strong className="text-text-main">{selectedVisit?.visitorName}</strong> on behalf of community administration.
          </p>

          <div>
            <label className="block text-xs font-semibold text-text-main mb-1.5">
              Reason for Override / Administrative Justification *
            </label>
            <textarea
              required
              rows={3}
              value={overrideReason}
              onChange={(e) => setOverrideReason(e.target.value)}
              placeholder="e.g. Verified over security intercom with resident"
              className="w-full p-2.5 bg-surface border border-border rounded-xl text-xs outline-none focus:border-primary"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setOverrideModalOpen(false)}
              className="px-4 py-2 bg-surface hover:bg-surface-hover text-text-secondary rounded-xl text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={overriding}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-xl text-xs font-semibold"
            >
              {overriding ? 'Applying...' : 'Confirm Override'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Audited Time Correction Modal */}
      <Modal
        isOpen={correctionModalOpen}
        onClose={() => setCorrectionModalOpen(false)}
        title="Audited Historical Timestamp Correction"
      >
        <form onSubmit={handleCorrectionSubmit} className="space-y-4">
          <div className="p-3 rounded-lg bg-blue-50 text-blue-900 border border-blue-200 text-xs">
            <strong>Audit Policy:</strong> All corrections are permanently logged with your user ID, previous timestamp, new timestamp, and justification.
          </div>

          <div>
            <label className="block text-xs font-semibold text-text-main mb-1.5">Field to Correct</label>
            <select
              value={correctionField}
              onChange={(e) => setCorrectionField(e.target.value as any)}
              className="w-full p-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary font-medium"
            >
              <option value="check_in_time">Check-In Time</option>
              <option value="check_out_time">Check-Out Time</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-text-main mb-1.5">New Corrected Time *</label>
            <input
              type="datetime-local"
              required
              value={correctedTime}
              onChange={(e) => setCorrectedTime(e.target.value)}
              className="w-full p-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-text-main mb-1.5">
              Reason / Justification for Correction *
            </label>
            <textarea
              required
              rows={3}
              value={correctionReason}
              onChange={(e) => setCorrectionReason(e.target.value)}
              placeholder="e.g. Guard recorded check-in with 10-minute delay due to boom gate hardware reboot"
              className="w-full p-2.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setCorrectionModalOpen(false)}
              className="px-4 py-2 bg-surface hover:bg-surface-hover text-text-secondary rounded-lg text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={correcting}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg text-xs font-semibold"
            >
              {correcting ? 'Saving Correction...' : 'Submit Audited Correction'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
