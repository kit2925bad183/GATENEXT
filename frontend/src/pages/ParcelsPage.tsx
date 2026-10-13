import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { Parcel, ParcelEvent, User, Flat } from '../types';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';

export const ParcelsPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [parcels, setParcels] = useState<Parcel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [lastRefreshed, setLastRefreshed] = useState<string>(new Date().toLocaleTimeString());

  // Modals
  const [showReceiveModal, setShowReceiveModal] = useState<boolean>(false);
  const [showDelegateModal, setShowDelegateModal] = useState<boolean>(false);
  const [showCollectModal, setShowCollectModal] = useState<boolean>(false);
  const [showEventsModal, setShowEventsModal] = useState<boolean>(false);

  const [selectedParcel, setSelectedParcel] = useState<Parcel | null>(null);
  const [parcelEvents, setParcelEvents] = useState<ParcelEvent[]>([]);

  // Form states
  const [residents, setResidents] = useState<User[]>([]);
  const [recipientUserId, setRecipientUserId] = useState<string>('');
  const [destinationFlatId, setDestinationFlatId] = useState<string>('');
  const [courierName, setCourierName] = useState<string>('');
  const [trackingNumber, setTrackingNumber] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [storageLocation, setStorageLocation] = useState<string>('Gate Security Room - Shelf A1');

  const [delegateName, setDelegateName] = useState<string>('');
  const [delegateNotes, setDelegateNotes] = useState<string>('');

  const [collectorName, setCollectorName] = useState<string>('');

  const isSecurityOrAdmin = user?.role === 'ADMINISTRATOR' || user?.role === 'SECURITY';

  const fetchParcels = async () => {
    setLoading(true);
    setError(null);
    try {
      const endpoint = isSecurityOrAdmin ? '/parcels/pending' : '/parcels/my-parcels';
      const res = await api.get<{ data: Parcel[] }>(endpoint);
      if (res.data?.data) {
        setParcels(res.data.data);
      }
      setLastRefreshed(new Date().toLocaleTimeString());
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load parcels');
    } finally {
      setLoading(false);
    }
  };

  const fetchResidents = async () => {
    if (isSecurityOrAdmin) {
      try {
        const res = await api.get<{ data: { items?: User[]; residents?: User[] } }>('/admin/residents?status=APPROVED&limit=100');
        const list = res.data?.data?.items || res.data?.data?.residents || [];
        setResidents(list);
      } catch (err) {
        console.error('Failed to load resident directory', err);
      }
    }
  };

  useEffect(() => {
    fetchParcels();
    fetchResidents();
  }, []);

  const handleReceiveParcel = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/parcels', {
        recipientUserId: Number(recipientUserId),
        destinationFlatId: Number(destinationFlatId),
        courierName,
        trackingNumber,
        description,
        storageLocation
      });
      setShowReceiveModal(false);
      setCourierName('');
      setTrackingNumber('');
      setDescription('');
      fetchParcels();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to receive parcel');
    }
  };

  const handleDelegateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedParcel) return;
    try {
      await api.post(`/parcels/${selectedParcel.id}/delegate`, {
        delegateName,
        notes: delegateNotes
      });
      setShowDelegateModal(false);
      fetchParcels();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update delegate');
    }
  };

  const handleCollectSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedParcel) return;
    try {
      await api.post(`/parcels/${selectedParcel.id}/collect`, {
        collectorName: collectorName || selectedParcel.recipientName || 'Resident',
        collectorUserId: selectedParcel.recipientUserId
      });
      setShowCollectModal(false);
      fetchParcels();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to mark parcel collected');
    }
  };

  const viewEvents = async (parcel: Parcel) => {
    setSelectedParcel(parcel);
    try {
      const res = await api.get<{ data: ParcelEvent[] }>(`/parcels/${parcel.id}/events`);
      setParcelEvents(res.data?.data || []);
      setShowEventsModal(true);
    } catch (err) {
      alert('Failed to load parcel history logs');
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('parcel.title')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            {t('common.lastUpdated')}: {lastRefreshed}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            onClick={fetchParcels}
            style={{
              padding: '8px 14px',
              borderRadius: '8px',
              border: '1px solid #CBD5E1',
              backgroundColor: '#FFFFFF',
              color: '#334155',
              cursor: 'pointer',
              fontWeight: 500,
              fontSize: '13px'
            }}
          >
            {t('common.refresh')}
          </button>
          {isSecurityOrAdmin && (
            <button
              onClick={() => setShowReceiveModal(true)}
              style={{
                padding: '8px 16px',
                borderRadius: '8px',
                border: 'none',
                backgroundColor: '#1E40AF',
                color: '#FFFFFF',
                cursor: 'pointer',
                fontWeight: 600,
                fontSize: '13px',
                boxShadow: '0 2px 4px rgba(30,64,175,0.2)'
              }}
            >
              + {t('parcel.receive')}
            </button>
          )}
        </div>
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '40px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '15px', color: '#64748B' }}>{t('common.loading')}</div>
        </div>
      ) : parcels.length === 0 ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '48px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '40px', marginBottom: '12px' }}>📦</div>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#1E293B', margin: '0 0 6px 0' }}>No parcels found</h3>
          <p style={{ fontSize: '14px', color: '#64748B', margin: 0 }}>
            {isSecurityOrAdmin ? 'No pending parcels awaiting pickup at the gate.' : 'You have no parcels registered at the gate.'}
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '16px' }}>
          {parcels.map((parcel) => (
            <div
              key={parcel.id}
              style={{
                backgroundColor: '#FFFFFF',
                border: '1px solid #E2E8F0',
                borderRadius: '12px',
                padding: '20px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                  <span style={{ fontSize: '12px', fontWeight: 700, color: '#1E40AF', backgroundColor: '#EFF6FF', padding: '3px 8px', borderRadius: '6px' }}>
                    {parcel.courierName}
                  </span>
                  <span
                    style={{
                      fontSize: '11px',
                      fontWeight: 600,
                      padding: '3px 8px',
                      borderRadius: '12px',
                      backgroundColor: parcel.status === 'COLLECTED' ? '#DEF7EC' : '#FEF08A',
                      color: parcel.status === 'COLLECTED' ? '#03543F' : '#713F12'
                    }}
                  >
                    {parcel.status}
                  </span>
                </div>

                <h4 style={{ fontSize: '16px', fontWeight: 600, color: '#0F172A', margin: '0 0 6px 0' }}>
                  {parcel.description || 'Package / Delivery Parcel'}
                </h4>

                <div style={{ fontSize: '13px', color: '#475569', lineHeight: '1.6', marginBottom: '12px' }}>
                  <div><strong>Flat:</strong> {parcel.blockName} - {parcel.flatNumber}</div>
                  <div><strong>Recipient:</strong> {parcel.recipientName} ({parcel.recipientMobile || ''})</div>
                  <div><strong>Storage:</strong> {parcel.storageLocation}</div>
                  {parcel.trackingNumber && <div><strong>Tracking:</strong> {parcel.trackingNumber}</div>}
                  {parcel.delegateCollectorName && (
                    <div style={{ marginTop: '6px', padding: '6px 10px', backgroundColor: '#F8FAFC', borderRadius: '6px', border: '1px solid #E2E8F0' }}>
                      <strong>Authorized Delegate:</strong> {parcel.delegateCollectorName}
                      {parcel.delegateNotes && <div style={{ fontSize: '12px', color: '#64748B' }}>"{parcel.delegateNotes}"</div>}
                    </div>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', gap: '8px', borderTop: '1px solid #F1F5F9', paddingTop: '14px', marginTop: '10px' }}>
                <button
                  onClick={() => viewEvents(parcel)}
                  style={{
                    flex: 1,
                    padding: '6px 10px',
                    fontSize: '12px',
                    borderRadius: '6px',
                    border: '1px solid #CBD5E1',
                    backgroundColor: '#FFFFFF',
                    color: '#334155',
                    cursor: 'pointer'
                  }}
                >
                  History Log
                </button>

                {!isSecurityOrAdmin && parcel.status !== 'COLLECTED' && (
                  <button
                    onClick={() => {
                      setSelectedParcel(parcel);
                      setDelegateName(parcel.delegateCollectorName || '');
                      setDelegateNotes(parcel.delegateNotes || '');
                      setShowDelegateModal(true);
                    }}
                    style={{
                      flex: 1,
                      padding: '6px 10px',
                      fontSize: '12px',
                      borderRadius: '6px',
                      border: '1px solid #3B82F6',
                      backgroundColor: '#EFF6FF',
                      color: '#1D4ED8',
                      cursor: 'pointer',
                      fontWeight: 600
                    }}
                  >
                    Authorize Delegate
                  </button>
                )}

                {isSecurityOrAdmin && parcel.status !== 'COLLECTED' && (
                  <button
                    onClick={() => {
                      setSelectedParcel(parcel);
                      setCollectorName(parcel.delegateCollectorName || parcel.recipientName || '');
                      setShowCollectModal(true);
                    }}
                    style={{
                      flex: 1,
                      padding: '6px 10px',
                      fontSize: '12px',
                      borderRadius: '6px',
                      border: 'none',
                      backgroundColor: '#059669',
                      color: '#FFFFFF',
                      cursor: 'pointer',
                      fontWeight: 600
                    }}
                  >
                    Mark Collected
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Receive Parcel Modal */}
      {showReceiveModal && (
        <Modal title="Receive New Parcel at Gate" onClose={() => setShowReceiveModal(false)}>
          <form onSubmit={handleReceiveParcel} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Recipient Resident *
              </label>
              <select
                required
                value={recipientUserId}
                onChange={(e) => {
                  setRecipientUserId(e.target.value);
                  const found = residents.find(r => String(r.id) === e.target.value);
                  if (found && found.flatId) {
                    setDestinationFlatId(String(found.flatId));
                  }
                }}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="">-- Select Resident --</option>
                {residents.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.fullName} ({r.blockName} - {r.flatNumber})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Courier / Delivery Agency *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Amazon, BlueDart, Dunzo, Swiggy Instamart"
                value={courierName}
                onChange={(e) => setCourierName(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Tracking / AWB Number (Optional)
              </label>
              <input
                type="text"
                placeholder="e.g. TRK-99881122"
                value={trackingNumber}
                onChange={(e) => setTrackingNumber(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Description
              </label>
              <input
                type="text"
                placeholder="e.g. Medium cardboard box, envelope"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Storage Location *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Gate Security Room - Shelf A1"
                value={storageLocation}
                onChange={(e) => setStorageLocation(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowReceiveModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Record Receipt & Notify Resident
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Delegate Modal */}
      {showDelegateModal && selectedParcel && (
        <Modal title="Authorize Someone Else to Collect" onClose={() => setShowDelegateModal(false)}>
          <form onSubmit={handleDelegateSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <p style={{ fontSize: '13px', color: '#64748B', margin: 0 }}>
              Specify the name of a neighbor, family member, or domestic help authorized to collect this parcel on your behalf.
            </p>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Delegate Full Name *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Priya (Neighbor in A-102) or Maid Lakshmi"
                value={delegateName}
                onChange={(e) => setDelegateName(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Instructions / Notes for Security Guard
              </label>
              <input
                type="text"
                placeholder="e.g. Hand over after checking ID"
                value={delegateNotes}
                onChange={(e) => setDelegateNotes(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowDelegateModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Save Authorization
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Collect Modal */}
      {showCollectModal && selectedParcel && (
        <Modal title="Confirm Parcel Handover" onClose={() => setShowCollectModal(false)}>
          <form onSubmit={handleCollectSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <p style={{ fontSize: '13px', color: '#64748B', margin: 0 }}>
              Verify identity before releasing the package.
            </p>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Collected By Name *
              </label>
              <input
                required
                type="text"
                value={collectorName}
                onChange={(e) => setCollectorName(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowCollectModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#059669', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Confirm Collection & Record
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Events Log Modal */}
      {showEventsModal && (
        <Modal title="Parcel Activity Timeline" onClose={() => setShowEventsModal(false)}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {parcelEvents.length === 0 ? (
              <p style={{ color: '#64748B', fontSize: '14px' }}>No activity records found.</p>
            ) : (
              parcelEvents.map((ev) => (
                <div key={ev.id} style={{ borderLeft: '3px solid #3B82F6', paddingLeft: '12px', fontSize: '13px' }}>
                  <div style={{ fontWeight: 600, color: '#1E293B' }}>{ev.eventType}</div>
                  <div style={{ color: '#475569', margin: '2px 0' }}>{ev.notes}</div>
                  <div style={{ fontSize: '11px', color: '#94A3B8' }}>{new Date(ev.createdAt).toLocaleString()}</div>
                </div>
              ))
            )}
          </div>
        </Modal>
      )}
    </div>
  );
};
