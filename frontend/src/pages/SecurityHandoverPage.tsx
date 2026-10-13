import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { SecurityHandoverNote, Gate } from '../types';
import { Modal } from '../components/Modal';

export const SecurityHandoverPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [notes, setNotes] = useState<SecurityHandoverNote[]>([]);
  const [gates, setGates] = useState<Gate[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Operational snapshot
  const [dashboardStats, setDashboardStats] = useState<Record<string, number>>({});

  // Add Note form
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [shiftName, setShiftName] = useState<string>('Morning to Afternoon Handover');
  const [gateId, setGateId] = useState<string>(String(user?.assignedGateId || 1));
  const [noteText, setNoteText] = useState<string>('');

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [notesRes, gatesRes, statsRes] = await Promise.all([
        api.get<{ data: SecurityHandoverNote[] }>('/security/handover/recent'),
        api.get<{ data: Gate[] }>('/community/gates'),
        api.get<{ data: Record<string, number> }>('/reports/dashboard')
      ]);
      setNotes(notesRes.data?.data || []);
      setGates(gatesRes.data?.data || []);
      setDashboardStats(statsRes.data?.data || {});
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load handover logs');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreateNote = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/security/handover', {
        shiftName,
        gateId: Number(gateId),
        noteText
      });
      setShowAddModal(false);
      setNoteText('');
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create handover note');
    }
  };

  const handleAcknowledge = async (id: number) => {
    try {
      await api.post(`/security/handover/${id}/acknowledge`);
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to acknowledge note');
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('handover.title')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Guard shift-to-shift log, pending operational tasks, and acknowledgment audit
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
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
          + {t('handover.addNote')}
        </button>
      </div>

      {/* Handover live status snapshot */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px', marginBottom: '24px' }}>
        <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', padding: '16px', borderRadius: '10px' }}>
          <div style={{ fontSize: '12px', color: '#64748B', fontWeight: 600 }}>Visitors Inside</div>
          <div style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', marginTop: '4px' }}>{dashboardStats.currentlyInside || 0}</div>
        </div>
        <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', padding: '16px', borderRadius: '10px' }}>
          <div style={{ fontSize: '12px', color: '#64748B', fontWeight: 600 }}>Overdue Visits</div>
          <div style={{ fontSize: '24px', fontWeight: 700, color: (dashboardStats.overdueVisits || 0) > 0 ? '#DC2626' : '#0F172A', marginTop: '4px' }}>{dashboardStats.overdueVisits || 0}</div>
        </div>
        <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', padding: '16px', borderRadius: '10px' }}>
          <div style={{ fontSize: '12px', color: '#64748B', fontWeight: 600 }}>Parcels at Gate</div>
          <div style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', marginTop: '4px' }}>{dashboardStats.pendingParcels || 0}</div>
        </div>
        <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', padding: '16px', borderRadius: '10px' }}>
          <div style={{ fontSize: '12px', color: '#64748B', fontWeight: 600 }}>Staff Inside</div>
          <div style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', marginTop: '4px' }}>{dashboardStats.staffInside || 0}</div>
        </div>
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : notes.length === 0 ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '48px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '40px', marginBottom: '12px' }}>📋</div>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#1E293B', margin: '0 0 6px 0' }}>No handover logs</h3>
          <p style={{ fontSize: '14px', color: '#64748B', margin: 0 }}>
            No shift handover records logged recently.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {notes.map((n) => (
            <div
              key={n.id}
              style={{
                backgroundColor: '#FFFFFF',
                border: '1px solid #E2E8F0',
                borderRadius: '12px',
                padding: '20px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.02)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <div>
                  <span style={{ fontSize: '12px', fontWeight: 700, color: '#1E40AF', backgroundColor: '#EFF6FF', padding: '3px 8px', borderRadius: '6px' }}>
                    {n.shiftName}
                  </span>
                  <span style={{ fontSize: '13px', color: '#64748B', marginLeft: '10px' }}>
                    Gate: <strong>{n.gateName}</strong> • Logged by <strong>{n.authorName}</strong>
                  </span>
                </div>

                <span style={{ fontSize: '12px', color: '#94A3B8' }}>
                  {new Date(n.createdAt || '').toLocaleString()}
                </span>
              </div>

              <div style={{ fontSize: '14px', color: '#334155', lineHeight: '1.6', margin: '12px 0', whiteSpace: 'pre-wrap' }}>
                {n.noteText}
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid #F1F5F9', paddingTop: '12px', fontSize: '13px' }}>
                <div>
                  {n.acknowledged ? (
                    <span style={{ color: '#059669', fontWeight: 600 }}>
                      ✓ Acknowledged by {n.acknowledgedByName} at {new Date(n.acknowledgedAt || '').toLocaleTimeString()}
                    </span>
                  ) : (
                    <span style={{ color: '#D97706', fontWeight: 600 }}>
                      ⏳ Pending Incoming Guard Acknowledgment
                    </span>
                  )}
                </div>

                {!n.acknowledged && (
                  <button
                    onClick={() => handleAcknowledge(n.id)}
                    style={{
                      padding: '6px 14px',
                      backgroundColor: '#059669',
                      color: '#FFFFFF',
                      border: 'none',
                      borderRadius: '6px',
                      fontWeight: 600,
                      cursor: 'pointer',
                      fontSize: '12px'
                    }}
                  >
                    Acknowledge & Sign
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Handover Note Modal */}
      {showAddModal && (
        <Modal title="Add Shift Handover Note" onClose={() => setShowAddModal(false)}>
          <form onSubmit={handleCreateNote} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Shift Name *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Day to Night Shift Handover"
                value={shiftName}
                onChange={(e) => setShiftName(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Gate *
              </label>
              <select
                value={gateId}
                onChange={(e) => setGateId(e.target.value)}
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
                Handover Notes / Operational Instructions *
              </label>
              <textarea
                required
                rows={5}
                placeholder="Details of visitors still inside, expected night couriers, maintenance personnel in Block B, intercom issues..."
                value={noteText}
                onChange={(e) => setNoteText(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px', fontFamily: 'inherit' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Record Handover
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
