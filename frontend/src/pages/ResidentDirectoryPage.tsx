import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { ResidentProfile, Block, VerificationStatus, PagedResponse } from '@/types';
import { DataTable } from '@/components/DataTable';
import { StatusBadge } from '@/components/StatusBadge';
import { Users, Filter, UserX, UserCheck, ShieldAlert } from 'lucide-react';

export const ResidentDirectoryPage: React.FC = () => {
  const [residents, setResidents] = useState<ResidentProfile[]>([]);
  const [blocks, setBlocks] = useState<Block[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);

  // Filters
  const [search, setSearch] = useState('');
  const [selectedBlock, setSelectedBlock] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');

  const loadBlocks = async () => {
    try {
      const data = await apiRequest<Block[]>('community/blocks');
      setBlocks(data || []);
    } catch (err) {}
  };

  const loadResidents = async () => {
    try {
      setLoading(true);
      let queryParams = `page=${page}&limit=12`;
      if (search.trim()) queryParams += `&query=${encodeURIComponent(search.trim())}`;
      if (selectedBlock) queryParams += `&blockId=${selectedBlock}`;
      if (selectedStatus) queryParams += `&status=${selectedStatus}`;

      const res = await apiRequest<PagedResponse<ResidentProfile>>(`admin/residents?${queryParams}`);
      setResidents(res.items || []);
      setTotal(res.total || 0);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBlocks();
  }, []);

  useEffect(() => {
    loadResidents();
  }, [page, selectedBlock, selectedStatus]);

  const handleSearchSubmit = (val: string) => {
    setSearch(val);
    setPage(1);
    // Debounced or direct
    const timeout = setTimeout(() => {
      loadResidents();
    }, 300);
    return () => clearTimeout(timeout);
  };

  const handleToggleStatus = async (userId: number, currentStatus: string) => {
    const newStatus = currentStatus === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED';
    if (!window.confirm(`Are you sure you want to set this resident account to ${newStatus}?`)) {
      return;
    }
    try {
      await apiRequest(`admin/residents/${userId}/status?status=${newStatus}`, { method: 'PUT' });
      loadResidents();
    } catch (err: any) {
      alert(err.message || 'Status change failed');
    }
  };

  const columns = [
    {
      header: 'Resident Name',
      render: (p: ResidentProfile) => (
        <div>
          <div className="font-bold text-text-main">{p.userFullName}</div>
          <div className="text-[11px] text-text-muted">{p.userEmail}</div>
        </div>
      ),
    },
    {
      header: 'Mobile',
      render: (p: ResidentProfile) => (
        <span className="font-mono text-xs">{p.userMobile}</span>
      ),
    },
    {
      header: 'Flat Allocation',
      render: (p: ResidentProfile) => (
        <div>
          <strong className="text-primary font-bold">
            {p.blockName} — Flat {p.flatNumber}
          </strong>
          <div className="text-[10px] text-text-muted">Floor {p.floorNumber}</div>
        </div>
      ),
    },
    {
      header: 'Occupancy',
      render: (p: ResidentProfile) => (
        <span className="text-[11px] font-semibold text-text-secondary bg-surface px-2 py-0.5 rounded border border-border">
          {p.occupancyType}
        </span>
      ),
    },
    {
      header: 'Verification Status',
      render: (p: ResidentProfile) => (
        <StatusBadge status={p.verificationStatus} size="sm" />
      ),
    },
    {
      header: 'Actions',
      render: (p: ResidentProfile) => (
        <div className="flex items-center gap-2">
          {p.verificationStatus === 'APPROVED' && (
            <button
              onClick={() => handleToggleStatus(p.userId, 'ACTIVE')}
              className="px-2 py-1 bg-surface hover:bg-border text-text-secondary rounded text-[11px] font-semibold border border-border flex items-center gap-1 transition-colors"
              title="Toggle Account Suspension"
            >
              <UserX className="w-3 h-3 text-status-rejected-text" /> Suspend
            </button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <Users className="w-5 h-5 text-primary" />
            Resident Directory
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Authoritative directory of registered owners, tenants, and their assigned apartment flats.
          </p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="flex flex-wrap items-center gap-3 p-3 bg-surface rounded-xl border border-border text-xs">
        <div className="flex items-center gap-1.5 text-text-secondary font-semibold">
          <Filter className="w-3.5 h-3.5 text-primary" /> Filter by:
        </div>

        <select
          value={selectedBlock}
          onChange={(e) => {
            setSelectedBlock(e.target.value);
            setPage(1);
          }}
          className="px-3 py-1.5 bg-white border border-border rounded-lg text-xs outline-none focus:border-primary"
        >
          <option value="">All Blocks</option>
          {blocks.map((b) => (
            <option key={b.id} value={String(b.id)}>
              {b.name}
            </option>
          ))}
        </select>

        <select
          value={selectedStatus}
          onChange={(e) => {
            setSelectedStatus(e.target.value);
            setPage(1);
          }}
          className="px-3 py-1.5 bg-white border border-border rounded-lg text-xs outline-none focus:border-primary"
        >
          <option value="">All Statuses</option>
          <option value="APPROVED">Approved</option>
          <option value="PENDING">Pending</option>
          <option value="REJECTED">Rejected</option>
        </select>
      </div>

      {/* Resident Table */}
      <DataTable
        columns={columns}
        data={residents}
        loading={loading}
        total={total}
        page={page}
        limit={12}
        onPageChange={setPage}
        searchValue={search}
        onSearchChange={handleSearchSubmit}
        searchPlaceholder="Search by resident name, flat number, email, mobile..."
        emptyTitle="No residents found"
        emptyDescription="Try adjusting your block or verification status filter."
      />
    </div>
  );
};
