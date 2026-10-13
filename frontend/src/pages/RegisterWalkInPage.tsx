import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { apiRequest } from '@/lib/api';
import { Block, Flat, ResidentProfile, VisitorCategory, Visit, Gate } from '@/types';
import { UserPlus, User, Phone, MapPin, Car, CheckCircle2, AlertCircle } from 'lucide-react';

export const RegisterWalkInPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [blocks, setBlocks] = useState<Block[]>([]);
  const [flats, setFlats] = useState<Flat[]>([]);
  const [gates, setGates] = useState<Gate[]>([]);
  const [selectedBlockId, setSelectedBlockId] = useState<string>('');
  const [selectedFlatId, setSelectedFlatId] = useState<string>('');
  const [residents, setResidents] = useState<ResidentProfile[]>([]);
  const [selectedHostId, setSelectedHostId] = useState<string>('');

  // Visitor Form Fields
  const [visitorName, setVisitorName] = useState('');
  const [visitorMobile, setVisitorMobile] = useState('');
  const [visitorCategory, setVisitorCategory] = useState<VisitorCategory>('GUEST');
  const [purpose, setPurpose] = useState('');
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [notes, setNotes] = useState('');
  const [arrivalGateId, setArrivalGateId] = useState<string>(
    user?.assignedGateId ? String(user.assignedGateId) : '1'
  );

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [registeredVisit, setRegisteredVisit] = useState<Visit | null>(null);

  useEffect(() => {
    const loadStructure = async () => {
      try {
        const [bData, fData, gData] = await Promise.all([
          apiRequest<Block[]>('community/blocks'),
          apiRequest<Flat[]>('community/flats'),
          apiRequest<Gate[]>('community/gates'),
        ]);
        setBlocks(bData || []);
        setFlats(fData || []);
        setGates(gData || []);
        if (bData && bData.length > 0) {
          setSelectedBlockId(String(bData[0].id));
        }
      } catch (err) {}
    };
    loadStructure();
  }, []);

  // When flat changes, load approved residents living in this flat
  useEffect(() => {
    if (!selectedFlatId) {
      setResidents([]);
      setSelectedHostId('');
      return;
    }

    const loadFlatResidents = async () => {
      try {
        const res = await apiRequest<ResidentProfile[]>(
          `admin/residents?flatId=${selectedFlatId}&status=APPROVED`
        );
        // Note: admin/residents returns paged or list
        const items = Array.isArray(res) ? res : (res as any).items || [];
        setResidents(items);
        if (items.length > 0) {
          setSelectedHostId(String(items[0].userId));
        } else {
          setSelectedHostId('');
        }
      } catch (err) {
        setResidents([]);
      }
    };
    loadFlatResidents();
  }, [selectedFlatId]);

  const filteredFlats = flats.filter((f) => String(f.blockId) === selectedBlockId);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedHostId) {
      setError('Please select an active host resident for this flat.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const visit = await apiRequest<Visit>('security/walk-in', {
        method: 'POST',
        body: JSON.stringify({
          hostUserId: Number(selectedHostId),
          visitorName,
          visitorMobile,
          visitorCategory,
          purpose,
          vehicleNumber,
          notes,
          arrivalGateId: Number(arrivalGateId),
          expectedDurationMinutes: 120,
        }),
      });
      setRegisteredVisit(visit);
    } catch (err: any) {
      setError(err.message || 'Walk-in registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Page Heading */}
      <div className="pb-4 border-b border-border">
        <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
          <UserPlus className="w-5 h-5 text-primary" />
          Register Walk-In Visitor at Gate
        </h1>
        <p className="text-xs text-text-secondary mt-1">
          Record unexpected visitor details and instantly send an approval request to the host resident.
        </p>
      </div>

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {registeredVisit ? (
        <div className="bg-white border border-border rounded-2xl p-6 shadow-card text-center space-y-4">
          <div className="w-12 h-12 rounded-full bg-status-pending-bg text-status-pending-text border border-status-pending-border flex items-center justify-center mx-auto">
            <CheckCircle2 className="w-6 h-6" />
          </div>
          <h2 className="text-base font-bold text-text-main">Walk-In Request Dispatched</h2>
          <p className="text-xs text-text-secondary leading-relaxed max-w-md mx-auto">
            Visit request for <strong className="text-text-main">{registeredVisit.visitorName}</strong> has been sent to{' '}
            <strong className="text-primary">{registeredVisit.hostNameSnapshot}</strong> (Flat {registeredVisit.destinationFlatNumber}).
          </p>

          <div className="p-3 bg-surface rounded-xl border border-border text-xs text-left max-w-sm mx-auto font-mono">
            <div>Reference: <strong>{registeredVisit.visitReference}</strong></div>
            <div>Status: <span className="text-status-pending-text font-bold">Awaiting Resident Approval</span></div>
          </div>

          <div className="flex justify-center gap-3 pt-2">
            <button
              onClick={() => {
                setRegisteredVisit(null);
                setVisitorName('');
                setVisitorMobile('');
                setPurpose('');
                setVehicleNumber('');
              }}
              className="px-4 py-2 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors"
            >
              Register Another Visitor
            </button>
            <button
              onClick={() => navigate('/security/waiting-approvals')}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
            >
              View Waiting Queue
            </button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-4 text-xs">
          {/* Destination & Host Selector */}
          <div className="p-4 bg-surface rounded-xl border border-border space-y-3">
            <div className="font-bold text-text-main flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-primary" />
              1. Select Destination & Host Resident
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-text-secondary mb-1">Block / Tower *</label>
                <select
                  value={selectedBlockId}
                  onChange={(e) => {
                    setSelectedBlockId(e.target.value);
                    setSelectedFlatId('');
                  }}
                  className="w-full px-3 py-2 bg-white border border-border rounded-lg text-xs outline-none focus:border-primary"
                >
                  {blocks.map((b) => (
                    <option key={b.id} value={String(b.id)}>
                      {b.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-text-secondary mb-1">Flat Number *</label>
                <select
                  required
                  value={selectedFlatId}
                  onChange={(e) => setSelectedFlatId(e.target.value)}
                  className="w-full px-3 py-2 bg-white border border-border rounded-lg text-xs outline-none focus:border-primary"
                >
                  <option value="">Select Flat...</option>
                  {filteredFlats.map((f) => (
                    <option key={f.id} value={String(f.id)}>
                      Flat {f.flatNumber} (Floor {f.floorNumber})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block font-semibold text-text-secondary mb-1">Host Resident *</label>
              {residents.length === 0 ? (
                <div className="text-[11px] text-text-muted italic bg-white p-2 rounded border border-border-light">
                  {selectedFlatId ? 'No approved residents found in this flat.' : 'Select a flat to view residents.'}
                </div>
              ) : (
                <select
                  required
                  value={selectedHostId}
                  onChange={(e) => setSelectedHostId(e.target.value)}
                  className="w-full px-3 py-2 bg-white border border-border rounded-lg text-xs outline-none focus:border-primary font-medium"
                >
                  {residents.map((r) => (
                    <option key={r.userId} value={String(r.userId)}>
                      {r.userFullName} ({r.occupancyType} • {r.userMobile})
                    </option>
                  ))}
                </select>
              )}
            </div>
          </div>

          {/* Visitor Details */}
          <div className="space-y-3 pt-2">
            <div className="font-bold text-text-main flex items-center gap-1.5">
              <User className="w-4 h-4 text-primary" />
              2. Visitor Information
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-text-main mb-1">Visitor Full Name *</label>
                <input
                  type="text"
                  required
                  value={visitorName}
                  onChange={(e) => setVisitorName(e.target.value)}
                  placeholder="E.g. Vikram Seth"
                  className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                />
              </div>

              <div>
                <label className="block font-semibold text-text-main mb-1">Visitor Mobile *</label>
                <input
                  type="tel"
                  required
                  value={visitorMobile}
                  onChange={(e) => setVisitorMobile(e.target.value)}
                  placeholder="+91 98765 00000"
                  className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-text-main mb-1">Category *</label>
                <select
                  value={visitorCategory}
                  onChange={(e) => setVisitorCategory(e.target.value as VisitorCategory)}
                  className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                >
                  <option value="GUEST">Guest / Family</option>
                  <option value="DELIVERY">Delivery Agent (Food / Parcel)</option>
                  <option value="SERVICE">Home Service / Maintenance</option>
                  <option value="CAB">Cab / Taxi Pickup</option>
                  <option value="OTHER">Other Purpose</option>
                </select>
              </div>

              <div>
                <label className="block font-semibold text-text-main mb-1">Purpose of Visit *</label>
                <input
                  type="text"
                  required
                  value={purpose}
                  onChange={(e) => setPurpose(e.target.value)}
                  placeholder="E.g. Food Delivery / AC Repair"
                  className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block font-semibold text-text-main mb-1">Vehicle Number (Optional)</label>
                <input
                  type="text"
                  value={vehicleNumber}
                  onChange={(e) => setVehicleNumber(e.target.value)}
                  placeholder="KA-01-AB-1234"
                  className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white uppercase"
                />
              </div>

              <div>
                <label className="block font-semibold text-text-main mb-1">Arrival Gate</label>
                <select
                  value={arrivalGateId}
                  onChange={(e) => setArrivalGateId(e.target.value)}
                  className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                >
                  {gates.map((g) => (
                    <option key={g.id} value={String(g.id)}>
                      {g.name} ({g.code})
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div className="pt-4 border-t border-border flex justify-end">
            <button
              type="submit"
              disabled={loading || !selectedHostId}
              className="px-5 py-2.5 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors disabled:opacity-50 flex items-center gap-1.5"
            >
              {loading ? 'Dispatching Approval Request...' : 'Send Walk-In Request to Resident'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
