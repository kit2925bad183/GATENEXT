import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { apiRequest } from '@/lib/api';
import { PassCredential, VisitorCategory } from '@/types';
import { PrintablePass } from '@/components/PrintablePass';
import { Send, User, Phone, Calendar, Clock, Car, FileText, AlertCircle } from 'lucide-react';
import { format, addHours } from 'date-fns';

export const InviteVisitorPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const now = new Date();
  const defaultStart = format(now, "yyyy-MM-dd'T'HH:mm");
  const defaultEnd = format(addHours(now, 12), "yyyy-MM-dd'T'HH:mm");

  const [visitorName, setVisitorName] = useState('');
  const [visitorMobile, setVisitorMobile] = useState('');
  const [visitorCategory, setVisitorCategory] = useState<VisitorCategory>('GUEST');
  const [purpose, setPurpose] = useState('');
  const [vehicleNumber, setVehicleNumber] = useState('');
  const [notes, setNotes] = useState('');
  const [windowStart, setWindowStart] = useState(defaultStart);
  const [windowEnd, setWindowEnd] = useState(defaultEnd);
  const [duration, setDuration] = useState(120);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [createdPass, setCreatedPass] = useState<PassCredential | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError(null);
      const cred = await apiRequest<PassCredential>('resident/invitations', {
        method: 'POST',
        body: JSON.stringify({
          visitorName,
          visitorMobile,
          visitorCategory,
          purpose,
          vehicleNumber,
          notes,
          arrivalWindowStart: new Date(windowStart).toISOString(),
          arrivalWindowEnd: new Date(windowEnd).toISOString(),
          expectedDurationMinutes: Number(duration),
        }),
      });
      setCreatedPass(cred);
    } catch (err: any) {
      setError(err.message || 'Failed to create invitation');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Page Heading */}
      <div className="pb-4 border-b border-border">
        <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
          <Send className="w-5 h-5 text-primary" />
          Invite Visitor & Issue QR Pass
        </h1>
        <p className="text-xs text-text-secondary mt-1">
          Create an authorized entry pass for personal guests, delivery personnel, service agents, or cabs.
        </p>
      </div>

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {createdPass ? (
        <div className="space-y-6">
          <div className="bg-white border border-border rounded-2xl p-6 shadow-card">
            <PrintablePass
              visitReference={createdPass.visitReference}
              visitorName={createdPass.visitorName}
              visitorCategory={visitorCategory}
              purpose={purpose}
              vehicleNumber={vehicleNumber}
              blockName={createdPass.destinationBlockName}
              flatNumber={createdPass.destinationFlatNumber}
              hostName={createdPass.hostName}
              arrivalWindowStart={createdPass.arrivalWindowStart}
              arrivalWindowEnd={createdPass.arrivalWindowEnd}
              passToken={createdPass.passToken}
              qrCodeDataUrl={createdPass.qrCodeDataUrl}
            />
          </div>

          <div className="flex justify-center gap-3 no-print">
            <button
              onClick={() => {
                setCreatedPass(null);
                setVisitorName('');
                setVisitorMobile('');
                setPurpose('');
                setVehicleNumber('');
              }}
              className="px-4 py-2 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors"
            >
              Create Another Pass
            </button>
            <button
              onClick={() => navigate('/resident/upcoming')}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
            >
              View Upcoming Passes
            </button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-4 text-xs">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">Visitor Full Name *</label>
              <div className="relative">
                <User className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  required
                  value={visitorName}
                  onChange={(e) => setVisitorName(e.target.value)}
                  placeholder="E.g. Ananya Rao"
                  className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white"
                />
              </div>
            </div>

            <div>
              <label className="block font-semibold text-text-main mb-1">Visitor Mobile *</label>
              <div className="relative">
                <Phone className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="tel"
                  required
                  value={visitorMobile}
                  onChange={(e) => setVisitorMobile(e.target.value)}
                  placeholder="+91 98765 12345"
                  className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white"
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">Category *</label>
              <select
                value={visitorCategory}
                onChange={(e) => setVisitorCategory(e.target.value as VisitorCategory)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white"
              >
                <option value="GUEST">Guest / Family / Friend</option>
                <option value="DELIVERY">Delivery (Amazon / Swiggy / Courier)</option>
                <option value="SERVICE">Service (Cleaning / Appliance / Maid)</option>
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
                placeholder="E.g. Dinner Guest / Package Delivery"
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white"
              />
            </div>
          </div>

          {/* Arrival Window Date & Time */}
          <div className="p-3.5 bg-surface rounded-xl border border-border space-y-3">
            <div className="font-semibold text-text-main flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-primary" /> Arrival Validity Window
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block text-text-secondary text-[11px] mb-1">Valid From *</label>
                <input
                  type="datetime-local"
                  required
                  value={windowStart}
                  onChange={(e) => setWindowStart(e.target.value)}
                  className="w-full px-3 py-1.5 bg-white border border-border rounded-lg text-xs outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-text-secondary text-[11px] mb-1">Valid Until *</label>
                <input
                  type="datetime-local"
                  required
                  value={windowEnd}
                  onChange={(e) => setWindowEnd(e.target.value)}
                  className="w-full px-3 py-1.5 bg-white border border-border rounded-lg text-xs outline-none focus:border-primary"
                />
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">Expected Stay Duration (Minutes)</label>
              <input
                type="number"
                min={15}
                max={1440}
                required
                value={duration}
                onChange={(e) => setDuration(Number(e.target.value))}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white"
              />
            </div>

            <div>
              <label className="block font-semibold text-text-main mb-1">Vehicle Registration (Optional)</label>
              <input
                type="text"
                value={vehicleNumber}
                onChange={(e) => setVehicleNumber(e.target.value)}
                placeholder="MH-02-CD-5678"
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white uppercase"
              />
            </div>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Notes for Gate Security (Optional)</label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="E.g. Please allow parking in slot B-101"
              className="w-full p-2.5 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="pt-4 border-t border-border flex justify-end">
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors disabled:opacity-50 flex items-center gap-1.5"
            >
              {loading ? 'Issuing Pass...' : 'Generate Visitor Pass & QR Code'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
