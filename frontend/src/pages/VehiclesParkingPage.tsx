import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { ParkingSlot, Vehicle, Visit } from '../types';
import { Modal } from '../components/Modal';

export const VehiclesParkingPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [activeTab, setActiveTab] = useState<'slots' | 'vehicles'>('slots');
  const [slots, setSlots] = useState<ParkingSlot[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Modals
  const [showVehicleModal, setShowVehicleModal] = useState<boolean>(false);
  const [showAssignVisitorModal, setShowAssignVisitorModal] = useState<boolean>(false);
  const [selectedSlot, setSelectedSlot] = useState<ParkingSlot | null>(null);

  // Vehicle form
  const [vehicleNumber, setVehicleNumber] = useState<string>('');
  const [vehicleType, setVehicleType] = useState<string>('FOUR_WHEELER');
  const [makeModel, setMakeModel] = useState<string>('');
  const [color, setColor] = useState<string>('');
  const [stickerNumber, setStickerNumber] = useState<string>('');

  // Assign visitor form
  const [insideVisits, setInsideVisits] = useState<Visit[]>([]);
  const [selectedVisitId, setSelectedVisitId] = useState<string>('');
  const [parkingNotes, setParkingNotes] = useState<string>('');

  const isSecurityOrAdmin = user?.role === 'ADMINISTRATOR' || user?.role === 'SECURITY';

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [slotsRes, vehRes] = await Promise.all([
        api.get<{ data: ParkingSlot[] }>('/parking/slots'),
        user?.role === 'RESIDENT'
          ? api.get<{ data: Vehicle[] }>('/parking/my-vehicles')
          : api.get<{ data: Vehicle[] }>('/parking/vehicles')
      ]);
      setSlots(slotsRes.data?.data || []);
      setVehicles(vehRes.data?.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load parking details');
    } finally {
      setLoading(false);
    }
  };

  const fetchInsideVisits = async () => {
    if (isSecurityOrAdmin) {
      try {
        const res = await api.get<{ data: { items?: Visit[] } | Visit[] }>('/visits?status=INSIDE&limit=100');
        const list = Array.isArray(res.data?.data) ? res.data?.data : (res.data?.data?.items || []);
        setInsideVisits(list);
        if (list.length > 0) {
          setSelectedVisitId(String(list[0].id));
        }
      } catch (err) {
        console.error('Failed to load inside visits', err);
      }
    }
  };

  useEffect(() => {
    fetchData();
    fetchInsideVisits();
  }, [user]);

  const handleRegisterVehicle = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/parking/vehicles', {
        vehicleNumber,
        vehicleType,
        makeModel,
        color,
        stickerNumber
      });
      setShowVehicleModal(false);
      setVehicleNumber('');
      setMakeModel('');
      setColor('');
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to register vehicle');
    }
  };

  const handleAssignVisitor = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSlot) return;
    try {
      await api.post('/parking/assign-visitor', {
        slotId: selectedSlot.id,
        visitId: Number(selectedVisitId),
        notes: parkingNotes
      });
      setShowAssignVisitorModal(false);
      setParkingNotes('');
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to assign parking slot');
    }
  };

  const handleReleaseSlot = async (slot: ParkingSlot) => {
    if (!slot.currentAssignmentId) return;
    if (!confirm(`Are you sure you want to release slot ${slot.slotNumber}?`)) return;
    try {
      await api.post(`/parking/release/${slot.currentAssignmentId}`);
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to release slot');
    }
  };

  const getSlotTypeColor = (type: string) => {
    switch (type) {
      case 'EV_CHARGING': return '#059669';
      case 'TWO_WHEELER': return '#D97706';
      case 'VISITOR': return '#2563EB';
      default: return '#475569';
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('parking.title')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Allocated resident slots and visitor parking management
          </p>
        </div>

        <div>
          <button
            onClick={() => setShowVehicleModal(true)}
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
            + {t('parking.registerVehicle')}
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '8px', borderBottom: '1px solid #E2E8F0', marginBottom: '20px' }}>
        <button
          onClick={() => setActiveTab('slots')}
          style={{
            padding: '10px 16px',
            fontSize: '14px',
            fontWeight: activeTab === 'slots' ? 600 : 500,
            color: activeTab === 'slots' ? '#1E40AF' : '#64748B',
            borderBottom: activeTab === 'slots' ? '2px solid #1E40AF' : 'none',
            backgroundColor: 'transparent',
            border: 'none',
            cursor: 'pointer'
          }}
        >
          {t('parking.slots')} ({slots.length})
        </button>
        <button
          onClick={() => setActiveTab('vehicles')}
          style={{
            padding: '10px 16px',
            fontSize: '14px',
            fontWeight: activeTab === 'vehicles' ? 600 : 500,
            color: activeTab === 'vehicles' ? '#1E40AF' : '#64748B',
            borderBottom: activeTab === 'vehicles' ? '2px solid #1E40AF' : 'none',
            backgroundColor: 'transparent',
            border: 'none',
            cursor: 'pointer'
          }}
        >
          {t('parking.myVehicles')} ({vehicles.length})
        </button>
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : activeTab === 'slots' ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '16px' }}>
          {slots.map((slot) => (
            <div
              key={slot.id}
              style={{
                backgroundColor: slot.occupied ? '#FFFBEB' : '#FFFFFF',
                border: slot.occupied ? '1px solid #FDE68A' : '1px solid #E2E8F0',
                borderRadius: '12px',
                padding: '16px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                <span style={{ fontSize: '16px', fontWeight: 700, color: '#0F172A' }}>
                  {slot.slotNumber}
                </span>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 600,
                    padding: '2px 8px',
                    borderRadius: '10px',
                    backgroundColor: slot.occupied ? '#FEF2F2' : '#DEF7EC',
                    color: slot.occupied ? '#991B1B' : '#03543F'
                  }}
                >
                  {slot.occupied ? 'Occupied' : 'Available'}
                </span>
              </div>

              <div style={{ fontSize: '12px', color: '#64748B', lineHeight: '1.6', marginBottom: '12px' }}>
                <div><strong>Type:</strong> <span style={{ color: getSlotTypeColor(slot.slotType), fontWeight: 600 }}>{slot.slotType}</span></div>
                <div><strong>Floor:</strong> {slot.floorLevel} ({slot.blockName || 'General'})</div>
                {slot.reservedForResidents && <div style={{ color: '#4338CA', fontWeight: 500 }}>🏢 Resident Reserved</div>}
                {slot.occupied && (
                  <div style={{ marginTop: '8px', padding: '8px', backgroundColor: '#FFFFFF', borderRadius: '6px', border: '1px solid #FEF08A' }}>
                    <div><strong>Vehicle:</strong> {slot.occupiedByVehicleNumber || 'Assigned'}</div>
                    {slot.occupiedByVisitorName && <div><strong>Visitor:</strong> {slot.occupiedByVisitorName} ({slot.currentVisitReference})</div>}
                    {slot.occupiedByResidentName && <div><strong>Resident:</strong> {slot.occupiedByResidentName}</div>}
                  </div>
                )}
              </div>

              {isSecurityOrAdmin && !slot.reservedForResidents && (
                <div style={{ borderTop: '1px solid #F1F5F9', paddingTop: '10px' }}>
                  {!slot.occupied ? (
                    <button
                      onClick={() => {
                        setSelectedSlot(slot);
                        setShowAssignVisitorModal(true);
                      }}
                      style={{
                        width: '100%',
                        padding: '6px',
                        fontSize: '12px',
                        fontWeight: 600,
                        backgroundColor: '#1E40AF',
                        color: '#FFFFFF',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer'
                      }}
                    >
                      Assign to Inside Visitor
                    </button>
                  ) : (
                    <button
                      onClick={() => handleReleaseSlot(slot)}
                      style={{
                        width: '100%',
                        padding: '6px',
                        fontSize: '12px',
                        fontWeight: 600,
                        backgroundColor: '#FFFFFF',
                        color: '#DC2626',
                        border: '1px solid #FCA5A5',
                        borderRadius: '6px',
                        cursor: 'pointer'
                      }}
                    >
                      Release Slot
                    </button>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', borderRadius: '12px', overflow: 'hidden' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
            <thead>
              <tr style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0', color: '#475569' }}>
                <th style={{ padding: '12px 16px' }}>Vehicle Number</th>
                <th style={{ padding: '12px 16px' }}>Type</th>
                <th style={{ padding: '12px 16px' }}>Make / Model</th>
                <th style={{ padding: '12px 16px' }}>Color</th>
                <th style={{ padding: '12px 16px' }}>Flat</th>
                <th style={{ padding: '12px 16px' }}>Owner</th>
              </tr>
            </thead>
            <tbody>
              {vehicles.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ padding: '30px', textAlign: 'center', color: '#64748B' }}>
                    No vehicles registered yet.
                  </td>
                </tr>
              ) : (
                vehicles.map((v) => (
                  <tr key={v.id} style={{ borderBottom: '1px solid #F1F5F9' }}>
                    <td style={{ padding: '12px 16px', fontWeight: 700, color: '#0F172A' }}>{v.vehicleNumber}</td>
                    <td style={{ padding: '12px 16px', color: '#0F766E' }}>{v.vehicleType}</td>
                    <td style={{ padding: '12px 16px', color: '#334155' }}>{v.makeModel || '-'}</td>
                    <td style={{ padding: '12px 16px', color: '#475569' }}>{v.color || '-'}</td>
                    <td style={{ padding: '12px 16px', color: '#475569' }}>{v.blockName} - {v.flatNumber}</td>
                    <td style={{ padding: '12px 16px', color: '#475569' }}>{v.ownerName}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Register Vehicle Modal */}
      {showVehicleModal && (
        <Modal title="Register Resident Vehicle" onClose={() => setShowVehicleModal(false)}>
          <form onSubmit={handleRegisterVehicle} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Vehicle Number *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. KA-01-AB-1234"
                value={vehicleNumber}
                onChange={(e) => setVehicleNumber(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px', textTransform: 'uppercase' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Vehicle Type *
              </label>
              <select
                value={vehicleType}
                onChange={(e) => setVehicleType(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="FOUR_WHEELER">4-Wheeler Car / SUV</option>
                <option value="TWO_WHEELER">2-Wheeler Bike / Scooter</option>
                <option value="EV">Electric Vehicle (EV)</option>
              </select>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Make / Model
                </label>
                <input
                  type="text"
                  placeholder="e.g. Hyundai Creta"
                  value={makeModel}
                  onChange={(e) => setMakeModel(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Color
                </label>
                <input
                  type="text"
                  placeholder="e.g. White, Silver"
                  value={color}
                  onChange={(e) => setColor(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Society Parking Sticker Number
              </label>
              <input
                type="text"
                placeholder="e.g. STK-2026-A101"
                value={stickerNumber}
                onChange={(e) => setStickerNumber(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowVehicleModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Save Vehicle
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Assign Visitor Parking Modal */}
      {showAssignVisitorModal && selectedSlot && (
        <Modal title={`Assign Slot ${selectedSlot.slotNumber} to Visitor`} onClose={() => setShowAssignVisitorModal(false)}>
          <form onSubmit={handleAssignVisitor} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Select Admitted Visitor *
              </label>
              <select
                required
                value={selectedVisitId}
                onChange={(e) => setSelectedVisitId(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="">-- Select Active Visitor --</option>
                {insideVisits.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.visitorName} ({v.visitReference}) - Flat {v.destinationFlatNumber} {v.vehicleNumber ? `[${v.vehicleNumber}]` : ''}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Parking Instructions / Notes
              </label>
              <input
                type="text"
                placeholder="e.g. Visitor instructed to park in reverse"
                value={parkingNotes}
                onChange={(e) => setParkingNotes(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowAssignVisitorModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Assign Slot
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
