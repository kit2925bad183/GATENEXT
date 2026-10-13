import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { AuditLog, PagedResponse } from '@/types';
import { DataTable } from '@/components/DataTable';
import { FileSpreadsheet, ShieldAlert, Terminal } from 'lucide-react';
import { format } from 'date-fns';

export const AuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');

  const loadLogs = async () => {
    try {
      setLoading(true);
      let queryParams = `page=${page}&limit=15`;
      if (search.trim()) queryParams += `&query=${encodeURIComponent(search.trim())}`;
      const res = await apiRequest<PagedResponse<AuditLog>>(`admin/audit-logs?${queryParams}`);
      setLogs(res.items || []);
      setTotal(res.total || 0);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLogs();
  }, [page]);

  const columns = [
    {
      header: 'Timestamp',
      render: (a: AuditLog) => (
        <span className="font-mono text-[11px] text-text-secondary">
          {format(new Date(a.createdAt), 'dd MMM yyyy • HH:mm:ss')}
        </span>
      ),
    },
    {
      header: 'Actor & Role',
      render: (a: AuditLog) => (
        <div>
          <div className="font-bold text-text-main text-xs">{a.actorEmail || 'System'}</div>
          <span className="text-[10px] font-semibold text-primary bg-primary-light px-1.5 py-0.2 rounded">
            {a.actorRole || 'SYSTEM'}
          </span>
        </div>
      ),
    },
    {
      header: 'Action Performed',
      render: (a: AuditLog) => (
        <span className="font-mono text-xs font-bold text-text-main bg-surface px-2 py-0.5 rounded border border-border">
          {a.action}
        </span>
      ),
    },
    {
      header: 'Target Entity',
      render: (a: AuditLog) => (
        <span className="text-xs text-text-secondary">
          {a.entityType} {a.entityId ? `#${a.entityId}` : ''}
        </span>
      ),
    },
    {
      header: 'IP Address',
      render: (a: AuditLog) => (
        <span className="font-mono text-[11px] text-text-muted">{a.ipAddress || '—'}</span>
      ),
    },
    {
      header: 'Details',
      render: (a: AuditLog) => (
        <div className="max-w-xs truncate text-[11px] font-mono text-text-muted bg-surface/70 px-2 py-1 rounded border border-border-light">
          {a.detailsJson || '—'}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <FileSpreadsheet className="w-5 h-5 text-primary" />
            Security & System Audit Trail
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Immutable system logs tracking admissions, check-in operations, administrative overrides, and account modifications.
          </p>
        </div>
      </div>

      <DataTable
        columns={columns}
        data={logs}
        loading={loading}
        total={total}
        page={page}
        limit={15}
        onPageChange={setPage}
        searchValue={search}
        onSearchChange={(val) => {
          setSearch(val);
          setPage(1);
          loadLogs();
        }}
        searchPlaceholder="Search audit events by action, user email, entity ID..."
        emptyTitle="No audit records found"
      />
    </div>
  );
};
