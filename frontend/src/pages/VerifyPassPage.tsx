import React, { useState } from 'react';
import { useAuth } from '@/app/AuthContext';
import { apiRequest } from '@/lib/api';
import { PassVerificationResponse, Visit } from '@/types';
import { StatusBadge } from '@/components/StatusBadge';
import {
  QrCode,
  Search,
  CheckCircle2,
  XCircle,
  LogIn,
  LogOut,
  MapPin,
  User,
  Clock,
  Car,
  AlertCircle,
} from 'lucide-react';
import { format } from 'date-fns';

export const VerifyPassPage: React.FC = () => {
  const { user } = useAuth();

  const [passCode, setPassCode] = useState('');
  const [verifying, setVerifying] = useState(false);
  const [result, setResult] = useState<PassVerificationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  const handleVerify = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!passCode.trim()) return;

    try {
      setVerifying(true);
      setError(null);
      setActionSuccess(null);
      const res = await apiRequest<PassVerificationResponse>('security/verify-pass', {
        method: 'POST',
        body: JSON.stringify({ code: passCode.trim() }),
      });
      setResult(res);
    } catch (err: any) {
      setError(err.message || 'Verification failed');
      setResult(null);
    } finally {
      setVerifying(false);
    }
  };

  const handleCheckIn = async (visit: Visit) => {
    try {
      setActionLoading(true);
      await apiRequest(`security/check-in/${visit.id}`, {
        method: 'POST',
        body: JSON.stringify({
          gateId: user?.assignedGateId || 1,
          notes: 'Admitted via gate pass verification',
        }),
      });
      setActionSuccess(`Visitor ${visit.visitorName} successfully CHECKED IN to the premises.`);
      // Re-verify to refresh state
      handleVerify();
    } catch (err: any) {
      alert(err.message || 'Check-in failed');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCheckOut = async (visit: Visit) => {
    try {
      setActionLoading(true);
      await apiRequest(`security/check-out/${visit.id}`, {
        method: 'POST',
        body: JSON.stringify({
          gateId: user?.assignedGateId || 1,
          notes: 'Checked out via gate scan',
        }),
      });
      setActionSuccess(`Visitor ${visit.visitorName} successfully CHECKED OUT.`);
      handleVerify();
    } catch (err: any) {
      alert(err.message || 'Check-out failed');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Page Heading */}
      <div className="pb-4 border-b border-border">
        <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
          <QrCode className="w-5 h-5 text-primary" />
          Verify Visitor Pass & Physical Admission
        </h1>
        <p className="text-xs text-text-secondary mt-1">
          Scan QR code payload or type visit reference (GN-YYYYMMDD-XXXX) to inspect permission.
        </p>
      </div>

      {/* Code Input Card */}
      <div className="bg-white border border-border rounded-2xl p-6 shadow-card">
        <form onSubmit={handleVerify} className="space-y-3">
          <label className="block text-xs font-semibold text-text-main">
            Pass Reference Code or QR Payload *
          </label>
          <div className="flex gap-2">
            <div className="relative flex-1">
              <QrCode className="w-4 h-4 text-text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                required
                value={passCode}
                onChange={(e) => setPassCode(e.target.value)}
                placeholder="Scan or enter GN-20261007-XXXX or token..."
                className="w-full pl-10 pr-3 py-2.5 bg-surface border border-border rounded-xl text-xs font-mono outline-none focus:border-primary focus:bg-white focus:ring-2 focus:ring-primary/20 transition-all"
              />
            </div>
            <button
              type="submit"
              disabled={verifying || !passCode.trim()}
              className="px-5 py-2.5 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-xl shadow-sm transition-colors flex items-center gap-1.5 disabled:opacity-50"
            >
              <Search className="w-4 h-4" /> {verifying ? 'Verifying...' : 'Verify Pass'}
            </button>
          </div>
        </form>
      </div>

      {actionSuccess && (
        <div className="p-3.5 bg-status-approved-bg border border-status-approved-border rounded-xl text-xs text-status-approved-text flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Verification Result Card */}
      {result && (
        <div className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-4">
          {/* Validity Banner */}
          <div
            className={`p-4 rounded-xl border flex items-center justify-between gap-3 ${
              result.valid
                ? 'bg-status-approved-bg border-status-approved-border text-status-approved-text'
                : 'bg-status-rejected-bg border-status-rejected-border text-status-rejected-text'
            }`}
          >
            <div className="flex items-center gap-2.5">
              {result.valid ? (
                <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
              ) : (
                <XCircle className="w-5 h-5 flex-shrink-0" />
              )}
              <div>
                <div className="font-bold text-xs">
                  {result.valid ? 'VALID VISITOR PASS' : 'INVALID OR UNUSABLE PASS'}
                </div>
                <div className="text-[11px] opacity-90 mt-0.5">{result.reason}</div>
              </div>
            </div>

            {result.visit && <StatusBadge status={result.visit.status} size="sm" />}
          </div>

          {/* Visitor & Destination Details if found */}
          {result.visit && (
            <div className="space-y-3 pt-2 text-xs">
              <div className="grid grid-cols-2 gap-3 p-3.5 bg-surface rounded-xl border border-border">
                <div>
                  <span className="text-[10px] text-text-muted block">Visitor</span>
                  <strong className="text-text-main text-sm font-bold">
                    {result.visit.visitorName}
                  </strong>
                  <div className="text-[11px] text-text-secondary mt-0.5">
                    {result.visit.visitorCategory} • {result.visit.visitorMobile}
                  </div>
                </div>

                <div>
                  <span className="text-[10px] text-text-muted block">Destination Flat</span>
                  <strong className="text-primary text-sm font-bold">
                    {result.visit.destinationBlockName} — Flat {result.visit.destinationFlatNumber}
                  </strong>
                  <div className="text-[11px] text-text-secondary mt-0.5">
                    Host: {result.visit.hostNameSnapshot} ({result.visit.hostMobileSnapshot})
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3 text-text-secondary">
                <div className="flex items-center gap-2 p-2.5 bg-surface/50 rounded-lg border border-border-light">
                  <Clock className="w-4 h-4 text-primary flex-shrink-0" />
                  <div>
                    <div className="text-[10px] text-text-muted">Arrival Window</div>
                    <div className="font-medium text-text-main text-[11px]">
                      {format(new Date(result.visit.arrivalWindowStart), 'dd MMM, hh:mm a')} –{' '}
                      {format(new Date(result.visit.arrivalWindowEnd), 'hh:mm a')}
                    </div>
                  </div>
                </div>

                {result.visit.vehicleNumber && (
                  <div className="flex items-center gap-2 p-2.5 bg-surface/50 rounded-lg border border-border-light">
                    <Car className="w-4 h-4 text-text-secondary flex-shrink-0" />
                    <div>
                      <div className="text-[10px] text-text-muted">Registered Vehicle</div>
                      <div className="font-mono font-bold text-text-main text-[11px]">
                        {result.visit.vehicleNumber}
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Physical Action Buttons */}
              <div className="pt-4 border-t border-border flex justify-end gap-3">
                {result.canCheckIn && (
                  <button
                    onClick={() => handleCheckIn(result.visit!)}
                    disabled={actionLoading}
                    className="px-5 py-2.5 bg-[#137547] hover:bg-[#0f5c38] text-white font-bold rounded-xl shadow-sm text-xs flex items-center gap-2 transition-colors disabled:opacity-50"
                  >
                    <LogIn className="w-4 h-4" /> Check In Visitor Now
                  </button>
                )}

                {result.canCheckOut && (
                  <button
                    onClick={() => handleCheckOut(result.visit!)}
                    disabled={actionLoading}
                    className="px-5 py-2.5 bg-primary hover:bg-primary-hover text-white font-bold rounded-xl shadow-sm text-xs flex items-center gap-2 transition-colors disabled:opacity-50"
                  >
                    <LogOut className="w-4 h-4" /> Check Out Visitor Now
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
