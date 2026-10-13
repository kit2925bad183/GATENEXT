import React, { useState, useEffect } from 'react';
import { api } from '@/lib/api';
import { UserDto, Role, UserStatus, PagedResponse } from '@/types';
import { DataTable } from '@/components/DataTable';
import { Modal } from '@/components/Modal';
import {
  Users,
  UserPlus,
  Shield,
  Key,
  Lock,
  Copy,
  Check,
  RotateCcw,
  Search,
  Filter,
  AlertTriangle,
  MoveRight,
  History,
  Building,
  DoorClosed,
  CheckCircle2,
  XCircle,
  AlertCircle
} from 'lucide-react';
import { format } from 'date-fns';

export const UserManagementPage: React.FC = () => {
  const [users, setUsers] = useState<UserDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(15);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('');

  // Dropdown metadata
  const [blocks, setBlocks] = useState<Array<{ id: number; name: string }>>([]);
  const [flats, setFlats] = useState<Array<{ id: number; blockId: number; flatNumber: string }>>([]);
  const [gates, setGates] = useState<Array<{ id: number; name: string; code: string }>>([]);

  // Create User Modal
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [createRole, setCreateRole] = useState<Role>('RESIDENT');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [mobile, setMobile] = useState('');
  const [selectedBlockId, setSelectedBlockId] = useState<string>('');
  const [selectedFlatId, setSelectedFlatId] = useState<string>('');
  const [selectedGateId, setSelectedGateId] = useState<string>('');
  const [shiftName, setShiftName] = useState('Morning Shift');
  const [occupancyType, setOccupancyType] = useState('OWNER');
  const [adminPasswordVerification, setAdminPasswordVerification] = useState('');
  const [creating, setCreating] = useState(false);

  // Activation Link Modal
  const [activationModalOpen, setActivationModalOpen] = useState(false);
  const [createdActivationLink, setCreatedActivationLink] = useState('');
  const [createdUserEmail, setCreatedUserEmail] = useState('');
  const [copied, setCopied] = useState(false);

  // Change Role Modal
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [targetUser, setTargetUser] = useState<UserDto | null>(null);
  const [newRole, setNewRole] = useState<Role>('RESIDENT');
  const [roleChangeReason, setRoleChangeReason] = useState('');
  const [roleAdminPassword, setRoleAdminPassword] = useState('');
  const [updatingRole, setUpdatingRole] = useState(false);

  // Change Status Modal
  const [statusModalOpen, setStatusModalOpen] = useState(false);
  const [newStatus, setNewStatus] = useState<string>('ACTIVE');
  const [statusReason, setStatusReason] = useState('');
  const [statusAdminPassword, setStatusAdminPassword] = useState('');
  const [updatingStatus, setUpdatingStatus] = useState(false);

  // Resident Move Flat Modal
  const [moveModalOpen, setMoveModalOpen] = useState(false);
  const [moveBlockId, setMoveBlockId] = useState<string>('');
  const [moveFlatId, setMoveFlatId] = useState<string>('');
  const [moveReason, setMoveReason] = useState('');
  const [movingFlat, setMovingFlat] = useState(false);

  // Move History Modal
  const [historyModalOpen, setHistoryModalOpen] = useState(false);
  const [moveHistory, setMoveHistory] = useState<Array<any>>([]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('limit', String(limit));
      if (searchQuery.trim()) params.set('query', searchQuery.trim());
      if (roleFilter) params.set('role', roleFilter);
      if (statusFilter) params.set('status', statusFilter);

      const res = await api.get<{ data: PagedResponse<UserDto> }>(`/admin/users?${params.toString()}`);
      setUsers(res.data?.data?.items || []);
      setTotal(res.data?.data?.total || 0);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load user management directory');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, [page, limit, roleFilter, statusFilter]);

  // Load Blocks, Flats, Gates
  useEffect(() => {
    const loadMetadata = async () => {
      try {
        const [bRes, fRes, gRes] = await Promise.all([
          api.get<{ data: any[] }>('/community/blocks'),
          api.get<{ data: any[] }>('/community/flats'),
          api.get<{ data: any[] }>('/community/gates')
        ]);
        setBlocks(bRes.data?.data || []);
        setFlats(fRes.data?.data || []);
        setGates(gRes.data?.data || []);
      } catch (err) {
        console.error('Failed to load metadata', err);
      }
    };
    loadMetadata();
  }, []);

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setCreating(true);
      const res = await api.post<{ data: { activationLink: string; user: UserDto } }>('/admin/users', {
        fullName: fullName.trim(),
        email: email.trim(),
        mobile: mobile.trim(),
        role: createRole,
        flatId: createRole === 'RESIDENT' && selectedFlatId ? Number(selectedFlatId) : null,
        occupancyType: createRole === 'RESIDENT' ? occupancyType : null,
        gateId: createRole === 'SECURITY' && selectedGateId ? Number(selectedGateId) : null,
        shiftName: createRole === 'SECURITY' ? shiftName : null,
        adminPasswordVerification: createRole === 'ADMINISTRATOR' ? adminPasswordVerification : null
      });

      setCreateModalOpen(false);
      setCreatedActivationLink(window.location.origin + res.data?.data?.activationLink);
      setCreatedUserEmail(email.trim());
      setActivationModalOpen(true);

      // Reset form
      setFullName('');
      setEmail('');
      setMobile('');
      setSelectedFlatId('');
      setSelectedGateId('');
      setAdminPasswordVerification('');
      loadUsers();
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || 'Failed to create user account');
    } finally {
      setCreating(false);
    }
  };

  const handleRegenerateActivation = async (userId: number, userEmail: string) => {
    try {
      const res = await api.post<{ data: { activationLink: string } }>(`/admin/users/${userId}/regenerate-activation`);
      setCreatedActivationLink(window.location.origin + res.data?.data?.activationLink);
      setCreatedUserEmail(userEmail);
      setActivationModalOpen(true);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to regenerate activation link');
    }
  };

  const handleRoleChangeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetUser || !roleAdminPassword.trim() || !roleChangeReason.trim()) return;

    try {
      setUpdatingRole(true);
      await api.post(`/admin/users/${targetUser.id}/role`, {
        newRole,
        reason: roleChangeReason.trim(),
        adminPasswordVerification: roleAdminPassword.trim()
      });
      setRoleModalOpen(false);
      setTargetUser(null);
      setRoleAdminPassword('');
      setRoleChangeReason('');
      loadUsers();
      alert('User role updated successfully.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update user role');
    } finally {
      setUpdatingRole(false);
    }
  };

  const handleStatusChangeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetUser) return;

    try {
      setUpdatingStatus(true);
      await api.post(`/admin/users/${targetUser.id}/status`, {
        newStatus,
        reason: statusReason.trim(),
        adminPasswordVerification: statusAdminPassword.trim()
      });
      setStatusModalOpen(false);
      setTargetUser(null);
      setStatusReason('');
      setStatusAdminPassword('');
      loadUsers();
      alert('User status updated successfully.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update user status');
    } finally {
      setUpdatingStatus(false);
    }
  };

  const handleMoveFlatSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetUser || !moveFlatId) return;

    try {
      setMovingFlat(true);
      await api.post(`/admin/users/${targetUser.id}/move-flat`, {
        newFlatId: Number(moveFlatId),
        reason: moveReason.trim()
      });
      setMoveModalOpen(false);
      setTargetUser(null);
      setMoveFlatId('');
      setMoveReason('');
      loadUsers();
      alert('Resident relocated successfully. Affected future invitations have been cancelled.');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to relocate resident');
    } finally {
      setMovingFlat(false);
    }
  };

  const handleViewMoveHistory = async (userId: number) => {
    try {
      const res = await api.get<{ data: any[] }>(`/admin/users/${userId}/move-history`);
      setMoveHistory(res.data?.data || []);
      setHistoryModalOpen(true);
    } catch (err: any) {
      alert('Failed to load resident movement history');
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(createdActivationLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const columns = [
    {
      header: 'User & Contact',
      render: (u: UserDto) => (
        <div>
          <div className="font-bold text-xs text-text-main">{u.fullName}</div>
          <div className="text-[11px] text-text-muted">{u.email}</div>
          <div className="text-[10px] text-text-secondary font-mono">{u.mobile}</div>
        </div>
      ),
    },
    {
      header: 'Role & Assignment',
      render: (u: UserDto) => (
        <div>
          <span className={`inline-block px-2 py-0.5 rounded text-[10px] font-bold ${
            u.role === 'ADMINISTRATOR' ? 'bg-purple-100 text-purple-800' :
            u.role === 'SECURITY' ? 'bg-amber-100 text-amber-800' : 'bg-blue-100 text-blue-800'
          }`}>
            {u.role}
          </span>
          {u.flatNumber && (
            <div className="text-[11px] text-text-main font-semibold mt-0.5">
              {u.blockName} - Flat {u.flatNumber}
            </div>
          )}
          {u.assignedGateName && (
            <div className="text-[10px] text-text-muted mt-0.5">
              Gate: {u.assignedGateName}
            </div>
          )}
        </div>
      ),
    },
    {
      header: 'Status',
      render: (u: UserDto) => (
        <div>
          <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold ${
            u.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
            u.status === 'PENDING' ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'
          }`}>
            {u.status === 'ACTIVE' ? <CheckCircle2 className="w-3 h-3 text-green-600" /> : <XCircle className="w-3 h-3" />}
            {u.status}
          </span>
        </div>
      ),
    },
    {
      header: 'Activity & Created',
      render: (u: UserDto) => (
        <div className="text-[11px] text-text-secondary space-y-0.5">
          <div>Created: {u.createdAt ? format(new Date(u.createdAt), 'dd MMM yyyy') : '-'}</div>
          <div className="text-[10px] text-text-muted">
            Last Login: {u.lastLoginAt ? format(new Date(u.lastLoginAt), 'dd MMM, HH:mm') : 'Never'}
          </div>
        </div>
      ),
    },
    {
      header: 'Management Actions',
      align: 'right' as const,
      render: (u: UserDto) => (
        <div className="flex items-center justify-end gap-1.5">
          {u.status === 'PENDING' && (
            <button
              onClick={() => handleRegenerateActivation(u.id, u.email)}
              className="px-2 py-1 text-[11px] font-semibold bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 rounded-md"
              title="Copy / Regenerate Activation Link"
            >
              Activation Link
            </button>
          )}

          <button
            onClick={() => {
              setTargetUser(u);
              setNewRole(u.role);
              setRoleModalOpen(true);
            }}
            className="px-2 py-1 text-[11px] font-semibold bg-surface hover:bg-surface-hover border border-border rounded-md text-text-main"
            title="Change User Role"
          >
            Role
          </button>

          <button
            onClick={() => {
              setTargetUser(u);
              setNewStatus(u.status);
              setStatusModalOpen(true);
            }}
            className="px-2 py-1 text-[11px] font-semibold bg-surface hover:bg-surface-hover border border-border rounded-md text-text-main"
            title="Change Account Status"
          >
            Status
          </button>

          {u.role === 'RESIDENT' && (
            <>
              <button
                onClick={() => {
                  setTargetUser(u);
                  setMoveModalOpen(true);
                }}
                className="px-2 py-1 text-[11px] font-semibold bg-teal-50 hover:bg-teal-100 text-teal-800 border border-teal-200 rounded-md"
                title="Relocate Resident Flat"
              >
                Move Flat
              </button>
              <button
                onClick={() => handleViewMoveHistory(u.id)}
                className="p-1 text-text-muted hover:text-text-main rounded"
                title="View Move History"
              >
                <History className="w-3.5 h-3.5" />
              </button>
            </>
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
          <h1 className="text-xl font-bold text-text-main tracking-tight">Account & User Management</h1>
          <p className="text-xs text-text-secondary mt-0.5">
            Administer resident credentials, security staff rosters, administrator privileges, and activations
          </p>
        </div>

        <button
          onClick={() => setCreateModalOpen(true)}
          className="px-3.5 py-2 bg-[#1E40AF] hover:bg-[#1D4ED8] text-white rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-xs transition-colors"
        >
          <UserPlus className="w-4 h-4" />
          Create New Account
        </button>
      </div>

      {/* Filter Toolbar */}
      <div className="bg-white border border-border rounded-xl p-4 flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && loadUsers()}
            placeholder="Search by name, email, or mobile..."
            className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg text-xs focus:bg-white focus:border-primary outline-none"
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="px-3 py-2 bg-surface border border-border rounded-lg text-xs font-medium outline-none"
          >
            <option value="">All Roles</option>
            <option value="ADMINISTRATOR">Administrator</option>
            <option value="SECURITY">Security</option>
            <option value="RESIDENT">Resident</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-2 bg-surface border border-border rounded-lg text-xs font-medium outline-none"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="PENDING">Pending Activation</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="DEACTIVATED">Deactivated</option>
          </select>

          <button
            onClick={loadUsers}
            className="px-3 py-2 bg-surface hover:bg-surface-hover border border-border rounded-lg text-xs font-semibold"
          >
            Filter
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* User Data Table */}
      <DataTable
        columns={columns}
        data={users}
        loading={loading}
        emptyTitle="No user accounts found matching your search."
        total={total}
        page={page}
        limit={limit}
        onPageChange={setPage}
      />

      {/* Create User Modal */}
      <Modal
        isOpen={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Create Authorized Community Account"
      >
        <form onSubmit={handleCreateSubmit} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-semibold text-text-main mb-1">Account Role *</label>
            <div className="grid grid-cols-3 gap-2">
              {(['RESIDENT', 'SECURITY', 'ADMINISTRATOR'] as Role[]).map((r) => (
                <button
                  type="button"
                  key={r}
                  onClick={() => setCreateRole(r)}
                  className={`py-2 px-3 rounded-lg border font-bold text-xs transition-all ${
                    createRole === r
                      ? 'bg-blue-50 border-primary text-primary'
                      : 'bg-surface border-border text-text-secondary hover:bg-surface-hover'
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Full Legal Name *</label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="e.g. Ramesh Kumar"
              className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
            />
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="block font-semibold text-text-main mb-1">Email Address *</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="ramesh@community.com"
                className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
              />
            </div>
            <div>
              <label className="block font-semibold text-text-main mb-1">Mobile Number *</label>
              <input
                type="tel"
                required
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                placeholder="+91 9876543210"
                className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
              />
            </div>
          </div>

          {createRole === 'RESIDENT' && (
            <>
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="block font-semibold text-text-main mb-1">Block Assignment *</label>
                  <select
                    value={selectedBlockId}
                    onChange={(e) => {
                      setSelectedBlockId(e.target.value);
                      setSelectedFlatId('');
                    }}
                    required
                    className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
                  >
                    <option value="">Select Block</option>
                    {blocks.map(b => (
                      <option key={b.id} value={String(b.id)}>{b.name}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block font-semibold text-text-main mb-1">Flat Assignment *</label>
                  <select
                    value={selectedFlatId}
                    onChange={(e) => setSelectedFlatId(e.target.value)}
                    required
                    disabled={!selectedBlockId}
                    className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary disabled:opacity-50"
                  >
                    <option value="">Select Flat</option>
                    {flats
                      .filter(f => !selectedBlockId || String(f.blockId) === selectedBlockId)
                      .map(f => (
                        <option key={f.id} value={String(f.id)}>Flat {f.flatNumber}</option>
                      ))}
                  </select>
                </div>
              </div>
            </>
          )}

          {createRole === 'SECURITY' && (
            <div className="grid grid-cols-2 gap-2.5">
              <div>
                <label className="block font-semibold text-text-main mb-1">Assigned Gate</label>
                <select
                  value={selectedGateId}
                  onChange={(e) => setSelectedGateId(e.target.value)}
                  className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
                >
                  <option value="">All / Floating Guard</option>
                  {gates.map(g => (
                    <option key={g.id} value={String(g.id)}>{g.name} ({g.code})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block font-semibold text-text-main mb-1">Shift Name</label>
                <input
                  type="text"
                  value={shiftName}
                  onChange={(e) => setShiftName(e.target.value)}
                  placeholder="Morning Shift"
                  className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
                />
              </div>
            </div>
          )}

          {createRole === 'ADMINISTRATOR' && (
            <div className="p-3 bg-purple-50 border border-purple-200 rounded-lg space-y-2">
              <div className="font-bold text-purple-900 flex items-center gap-1.5">
                <Lock className="w-3.5 h-3.5" />
                Sensitive Operation: Administrator Reauthentication Required
              </div>
              <input
                type="password"
                required
                value={adminPasswordVerification}
                onChange={(e) => setAdminPasswordVerification(e.target.value)}
                placeholder="Enter your current administrator password to confirm"
                className="w-full p-2 bg-white border border-purple-300 rounded-lg outline-none focus:border-purple-600"
              />
            </div>
          )}

          <div className="flex justify-end gap-2 pt-3 border-t border-border">
            <button
              type="button"
              onClick={() => setCreateModalOpen(false)}
              className="px-4 py-2 bg-surface hover:bg-surface-hover text-text-secondary rounded-lg font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={creating}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg font-semibold"
            >
              {creating ? 'Generating Account...' : 'Create Account & Generate Link'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Activation Link Modal */}
      <Modal
        isOpen={activationModalOpen}
        onClose={() => setActivationModalOpen(false)}
        title="Account Activation Link Generated"
      >
        <div className="space-y-4 text-xs">
          <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg text-blue-950">
            <p className="font-semibold">Single-Use Account Activation Link</p>
            <p className="text-[11px] text-blue-800 mt-1">
              Email delivery is configured for manual handover. Copy and send this secure activation link to{' '}
              <strong>{createdUserEmail}</strong>. The user will use it to set their password.
            </p>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Activation Link (Expires in 48 hours)</label>
            <div className="flex items-center gap-2">
              <input
                type="text"
                readOnly
                value={createdActivationLink}
                className="w-full p-2 bg-surface border border-border rounded-lg font-mono text-[11px] text-text-main select-all"
              />
              <button
                type="button"
                onClick={copyToClipboard}
                className="px-3 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg font-semibold flex items-center gap-1 flex-shrink-0"
              >
                {copied ? <Check className="w-4 h-4" /> : <Copy className="w-4 h-4" />}
                {copied ? 'Copied' : 'Copy'}
              </button>
            </div>
          </div>

          <div className="flex justify-end pt-2">
            <button
              type="button"
              onClick={() => setActivationModalOpen(false)}
              className="px-4 py-2 bg-surface hover:bg-surface-hover text-text-main rounded-lg font-semibold"
            >
              Done
            </button>
          </div>
        </div>
      </Modal>

      {/* Change Role Modal */}
      <Modal
        isOpen={roleModalOpen}
        onClose={() => setRoleModalOpen(false)}
        title="Change User Role Permissions"
      >
        <form onSubmit={handleRoleChangeSubmit} className="space-y-3.5 text-xs">
          <div className="p-2.5 rounded-lg bg-amber-50 border border-amber-200 text-amber-950 font-medium">
            Changing role for <strong>{targetUser?.fullName}</strong> ({targetUser?.email}). Active sessions will be automatically revoked.
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Target Role *</label>
            <select
              value={newRole}
              onChange={(e) => setNewRole(e.target.value as Role)}
              className="w-full p-2 bg-surface border border-border rounded-lg font-semibold"
            >
              <option value="RESIDENT">RESIDENT</option>
              <option value="SECURITY">SECURITY</option>
              <option value="ADMINISTRATOR">ADMINISTRATOR</option>
            </select>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Reason for Role Change *</label>
            <textarea
              required
              rows={2}
              value={roleChangeReason}
              onChange={(e) => setRoleChangeReason(e.target.value)}
              placeholder="e.g. Promoted to security supervisor"
              className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
            />
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Your Administrator Password *</label>
            <input
              type="password"
              required
              value={roleAdminPassword}
              onChange={(e) => setRoleAdminPassword(e.target.value)}
              placeholder="Confirm with your password"
              className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setRoleModalOpen(false)}
              className="px-4 py-2 bg-surface hover:bg-surface-hover text-text-secondary rounded-lg font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={updatingRole}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg font-semibold"
            >
              {updatingRole ? 'Updating...' : 'Confirm Role Change'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Change Status Modal */}
      <Modal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        title="Update Account Security Status"
      >
        <form onSubmit={handleStatusChangeSubmit} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-semibold text-text-main mb-1">Account Status *</label>
            <select
              value={newStatus}
              onChange={(e) => setNewStatus(e.target.value)}
              className="w-full p-2 bg-surface border border-border rounded-lg font-semibold"
            >
              <option value="ACTIVE">ACTIVE</option>
              <option value="SUSPENDED">SUSPENDED</option>
              <option value="DEACTIVATED">DEACTIVATED</option>
            </select>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Reason for Status Change</label>
            <textarea
              rows={2}
              value={statusReason}
              onChange={(e) => setStatusReason(e.target.value)}
              placeholder="e.g. Flat lease completed"
              className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
            />
          </div>

          {targetUser?.role === 'ADMINISTRATOR' && (
            <div>
              <label className="block font-semibold text-text-main mb-1">Admin Password Verification *</label>
              <input
                type="password"
                required
                value={statusAdminPassword}
                onChange={(e) => setStatusAdminPassword(e.target.value)}
                placeholder="Enter your administrator password"
                className="w-full p-2 bg-surface border border-border rounded-lg outline-none focus:border-primary"
              />
            </div>
          )}

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setStatusModalOpen(false)}
              className="px-4 py-2 bg-surface hover:bg-surface-hover text-text-secondary rounded-lg font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={updatingStatus}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white rounded-lg font-semibold"
            >
              {updatingStatus ? 'Saving...' : 'Apply Status'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Resident Move Flat Modal */}
      <Modal
        isOpen={moveModalOpen}
        onClose={() => setMoveModalOpen(false)}
        title="Relocate Resident to New Flat"
      >
        <form onSubmit={handleMoveFlatSubmit} className="space-y-3.5 text-xs">
          <div className="p-2.5 rounded-lg bg-yellow-50 border border-yellow-200 text-yellow-950 font-medium">
            <strong>Notice:</strong> Relocating resident <strong>{targetUser?.fullName}</strong> will preserve past visit history while cancelling future invitations scheduled for the old flat.
          </div>

          <div className="grid grid-cols-2 gap-2.5">
            <div>
              <label className="block font-semibold text-text-main mb-1">New Block *</label>
              <select
                value={moveBlockId}
                onChange={(e) => {
                  setMoveBlockId(e.target.value);
                  setMoveFlatId('');
                }}
                required
                className="w-full p-2 bg-surface border border-border rounded-lg"
              >
                <option value="">Select Block</option>
                {blocks.map(b => (
                  <option key={b.id} value={String(b.id)}>{b.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block font-semibold text-text-main mb-1">New Flat *</label>
              <select
                value={moveFlatId}
                onChange={(e) => setMoveFlatId(e.target.value)}
                required
                disabled={!moveBlockId}
                className="w-full p-2 bg-surface border border-border rounded-lg disabled:opacity-50"
              >
                <option value="">Select Flat</option>
                {flats
                  .filter(f => !moveBlockId || String(f.blockId) === moveBlockId)
                  .map(f => (
                    <option key={f.id} value={String(f.id)}>Flat {f.flatNumber}</option>
                  ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Reason for Relocation</label>
            <input
              type="text"
              value={moveReason}
              onChange={(e) => setMoveReason(e.target.value)}
              placeholder="e.g. Upgraded to 3BHK flat"
              className="w-full p-2 bg-surface border border-border rounded-lg"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={() => setMoveModalOpen(false)}
              className="px-4 py-2 bg-surface hover:bg-surface-hover text-text-secondary rounded-lg font-semibold"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={movingFlat}
              className="px-4 py-2 bg-teal-600 hover:bg-teal-700 text-white rounded-lg font-semibold"
            >
              {movingFlat ? 'Relocating...' : 'Confirm Flat Move'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Move History Modal */}
      <Modal
        isOpen={historyModalOpen}
        onClose={() => setHistoryModalOpen(false)}
        title="Resident Flat Movement History"
      >
        <div className="space-y-3 text-xs">
          {moveHistory.length === 0 ? (
            <p className="text-text-muted py-4 text-center">No flat relocation history recorded for this resident.</p>
          ) : (
            moveHistory.map((m) => (
              <div key={m.id} className="p-3 rounded-lg bg-surface border border-border space-y-1">
                <div className="flex items-center justify-between font-bold text-text-main">
                  <span>{m.old_block_name} - {m.old_flat_number} → {m.new_block_name} - {m.new_flat_number}</span>
                  <span className="text-text-muted text-[10px]">{new Date(m.move_date).toLocaleString()}</span>
                </div>
                <div className="text-text-secondary text-[11px]">Reason: {m.reason || 'Not specified'}</div>
                <div className="text-[10px] text-text-muted">
                  Authorized by: {m.authorizer_name} • Future passes cancelled: {m.future_invitations_invalidated_count}
                </div>
              </div>
            ))
          )}
        </div>
      </Modal>
    </div>
  );
};
