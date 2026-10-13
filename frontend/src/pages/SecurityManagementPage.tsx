import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { User, Gate, PagedResponse } from '@/types';
import { DataTable } from '@/components/DataTable';
import { Modal } from '@/components/Modal';
import { StatusBadge } from '@/components/StatusBadge';
import { Shield, UserPlus, DoorOpen, KeyRound, AlertCircle } from 'lucide-react';
import { format } from 'date-fns';

export const SecurityManagementPage: React.FC = () => {
  const [guards, setGuards] = useState<User[]>([]);
  const [gates, setGates] = useState<Gate[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState('');

  // Add Guard Modal State
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [mobile, setMobile] = useState('');
  const [primaryGateId, setPrimaryGateId] = useState('');
  const [shiftName, setShiftName] = useState('Day Shift (06:00 - 18:00)');
  const [password, setPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [modalError, setModalError] = useState<string | null>(null);

  // Assign Gate Modal State
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [selectedGuard, setSelectedGuard] = useState<User | null>(null);
  const [assignGateId, setAssignGateId] = useState('');
  const [assignShift, setAssignShift] = useState('');

  const loadGates = async () => {
    try {
      const data = await apiRequest<Gate[]>('community/gates');
      setGates(data || []);
      if (data && data.length > 0) {
        setPrimaryGateId(String(data[0].id));
      }
    } catch (err) {}
  };

  const loadGuards = async () => {
    try {
      setLoading(true);
      let queryParams = `page=${page}&limit=10`;
      if (search.trim()) queryParams += `&query=${encodeURIComponent(search.trim())}`;
      const res = await apiRequest<PagedResponse<User>>(`admin/security?${queryParams}`);
      setGuards(res.items || []);
      setTotal(res.total || 0);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadGates();
    loadGuards();
  }, [page]);

  const handleCreateGuard = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setModalError(null);
      await apiRequest('admin/security', {
        method: 'POST',
        body: JSON.stringify({
          fullName,
          email,
          mobile,
          primaryGateId: Number(primaryGateId),
          shiftName,
          password,
        }),
      });
      setAddModalOpen(false);
      setFullName('');
      setEmail('');
      setMobile('');
      setPassword('');
      loadGuards();
    } catch (err: any) {
      setModalError(err.message || 'Failed to create security account');
    } finally {
      setSaving(false);
    }
  };

  const handleAssignSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedGuard || !assignGateId) return;

    try {
      setSaving(true);
      await apiRequest(
        `admin/security/${selectedGuard.id}/assign-gate?gateId=${assignGateId}&shiftName=${encodeURIComponent(
          assignShift
        )}`,
        { method: 'POST' }
      );
      setAssignModalOpen(false);
      loadGuards();
    } catch (err: any) {
      alert(err.message || 'Gate assignment failed');
    } finally {
      setSaving(false);
    }
  };

  const columns = [
    {
      header: 'Officer Name',
      render: (u: User) => (
        <div>
          <div className="font-bold text-text-main">{u.fullName}</div>
          <div className="text-[11px] text-text-muted">{u.email}</div>
        </div>
      ),
    },
    {
      header: 'Mobile',
      render: (u: User) => <span className="font-mono text-xs">{u.mobile}</span>,
    },
    {
      header: 'Assigned Gate',
      render: (u: User) =>
        u.assignedGateName ? (
          <div>
            <strong className="text-primary font-bold">{u.assignedGateName}</strong>
            <div className="text-[10px] text-text-muted">Code: {u.assignedGateCode}</div>
          </div>
        ) : (
          <span className="text-text-muted italic">Unassigned</span>
        ),
    },
    {
      header: 'Account Status',
      render: (u: User) => <StatusBadge status={u.status} size="sm" />,
    },
    {
      header: 'Actions',
      render: (u: User) => (
        <button
          onClick={() => {
            setSelectedGuard(u);
            setAssignGateId(u.assignedGateId ? String(u.assignedGateId) : (gates[0] ? String(gates[0].id) : ''));
            setAssignModalOpen(true);
          }}
          className="px-2.5 py-1 bg-surface hover:bg-primary-light hover:text-primary text-text-main text-xs font-semibold rounded-lg border border-border transition-colors flex items-center gap-1"
        >
          <DoorOpen className="w-3.5 h-3.5" /> Reassign Gate
        </button>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Page Heading */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <Shield className="w-5 h-5 text-primary" />
            Security Personnel & Gate Desk Management
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Create security guard login accounts and assign staff to perimeter and resident gates.
          </p>
        </div>

        <button
          onClick={() => setAddModalOpen(true)}
          className="inline-flex items-center gap-2 px-3.5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors self-start"
        >
          <UserPlus className="w-4 h-4" /> Add Security Staff
        </button>
      </div>

      {/* Guard Table */}
      <DataTable
        columns={columns}
        data={guards}
        loading={loading}
        total={total}
        page={page}
        limit={10}
        onPageChange={setPage}
        searchValue={search}
        onSearchChange={(val) => {
          setSearch(val);
          setPage(1);
          loadGuards();
        }}
        searchPlaceholder="Search security staff by name, email, mobile..."
        emptyTitle="No security accounts found"
        emptyDescription="Click 'Add Security Staff' above to create gate personnel accounts."
      />

      {/* Add Guard Modal */}
      <Modal
        isOpen={addModalOpen}
        onClose={() => setAddModalOpen(false)}
        title="Add Security Officer Account"
      >
        <form onSubmit={handleCreateGuard} className="space-y-3.5 text-xs">
          {modalError && (
            <div className="p-3 bg-status-rejected-bg border border-status-rejected-border rounded-lg text-status-rejected-text flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0" />
              <span>{modalError}</span>
            </div>
          )}

          <div>
            <label className="block font-semibold text-text-main mb-1">Full Name *</label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="E.g. Ramesh Kumar"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">Email *</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="guard.ramesh@community.com"
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
            <div>
              <label className="block font-semibold text-text-main mb-1">Mobile *</label>
              <input
                type="tel"
                required
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                placeholder="+91 98765 43211"
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">Primary Gate Assignment *</label>
              <select
                required
                value={primaryGateId}
                onChange={(e) => setPrimaryGateId(e.target.value)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              >
                {gates.map((g) => (
                  <option key={g.id} value={String(g.id)}>
                    {g.name} ({g.code})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block font-semibold text-text-main mb-1">Shift</label>
              <input
                type="text"
                value={shiftName}
                onChange={(e) => setShiftName(e.target.value)}
                placeholder="Day Shift (06:00 - 18:00)"
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Initial Password *</label>
            <input
              type="password"
              required
              minLength={8}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Temporary password (min 8 chars)"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-border">
            <button
              type="button"
              onClick={() => setAddModalOpen(false)}
              className="px-3 py-1.5 border border-border rounded-lg font-semibold hover:bg-surface"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-1.5 bg-primary text-white font-semibold rounded-lg hover:bg-primary-hover transition-colors disabled:opacity-50"
            >
              {saving ? 'Creating...' : 'Create Account'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Reassign Gate Modal */}
      <Modal
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        title={`Assign Gate for ${selectedGuard?.fullName}`}
      >
        <form onSubmit={handleAssignSubmit} className="space-y-4 text-xs">
          <div>
            <label className="block font-semibold text-text-main mb-1.5">Select Gate *</label>
            <select
              required
              value={assignGateId}
              onChange={(e) => setAssignGateId(e.target.value)}
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            >
              {gates.map((g) => (
                <option key={g.id} value={String(g.id)}>
                  {g.name} ({g.code}) - {g.gateType}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1.5">Shift Name</label>
            <input
              type="text"
              value={assignShift}
              onChange={(e) => setAssignShift(e.target.value)}
              placeholder="Night Shift (18:00 - 06:00)"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-border">
            <button
              type="button"
              onClick={() => setAssignModalOpen(false)}
              className="px-3 py-1.5 border border-border rounded-lg font-semibold hover:bg-surface"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-1.5 bg-primary text-white font-semibold rounded-lg hover:bg-primary-hover transition-colors"
            >
              Update Assignment
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
