import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiRequest } from '@/lib/api';
import { Visit, VisitEvent, PassCredential } from '@/types';
import { StatusBadge } from '@/components/StatusBadge';
import { Timeline } from '@/components/Timeline';
import { Modal } from '@/components/Modal';
import { PrintablePass } from '@/components/PrintablePass';
import {
  Calendar,
  Clock,
  User,
  Phone,
  MapPin,
  Car,
  FileText,
  Printer,
  RefreshCw,
  ArrowLeft,
  ShieldCheck,
  AlertCircle,
} from 'lucide-react';
import { format } from 'date-fns';

export const VisitDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [visit, setVisit] = useState<Visit | null>(null);
  const [timeline, setTimeline] = useState<VisitEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Pass Modal
  const [passModalOpen, setPassModalOpen] = useState(false);
  const [regeneratedPass, setRegeneratedPass] = useState<PassCredential | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    const loadDetails = async () => {
      try {
        setLoading(true);
        setError(null);
        const [vData, tData] = await Promise.all([
          apiRequest<Visit>(`visits/${id}`),
          apiRequest<VisitEvent[]>(`visits/${id}/timeline`),
        ]);
        setVisit(vData);
        setTimeline(tData || []);
      } catch (err: any) {
        setError(err.message || 'Failed to load visit details');
      } finally {
        setLoading(false);
      }
    };
    if (id) loadDetails();
  }, [id]);

  const handleRegenerate = async () => {
    if (!visit) return;
    try {
      setActionLoading(true);
      const cred = await apiRequest<PassCredential>(`resident/visits/${visit.id}/regenerate-pass`, {
        method: 'POST',
      });
      setRegeneratedPass(cred);
      setPassModalOpen(true);
      // Reload details
      const [vData, tData] = await Promise.all([
        apiRequest<Visit>(`visits/${id}`),
        apiRequest<VisitEvent[]>(`visits/${id}/timeline`),
      ]);
      setVisit(vData);
      setTimeline(tData || []);
    } catch (err: any) {
      alert(err.message || 'Pass regeneration failed');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-xs text-text-secondary">Loading visit record...</div>;
  }

  if (error || !visit) {
    return (
      <div className="p-8 max-w-md mx-auto bg-white border border-border rounded-xl text-center space-y-3">
        <AlertCircle className="w-8 h-8 text-status-rejected-text mx-auto" />
        <div className="text-sm font-bold text-text-main">Visit Record Not Found</div>
        <p className="text-xs text-text-secondary">{error || 'Unable to access visit details.'}</p>
        <button
          onClick={() => navigate(-1)}
          className="px-4 py-2 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border"
        >
          Go Back
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Top Breadcrumb & Actions */}
      <div className="flex items-center justify-between gap-4 pb-4 border-b border-border">
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-1.5 text-xs font-semibold text-text-secondary hover:text-text-main"
        >
          <ArrowLeft className="w-4 h-4" /> Back to Visits
        </button>

        <div className="flex items-center gap-2">
          {visit.status === 'APPROVED' && (
            <button
              onClick={handleRegenerate}
              disabled={actionLoading}
              className="px-3 py-1.5 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border flex items-center gap-1.5 transition-colors"
            >
              <RefreshCw className="w-3.5 h-3.5" /> Regenerate Pass
            </button>
          )}

          <button
            onClick={() => setPassModalOpen(true)}
            className="px-3.5 py-1.5 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm flex items-center gap-1.5 transition-colors"
          >
            <Printer className="w-3.5 h-3.5" /> Printable Pass
          </button>
        </div>
      </div>

      {/* Main Details Card */}
      <div className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-6">
        {/* Header Summary */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-border">
          <div>
            <div className="font-mono text-xs font-bold text-primary">{visit.visitReference}</div>
            <h1 className="text-xl font-bold text-text-main mt-0.5">{visit.visitorName}</h1>
            <span className="text-xs text-text-secondary">{visit.visitorCategory} Visitor</span>
          </div>
          <div>
            <StatusBadge status={visit.status} size="md" />
          </div>
        </div>

        {/* 2-Column Info Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
          {/* Visitor Details */}
          <div className="p-4 bg-surface rounded-xl border border-border space-y-2.5">
            <div className="font-bold text-text-main flex items-center gap-1.5">
              <User className="w-4 h-4 text-primary" /> Visitor Information
            </div>
            <div className="flex justify-between">
              <span className="text-text-secondary">Mobile:</span>
              <span className="font-mono font-semibold text-text-main">{visit.visitorMobile}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-secondary">Purpose:</span>
              <span className="font-medium text-text-main text-right">{visit.purpose}</span>
            </div>
            {visit.vehicleNumber && (
              <div className="flex justify-between">
                <span className="text-text-secondary">Vehicle:</span>
                <span className="font-mono font-bold text-text-main">{visit.vehicleNumber}</span>
              </div>
            )}
            {visit.notes && (
              <div className="pt-2 border-t border-border-light text-text-secondary">
                <strong>Notes:</strong> {visit.notes}
              </div>
            )}
          </div>

          {/* Destination Snapshot */}
          <div className="p-4 bg-surface rounded-xl border border-border space-y-2.5">
            <div className="font-bold text-text-main flex items-center gap-1.5">
              <MapPin className="w-4 h-4 text-primary" /> Destination & Host Snapshot
            </div>
            <div className="flex justify-between">
              <span className="text-text-secondary">Destination Flat:</span>
              <strong className="text-primary">
                {visit.destinationBlockName} — Flat {visit.destinationFlatNumber}
              </strong>
            </div>
            <div className="flex justify-between">
              <span className="text-text-secondary">Host Resident:</span>
              <span className="font-semibold text-text-main">{visit.hostNameSnapshot}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-secondary">Host Mobile:</span>
              <span className="font-mono text-text-main">{visit.hostMobileSnapshot}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-text-secondary">Permitted Window:</span>
              <span className="font-medium text-text-main text-right">
                {format(new Date(visit.arrivalWindowStart), 'dd MMM, hh:mm a')}
              </span>
            </div>
          </div>
        </div>

        {/* Physical Gate Check-in Stats */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 p-3.5 bg-accent-blue/40 rounded-xl border border-accent-blue text-xs">
          <div>
            <span className="text-text-secondary text-[11px] block">Entry Check-In</span>
            <strong className="text-text-main">
              {visit.checkInTime ? format(new Date(visit.checkInTime), 'dd MMM yyyy, hh:mm a') : 'Not Checked In'}
            </strong>
            {visit.entryGateName && <div className="text-[10px] text-text-muted">Gate: {visit.entryGateName}</div>}
          </div>

          <div>
            <span className="text-text-secondary text-[11px] block">Exit Check-Out</span>
            <strong className="text-text-main">
              {visit.checkOutTime ? format(new Date(visit.checkOutTime), 'dd MMM yyyy, hh:mm a') : 'Not Exited'}
            </strong>
            {visit.exitGateName && <div className="text-[10px] text-text-muted">Gate: {visit.exitGateName}</div>}
          </div>

          <div>
            <span className="text-text-secondary text-[11px] block">Expected Stay</span>
            <strong className="text-text-main">{visit.expectedDurationMinutes} Minutes</strong>
            {visit.overstay && (
              <span className="block text-[10px] font-bold text-[#C02B10] mt-0.5">Overstay Detected</span>
            )}
          </div>
        </div>

        {/* Timeline Event Log */}
        <div className="pt-4 border-t border-border">
          <h2 className="text-sm font-bold text-text-main mb-4 flex items-center gap-2">
            <Clock className="w-4 h-4 text-primary" />
            Immutable Visit Timeline & Lifecycle Events
          </h2>
          <Timeline events={timeline} />
        </div>
      </div>

      {/* Printable Pass Modal */}
      <Modal
        isOpen={passModalOpen}
        onClose={() => setPassModalOpen(false)}
        title="Official Visitor Pass"
        maxWidth="md"
      >
        <PrintablePass
          visitReference={regeneratedPass?.visitReference || visit.visitReference}
          visitorName={regeneratedPass?.visitorName || visit.visitorName}
          visitorCategory={visit.visitorCategory}
          purpose={visit.purpose}
          vehicleNumber={visit.vehicleNumber}
          blockName={regeneratedPass?.destinationBlockName || visit.destinationBlockName}
          flatNumber={regeneratedPass?.destinationFlatNumber || visit.destinationFlatNumber}
          hostName={regeneratedPass?.hostName || visit.hostNameSnapshot}
          arrivalWindowStart={regeneratedPass?.arrivalWindowStart || visit.arrivalWindowStart}
          arrivalWindowEnd={regeneratedPass?.arrivalWindowEnd || visit.arrivalWindowEnd}
          passToken={regeneratedPass?.passToken}
          qrCodeDataUrl={regeneratedPass?.qrCodeDataUrl}
        />
      </Modal>
    </div>
  );
};
