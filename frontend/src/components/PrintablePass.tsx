import React from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { Logo } from './Logo';
import { Printer, Calendar, Clock, MapPin, User, ShieldCheck } from 'lucide-react';
import { format } from 'date-fns';

interface PrintablePassProps {
  visitReference: string;
  visitorName: string;
  visitorCategory?: string;
  purpose?: string;
  vehicleNumber?: string;
  blockName: string;
  flatNumber: string;
  hostName: string;
  arrivalWindowStart: string;
  arrivalWindowEnd: string;
  passToken?: string;
  qrCodeDataUrl?: string;
}

export const PrintablePass: React.FC<PrintablePassProps> = ({
  visitReference,
  visitorName,
  visitorCategory = 'GUEST',
  purpose,
  vehicleNumber,
  blockName,
  flatNumber,
  hostName,
  arrivalWindowStart,
  arrivalWindowEnd,
  passToken,
  qrCodeDataUrl,
}) => {
  const qrValue = passToken
    ? `GATENEST:${visitReference}:${passToken}`
    : visitReference;

  return (
    <div className="flex flex-col items-center">
      {/* Action Bar (hidden when printing) */}
      <div className="w-full flex justify-end mb-4 no-print">
        <button
          onClick={() => window.print()}
          className="inline-flex items-center gap-2 px-4 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors"
        >
          <Printer className="w-4 h-4" />
          Print Visitor Pass
        </button>
      </div>

      {/* The Printable Pass Card */}
      <div className="printable-card w-full max-w-md bg-white border border-border rounded-2xl p-6 shadow-card overflow-hidden">
        {/* Pass Header */}
        <div className="flex items-center justify-between border-b border-border pb-4 mb-4">
          <Logo size="sm" />
          <div className="text-right">
            <span className="text-[10px] uppercase tracking-wider font-bold text-secondary-teal bg-teal-light px-2 py-0.5 rounded-full">
              Official Entry Pass
            </span>
            <div className="text-xs font-mono font-bold text-text-main mt-1">
              {visitReference}
            </div>
          </div>
        </div>

        {/* QR Code Section */}
        <div className="flex flex-col items-center justify-center p-4 bg-surface rounded-xl border border-border mb-4">
          {qrCodeDataUrl ? (
            <img src={qrCodeDataUrl} alt="Visitor Pass QR" className="w-44 h-44 rounded-lg bg-white p-2 border border-border" />
          ) : (
            <div className="p-2 bg-white rounded-lg border border-border">
              <QRCodeSVG value={qrValue} size={160} level="M" />
            </div>
          )}
          <span className="text-[11px] font-medium text-text-secondary mt-2">
            Scan at gate scanner or present reference code
          </span>
        </div>

        {/* Visitor & Destination Details Grid */}
        <div className="space-y-3 text-xs">
          <div className="flex justify-between items-center py-1.5 border-b border-border-light">
            <span className="text-text-secondary flex items-center gap-1.5">
              <User className="w-3.5 h-3.5 text-primary" /> Visitor Name
            </span>
            <span className="font-bold text-text-main text-sm">{visitorName}</span>
          </div>

          <div className="flex justify-between items-center py-1.5 border-b border-border-light">
            <span className="text-text-secondary flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5 text-secondary-teal" /> Category & Purpose
            </span>
            <span className="font-semibold text-text-main text-right">
              {visitorCategory} {purpose ? `• ${purpose}` : ''}
            </span>
          </div>

          <div className="flex justify-between items-center py-1.5 border-b border-border-light">
            <span className="text-text-secondary flex items-center gap-1.5">
              <MapPin className="w-3.5 h-3.5 text-primary" /> Destination
            </span>
            <span className="font-bold text-primary text-sm">
              {blockName} — Flat {flatNumber}
            </span>
          </div>

          <div className="flex justify-between items-center py-1.5 border-b border-border-light">
            <span className="text-text-secondary">Host Resident</span>
            <span className="font-semibold text-text-main">{hostName}</span>
          </div>

          {vehicleNumber && (
            <div className="flex justify-between items-center py-1.5 border-b border-border-light">
              <span className="text-text-secondary">Vehicle Number</span>
              <span className="font-mono font-semibold text-text-main">{vehicleNumber}</span>
            </div>
          )}

          <div className="bg-accent-blue/50 p-3 rounded-lg border border-accent-blue space-y-1">
            <div className="flex items-center gap-1.5 text-[11px] font-semibold text-primary">
              <Clock className="w-3.5 h-3.5" /> Valid Arrival Window
            </div>
            <div className="text-[11px] text-text-main font-medium">
              {format(new Date(arrivalWindowStart), 'dd MMM yyyy, hh:mm a')}
              <br />
              <span className="text-text-secondary">until</span>{' '}
              {format(new Date(arrivalWindowEnd), 'dd MMM yyyy, hh:mm a')}
            </div>
          </div>
        </div>

        {/* Pass Footer */}
        <div className="mt-4 pt-3 border-t border-border text-center text-[10px] text-text-muted">
          Issued by GateNest Community Security • Non-transferable • Single admission
        </div>
      </div>
    </div>
  );
};
