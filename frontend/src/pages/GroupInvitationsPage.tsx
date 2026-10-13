import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { GroupInvitation, GroupInvitationMember, Gate } from '../types';
import { Modal } from '../components/Modal';

export const GroupInvitationsPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [invitations, setInvitations] = useState<GroupInvitation[]>([]);
  const [gates, setGates] = useState<Gate[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Modals
  const [showCreateModal, setShowCreateModal] = useState<boolean>(false);
  const [showDetailModal, setShowDetailModal] = useState<boolean>(false);
  const [selectedInvitation, setSelectedInvitation] = useState<GroupInvitation | null>(null);

  // Create form
  const [eventTitle, setEventTitle] = useState<string>('');
  const [arrivalWindowStart, setArrivalWindowStart] = useState<string>(new Date().toISOString().slice(0, 16));
  const [arrivalWindowEnd, setArrivalWindowEnd] = useState<string>(new Date(Date.now() + 6 * 3600000).toISOString().slice(0, 16));
  const [maxGuests, setMaxGuests] = useState<number>(10);
  const [notes, setNotes] = useState<string>('');
  const [guestNamesRaw, setGuestNamesRaw] = useState<string>('');

  const isSecurityOrAdmin = user?.role === 'ADMINISTRATOR' || user?.role === 'SECURITY';

  const fetchInvitations = async () => {
    setLoading(true);
    setError(null);
    try {
      const [invRes, gatesRes] = await Promise.all([
        api.get<{ data: GroupInvitation[] }>('/group-invitations/my-groups'),
        api.get<{ data: Gate[] }>('/community/gates')
      ]);
      setInvitations(invRes.data?.data || []);
      setGates(gatesRes.data?.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load group invitations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInvitations();
  }, [user]);

  const handleCreateGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    const guestNames = guestNamesRaw
      .split('\n')
      .map(n => n.trim())
      .filter(n => n.length > 0);

    if (guestNames.length === 0) {
      alert('Please enter at least one guest name');
      return;
    }

    try {
      await api.post('/group-invitations', {
        invitation: {
          eventTitle,
          arrivalWindowStart: new Date(arrivalWindowStart).toISOString(),
          arrivalWindowEnd: new Date(arrivalWindowEnd).toISOString(),
          maxGuests: Number(maxGuests) || Math.max(guestNames.length, 10),
          notes
        },
        guestNames
      });
      setShowCreateModal(false);
      setEventTitle('');
      setGuestNamesRaw('');
      fetchInvitations();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create group invitation');
    }
  };

  const openDetail = async (inv: GroupInvitation) => {
    try {
      const res = await api.get<{ data: GroupInvitation }>(`/group-invitations/${inv.id}`);
      setSelectedInvitation(res.data?.data || inv);
      setShowDetailModal(true);
    } catch (err) {
      setSelectedInvitation(inv);
      setShowDetailModal(true);
    }
  };

  const handleCheckInGuest = async (memberId: number) => {
    try {
      const gateId = gates[0]?.id || 1;
      await api.post(`/group-invitations/members/${memberId}/check-in?gateId=${gateId}`);
      if (selectedInvitation) openDetail(selectedInvitation);
      fetchInvitations();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to check in guest');
    }
  };

  const handleCheckOutGuest = async (memberId: number) => {
    try {
      const gateId = gates[0]?.id || 1;
      await api.post(`/group-invitations/members/${memberId}/check-out?gateId=${gateId}`);
      if (selectedInvitation) openDetail(selectedInvitation);
      fetchInvitations();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to check out guest');
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('nav.groupInvitations')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Birthday parties, family get-togethers, and multi-guest event passes
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
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
          + Create Group Event Pass
        </button>
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : invitations.length === 0 ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '48px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '40px', marginBottom: '12px' }}>🎉</div>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#1E293B', margin: '0 0 6px 0' }}>No group invitations</h3>
          <p style={{ fontSize: '14px', color: '#64748B', margin: 0 }}>
            Create an event pass with a list of named attendees for quick gate entry without multiple approval delays.
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '16px' }}>
          {invitations.map((inv) => (
            <div
              key={inv.id}
              onClick={() => openDetail(inv)}
              style={{
                backgroundColor: '#FFFFFF',
                border: '1px solid #E2E8F0',
                borderRadius: '12px',
                padding: '20px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.02)',
                cursor: 'pointer',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                  <span style={{ fontSize: '12px', fontWeight: 700, color: '#1E40AF', backgroundColor: '#EFF6FF', padding: '3px 8px', borderRadius: '6px' }}>
                    {inv.groupReference}
                  </span>
                  <span style={{ fontSize: '12px', fontWeight: 600, color: '#059669', backgroundColor: '#DEF7EC', padding: '3px 8px', borderRadius: '6px' }}>
                    {inv.status}
                  </span>
                </div>

                <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#0F172A', margin: '0 0 8px 0' }}>
                  {inv.eventTitle}
                </h3>

                <div style={{ fontSize: '13px', color: '#475569', lineHeight: '1.6' }}>
                  <div><strong>Time Window:</strong> {new Date(inv.arrivalWindowStart).toLocaleString()} - {new Date(inv.arrivalWindowEnd).toLocaleTimeString()}</div>
                  <div><strong>Capacity:</strong> Up to {inv.maxGuests} guests</div>
                  <div><strong>Inside Right Now:</strong> {inv.insideGuestsCount || 0} guests</div>
                  {inv.members && <div><strong>Invited Count:</strong> {inv.members.length} named guests</div>}
                </div>
              </div>

              <div style={{ marginTop: '16px', borderTop: '1px solid #F1F5F9', paddingTop: '12px', fontSize: '13px', color: '#3B82F6', fontWeight: 600 }}>
                View Guest Roster & QR Codes →
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Detail Modal */}
      {showDetailModal && selectedInvitation && (
        <Modal title={`${selectedInvitation.eventTitle} (${selectedInvitation.groupReference})`} onClose={() => setShowDetailModal(false)}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', maxHeight: '75vh', overflowY: 'auto' }}>
            <div style={{ fontSize: '13px', color: '#475569', backgroundColor: '#F8FAFC', padding: '12px', borderRadius: '8px', border: '1px solid #E2E8F0' }}>
              <div><strong>Destination Flat:</strong> {selectedInvitation.blockName} - {selectedInvitation.flatNumber}</div>
              <div><strong>Arrival Window:</strong> {new Date(selectedInvitation.arrivalWindowStart).toLocaleString()} - {new Date(selectedInvitation.arrivalWindowEnd).toLocaleString()}</div>
              <div><strong>Capacity & Occupancy:</strong> {selectedInvitation.insideGuestsCount || 0} inside / {selectedInvitation.maxGuests} max permitted</div>
              {selectedInvitation.notes && <div style={{ marginTop: '4px' }}><strong>Host Notes:</strong> {selectedInvitation.notes}</div>}
            </div>

            <h4 style={{ fontSize: '15px', fontWeight: 600, color: '#0F172A', margin: '0' }}>Named Guest List ({selectedInvitation.members?.length || 0})</h4>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {selectedInvitation.members?.map((m) => (
                <div
                  key={m.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '10px 14px',
                    backgroundColor: '#FFFFFF',
                    border: '1px solid #E2E8F0',
                    borderRadius: '8px'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 600, color: '#0F172A', fontSize: '14px' }}>{m.guestName}</div>
                    <div style={{ fontSize: '12px', color: '#64748B' }}>
                      Status: <span style={{ fontWeight: 600, color: m.status === 'INSIDE' ? '#059669' : m.status === 'CHECKED_OUT' ? '#64748B' : '#1E40AF' }}>{m.status}</span>
                      {m.checkInTime && ` • Admitted at ${new Date(m.checkInTime).toLocaleTimeString()}`}
                    </div>
                  </div>

                  {isSecurityOrAdmin && (
                    <div style={{ display: 'flex', gap: '6px' }}>
                      {m.status === 'EXPECTED' && (
                        <button
                          onClick={() => handleCheckInGuest(m.id)}
                          style={{ padding: '4px 10px', fontSize: '12px', backgroundColor: '#059669', color: '#FFFFFF', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 }}
                        >
                          Admit
                        </button>
                      )}
                      {m.status === 'INSIDE' && (
                        <button
                          onClick={() => handleCheckOutGuest(m.id)}
                          style={{ padding: '4px 10px', fontSize: '12px', backgroundColor: '#DC2626', color: '#FFFFFF', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 }}
                        >
                          Exit
                        </button>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </Modal>
      )}

      {/* Create Modal */}
      {showCreateModal && (
        <Modal title="Create Group Event Invitation" onClose={() => setShowCreateModal(false)}>
          <form onSubmit={handleCreateGroup} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Event / Party Title *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Aarav Birthday Party, Diwali Family Gathering"
                value={eventTitle}
                onChange={(e) => setEventTitle(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Arrival Window Start *
                </label>
                <input
                  required
                  type="datetime-local"
                  value={arrivalWindowStart}
                  onChange={(e) => setArrivalWindowStart(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Arrival Window End *
                </label>
                <input
                  required
                  type="datetime-local"
                  value={arrivalWindowEnd}
                  onChange={(e) => setArrivalWindowEnd(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Maximum Allowed Capacity *
              </label>
              <input
                required
                type="number"
                min={1}
                max={100}
                value={maxGuests}
                onChange={(e) => setMaxGuests(Number(e.target.value))}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Guest Full Names (One per line) *
              </label>
              <textarea
                required
                rows={5}
                placeholder="Rohan Sharma&#10;Kavita Menon&#10;Vikram Patel&#10;Sunita Reddy"
                value={guestNamesRaw}
                onChange={(e) => setGuestNamesRaw(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px', fontFamily: 'inherit' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Notes for Security Guard
              </label>
              <input
                type="text"
                placeholder="e.g. Direct guests to Clubhouse Parking Level 1"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Issue Group Event Passes
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
