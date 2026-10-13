import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { StaffMember, StaffAuthorization, StaffAttendanceSession, Gate } from '../types';
import { Modal } from '../components/Modal';

export const StaffAttendancePage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [activeTab, setActiveTab] = useState<'directory' | 'authorizations' | 'attendance'>('directory');
  const [staffList, setStaffList] = useState<StaffMember[]>([]);
  const [authorizations, setAuthorizations] = useState<StaffAuthorization[]>([]);
  const [attendanceSessions, setAttendanceSessions] = useState<StaffAttendanceSession[]>([]);
  const [gates, setGates] = useState<Gate[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Modals
  const [showAddStaffModal, setShowAddStaffModal] = useState<boolean>(false);
  const [showAuthModal, setShowAuthModal] = useState<boolean>(false);
  const [showCheckInModal, setShowCheckInModal] = useState<boolean>(false);
  const [selectedStaff, setSelectedStaff] = useState<StaffMember | null>(null);

  // Add Staff form
  const [fullName, setFullName] = useState<string>('');
  const [mobile, setMobile] = useState<string>('');
  const [staffType, setStaffType] = useState<string>('MAID');
  const [idProofType, setIdProofType] = useState<string>('AADHAAR');
  const [idProofNumber, setIdProofNumber] = useState<string>('');
  const [notes, setNotes] = useState<string>('');

  // Authorize form
  const [selectedStaffIdForAuth, setSelectedStaffIdForAuth] = useState<string>('');
  const [scheduleDescription, setScheduleDescription] = useState<string>('Mon-Sat 08:00 - 11:00');

  // Gate CheckIn form
  const [selectedGateId, setSelectedGateId] = useState<string>('');
  const [gateNotes, setGateNotes] = useState<string>('');

  const isSecurityOrAdmin = user?.role === 'ADMINISTRATOR' || user?.role === 'SECURITY';

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [staffRes, gatesRes] = await Promise.all([
        api.get<{ data: StaffMember[] }>('/staff'),
        api.get<{ data: Gate[] }>('/community/gates')
      ]);
      setStaffList(staffRes.data?.data || []);
      const gateList = gatesRes.data?.data || [];
      setGates(gateList);
      if (gateList.length > 0) {
        setSelectedGateId(String(gateList[0].id));
      }

      if (user?.role === 'RESIDENT') {
        const authRes = await api.get<{ data: StaffAuthorization[] }>('/staff/my-authorizations');
        setAuthorizations(authRes.data?.data || []);
      }

      if (isSecurityOrAdmin) {
        const attRes = await api.get<{ data: StaffAttendanceSession[] }>('/staff/attendance/active');
        setAttendanceSessions(attRes.data?.data || []);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load staff records');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const handleCreateStaff = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/staff', {
        fullName,
        mobile,
        staffType,
        idProofType,
        idProofNumber,
        notes,
        active: true
      });
      setShowAddStaffModal(false);
      setFullName('');
      setMobile('');
      setNotes('');
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to register staff member');
    }
  };

  const handleAuthorizeStaff = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/staff/authorize', {
        staffMemberId: Number(selectedStaffIdForAuth),
        scheduleDescription
      });
      setShowAuthModal(false);
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to authorize staff');
    }
  };

  const handleRevokeAuth = async (authId: number) => {
    if (!confirm('Are you sure you want to revoke authorization for this staff member?')) return;
    try {
      await api.delete(`/staff/authorizations/${authId}`);
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to revoke authorization');
    }
  };

  const handleGateCheckIn = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedStaff) return;
    try {
      await api.post(`/staff/${selectedStaff.id}/check-in`, {
        gateId: Number(selectedGateId),
        notes: gateNotes
      });
      setShowCheckInModal(false);
      setGateNotes('');
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to check in staff');
    }
  };

  const handleGateCheckOut = async (staff: StaffMember) => {
    try {
      await api.post(`/staff/${staff.id}/check-out`, {
        gateId: Number(selectedGateId) || gates[0]?.id || 1,
        notes: 'Normal shift completion checkout'
      });
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to check out staff');
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('staff.title')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Cleaners, cooks, drivers, and domestic service attendance
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          {user?.role === 'RESIDENT' && (
            <button
              onClick={() => setShowAuthModal(true)}
              style={{
                padding: '8px 16px',
                borderRadius: '8px',
                border: 'none',
                backgroundColor: '#1E40AF',
                color: '#FFFFFF',
                fontWeight: 600,
                fontSize: '13px',
                cursor: 'pointer'
              }}
            >
              + {t('staff.authorize')}
            </button>
          )}

          {isSecurityOrAdmin && (
            <button
              onClick={() => setShowAddStaffModal(true)}
              style={{
                padding: '8px 16px',
                borderRadius: '8px',
                border: 'none',
                backgroundColor: '#1E40AF',
                color: '#FFFFFF',
                fontWeight: 600,
                fontSize: '13px',
                cursor: 'pointer'
              }}
            >
              + {t('staff.addStaff')}
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '8px', borderBottom: '1px solid #E2E8F0', marginBottom: '20px' }}>
        <button
          onClick={() => setActiveTab('directory')}
          style={{
            padding: '10px 16px',
            fontSize: '14px',
            fontWeight: activeTab === 'directory' ? 600 : 500,
            color: activeTab === 'directory' ? '#1E40AF' : '#64748B',
            borderBottom: activeTab === 'directory' ? '2px solid #1E40AF' : 'none',
            backgroundColor: 'transparent',
            border: 'none',
            cursor: 'pointer'
          }}
        >
          {t('staff.directory')} ({staffList.length})
        </button>

        {user?.role === 'RESIDENT' && (
          <button
            onClick={() => setActiveTab('authorizations')}
            style={{
              padding: '10px 16px',
              fontSize: '14px',
              fontWeight: activeTab === 'authorizations' ? 600 : 500,
              color: activeTab === 'authorizations' ? '#1E40AF' : '#64748B',
              borderBottom: activeTab === 'authorizations' ? '2px solid #1E40AF' : 'none',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer'
            }}
          >
            {t('staff.authorizations')} ({authorizations.length})
          </button>
        )}

        {isSecurityOrAdmin && (
          <button
            onClick={() => setActiveTab('attendance')}
            style={{
              padding: '10px 16px',
              fontSize: '14px',
              fontWeight: activeTab === 'attendance' ? 600 : 500,
              color: activeTab === 'attendance' ? '#1E40AF' : '#64748B',
              borderBottom: activeTab === 'attendance' ? '2px solid #1E40AF' : 'none',
              backgroundColor: 'transparent',
              border: 'none',
              cursor: 'pointer'
            }}
          >
            {t('staff.attendance')} ({attendanceSessions.length})
          </button>
        )}
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : activeTab === 'directory' ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
          {staffList.map((staff) => (
            <div
              key={staff.id}
              style={{
                backgroundColor: '#FFFFFF',
                border: '1px solid #E2E8F0',
                borderRadius: '12px',
                padding: '20px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                <span style={{ fontSize: '12px', fontWeight: 600, color: '#0F766E', backgroundColor: '#F0FDFA', padding: '3px 8px', borderRadius: '6px' }}>
                  {staff.staffType}
                </span>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 600,
                    padding: '3px 8px',
                    borderRadius: '12px',
                    backgroundColor: staff.currentlyInside ? '#DEF7EC' : '#F1F5F9',
                    color: staff.currentlyInside ? '#03543F' : '#64748B'
                  }}
                >
                  {staff.currentlyInside ? 'Inside Premises' : 'Outside'}
                </span>
              </div>

              <h3 style={{ fontSize: '17px', fontWeight: 600, color: '#0F172A', margin: '0 0 4px 0' }}>
                {staff.fullName}
              </h3>
              <div style={{ fontSize: '13px', color: '#64748B', marginBottom: '12px' }}>
                📞 {staff.mobile}
              </div>

              <div style={{ fontSize: '13px', color: '#475569', marginBottom: '14px' }}>
                <div><strong>Authorized Flats:</strong> {staff.authorizedFlatsCount || 0} apartments</div>
                {staff.notes && <div style={{ marginTop: '4px', fontSize: '12px', color: '#64748B' }}>"{staff.notes}"</div>}
                {staff.currentlyInside && (
                  <div style={{ marginTop: '6px', fontSize: '12px', color: '#047857', fontWeight: 500 }}>
                    Checked in at {new Date(staff.checkInTime || '').toLocaleTimeString()} ({staff.entryGateName})
                  </div>
                )}
              </div>

              {isSecurityOrAdmin && (
                <div style={{ display: 'flex', gap: '8px', borderTop: '1px solid #F1F5F9', paddingTop: '12px' }}>
                  {!staff.currentlyInside ? (
                    <button
                      onClick={() => {
                        setSelectedStaff(staff);
                        setShowCheckInModal(true);
                      }}
                      style={{
                        flex: 1,
                        padding: '6px 12px',
                        fontSize: '12px',
                        fontWeight: 600,
                        backgroundColor: '#1E40AF',
                        color: '#FFFFFF',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer'
                      }}
                    >
                      Record Entry
                    </button>
                  ) : (
                    <button
                      onClick={() => handleGateCheckOut(staff)}
                      style={{
                        flex: 1,
                        padding: '6px 12px',
                        fontSize: '12px',
                        fontWeight: 600,
                        backgroundColor: '#DC2626',
                        color: '#FFFFFF',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer'
                      }}
                    >
                      Record Exit
                    </button>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      ) : activeTab === 'authorizations' ? (
        <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', borderRadius: '12px', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
            <thead>
              <tr style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0', color: '#475569' }}>
                <th style={{ padding: '12px 16px' }}>Staff Name</th>
                <th style={{ padding: '12px 16px' }}>Category</th>
                <th style={{ padding: '12px 16px' }}>Mobile</th>
                <th style={{ padding: '12px 16px' }}>Permitted Schedule</th>
                <th style={{ padding: '12px 16px' }}>Status</th>
                <th style={{ padding: '12px 16px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {authorizations.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ padding: '30px', textAlign: 'center', color: '#64748B' }}>
                    No domestic staff currently authorized for your flat.
                  </td>
                </tr>
              ) : (
                authorizations.map((auth) => (
                  <tr key={auth.id} style={{ borderBottom: '1px solid #F1F5F9' }}>
                    <td style={{ padding: '12px 16px', fontWeight: 600, color: '#0F172A' }}>{auth.staffName}</td>
                    <td style={{ padding: '12px 16px', color: '#0F766E' }}>{auth.staffType}</td>
                    <td style={{ padding: '12px 16px', color: '#475569' }}>{auth.staffMobile}</td>
                    <td style={{ padding: '12px 16px', color: '#334155' }}>{auth.scheduleDescription || 'All days'}</td>
                    <td style={{ padding: '12px 16px' }}>
                      <span style={{ fontSize: '12px', fontWeight: 600, color: auth.active ? '#059669' : '#DC2626' }}>
                        {auth.active ? 'Active' : 'Revoked'}
                      </span>
                    </td>
                    <td style={{ padding: '12px 16px' }}>
                      {auth.active && (
                        <button
                          onClick={() => handleRevokeAuth(auth.id)}
                          style={{ padding: '4px 10px', fontSize: '12px', color: '#DC2626', border: '1px solid #FCA5A5', backgroundColor: '#FEF2F2', borderRadius: '6px', cursor: 'pointer' }}
                        >
                          Revoke Access
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      ) : (
        <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', borderRadius: '12px', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
            <thead>
              <tr style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0', color: '#475569' }}>
                <th style={{ padding: '12px 16px' }}>Staff Name</th>
                <th style={{ padding: '12px 16px' }}>Category</th>
                <th style={{ padding: '12px 16px' }}>Check In Time</th>
                <th style={{ padding: '12px 16px' }}>Entry Gate</th>
                <th style={{ padding: '12px 16px' }}>Entry Guard</th>
                <th style={{ padding: '12px 16px' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {attendanceSessions.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ padding: '30px', textAlign: 'center', color: '#64748B' }}>
                    No domestic staff currently inside the community.
                  </td>
                </tr>
              ) : (
                attendanceSessions.map((session) => (
                  <tr key={session.id} style={{ borderBottom: '1px solid #F1F5F9' }}>
                    <td style={{ padding: '12px 16px', fontWeight: 600, color: '#0F172A' }}>{session.staffName}</td>
                    <td style={{ padding: '12px 16px', color: '#0F766E' }}>{session.staffType}</td>
                    <td style={{ padding: '12px 16px', color: '#334155' }}>{new Date(session.checkInTime).toLocaleString()}</td>
                    <td style={{ padding: '12px 16px', color: '#475569' }}>{session.entryGateName}</td>
                    <td style={{ padding: '12px 16px', color: '#475569' }}>{session.entryGuardName}</td>
                    <td style={{ padding: '12px 16px' }}>
                      <button
                        onClick={() => {
                          const staff = staffList.find(s => s.id === session.staffMemberId) || { id: session.staffMemberId } as StaffMember;
                          handleGateCheckOut(staff);
                        }}
                        style={{ padding: '4px 10px', fontSize: '12px', color: '#FFFFFF', backgroundColor: '#DC2626', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}
                      >
                        Check Out
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Add Staff Modal */}
      {showAddStaffModal && (
        <Modal title="Register Domestic Staff Member" onClose={() => setShowAddStaffModal(false)}>
          <form onSubmit={handleCreateStaff} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Full Name *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Ramesh Kumar"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Mobile Number *
              </label>
              <input
                required
                type="text"
                placeholder="+91 98 4500 1122"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Staff Category *
              </label>
              <select
                value={staffType}
                onChange={(e) => setStaffType(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="MAID">{t('staff.type.MAID')}</option>
                <option value="COOK">{t('staff.type.COOK')}</option>
                <option value="DRIVER">{t('staff.type.DRIVER')}</option>
                <option value="CLEANER">{t('staff.type.CLEANER')}</option>
                <option value="GARDENER">{t('staff.type.GARDENER')}</option>
                <option value="TUTOR">{t('staff.type.TUTOR')}</option>
                <option value="OTHER">{t('staff.type.OTHER')}</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                ID Proof Type & Number
              </label>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '8px' }}>
                <select
                  value={idProofType}
                  onChange={(e) => setIdProofType(e.target.value)}
                  style={{ padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                >
                  <option value="AADHAAR">Aadhaar</option>
                  <option value="DRIVING_LICENSE">Driving License</option>
                  <option value="VOTER_ID">Voter ID</option>
                  <option value="PASSPORT">Passport</option>
                </select>
                <input
                  type="text"
                  placeholder="ID Number (Optional)"
                  value={idProofNumber}
                  onChange={(e) => setIdProofNumber(e.target.value)}
                  style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Notes / Special Skills
              </label>
              <input
                type="text"
                placeholder="e.g. Cleans cars, speaks Tamil & Hindi"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowAddStaffModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Save Staff Member
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Authorize Staff for Flat Modal */}
      {showAuthModal && (
        <Modal title="Authorize Staff for My Flat" onClose={() => setShowAuthModal(false)}>
          <form onSubmit={handleAuthorizeStaff} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Select Domestic Staff Member *
              </label>
              <select
                required
                value={selectedStaffIdForAuth}
                onChange={(e) => setSelectedStaffIdForAuth(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="">-- Choose from Directory --</option>
                {staffList.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.fullName} ({s.staffType} - {s.mobile})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Permitted Routine Schedule
              </label>
              <input
                type="text"
                placeholder="e.g. Mon-Sat 07:30 to 10:30"
                value={scheduleDescription}
                onChange={(e) => setScheduleDescription(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowAuthModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Grant Access
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Check In Staff Modal */}
      {showCheckInModal && selectedStaff && (
        <Modal title={`Record Entry for ${selectedStaff.fullName}`} onClose={() => setShowCheckInModal(false)}>
          <form onSubmit={handleGateCheckIn} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Entry Gate *
              </label>
              <select
                required
                value={selectedGateId}
                onChange={(e) => setSelectedGateId(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                {gates.map((g) => (
                  <option key={g.id} value={g.id}>
                    {g.name} ({g.code})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Entry Notes
              </label>
              <input
                type="text"
                placeholder="e.g. Regular morning housekeeping session"
                value={gateNotes}
                onChange={(e) => setGateNotes(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowCheckInModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Confirm Gate Check-In
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
