import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { Visit, PagedResponse } from '@/types';
import { DataTable } from '@/components/DataTable';
import { StatusBadge } from '@/components/StatusBadge';
import { History, Download, Eye } from 'lucide-react';
import { Link } from 'react-router-dom';
import { format } from 'date-fns';

export const VisitorHistoryPage: React.FC = () => {
  const [visits, setVisits] = useState<Visit[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');

  const loadHistory = async () => {
    try {
      setLoading(true);
      let queryParams = `page=${page}&limit=12`;
      if (search.trim()) queryParams += `&query=${encodeURIComponent(search.trim())}`;
      const res = await apiRequest<PagedResponse<Visit>>(`visits?${queryParams}`);
      setVisits(res.items || []);
      setTotal(res.total || 0);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, [page]);

  const handleExport = () => {
    let exportUrl = '/api/v1/visits/export?';
    if (search.trim()) exportUrl += `query=${encodeURIComponent(search.trim())}&`;
    window.open(exportUrl, '_blank');
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
      header: 'Category & Purpose',
      render: (v: Visit) => (
        <div>
          <span className="text-[10px] font-semibold bg-surface px-1.5 py-0.5 rounded border border-border">
            {v.visitorCategory}
          </span>
          <div className="text-xs text-text-secondary mt-0.5 max-w-[160px] truncate">{v.purpose}</div>
        </div>
      ),
    },
    {
      header: 'Destination Flat',
      render: (v: Visit) => (
        <div>
          <strong className="text-text-main font-bold">
            {v.destinationBlockName} — {v.destinationFlatNumber}
          </strong>
          <div className="text-[10px] text-text-muted">Host: {v.hostNameSnapshot}</div>
        </div>
      ),
    },
    {
      header: 'Check-In / Out',
      render: (v: Visit) => (
        <div className="text-[11px] space-y-0.5">
          <div className="text-[#137547] font-medium">
            In: {v.checkInTime ? format(new Date(v.checkInTime), 'dd MMM, hh:mm a') : '—'}
          </div>
          <div className="text-text-secondary">
            Out: {v.checkOutTime ? format(new Date(v.checkOutTime), 'dd MMM, hh:mm a') : '—'}
          </div>
        </div>
      ),
    },
    {
      header: 'Status',
      render: (v: Visit) => <StatusBadge status={v.status} size="sm" />,
    },
    {
      header: 'Actions',
      render: (v: Visit) => (
        <Link
          to={`/visits/${v.id}`}
          className="p-1.5 bg-surface hover:bg-border rounded-lg text-text-secondary hover:text-text-main inline-block"
          title="View Details"
        >
          <Eye className="w-4 h-4" />
        </Link>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <History className="w-5 h-5 text-primary" />
            Gate Visitor Logs & Historical Records
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Search historical admissions, entry gate timestamps, and exit verification records.
          </p>
        </div>

        <button
          onClick={handleExport}
          className="inline-flex items-center gap-2 px-3.5 py-2 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors self-start shadow-sm"
        >
          <Download className="w-4 h-4 text-primary" /> Export CSV
        </button>
      </div>

      <DataTable
        columns={columns}
        data={visits}
        loading={loading}
        total={total}
        page={page}
        limit={12}
        onPageChange={setPage}
        searchValue={search}
        onSearchChange={(val) => {
          setSearch(val);
          setPage(1);
          loadHistory();
        }}
        searchPlaceholder="Search visitor history..."
        emptyTitle="No visitor records found"
      />
    </div>
  );
};
