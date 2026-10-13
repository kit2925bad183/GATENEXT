import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { PassVerificationResponse, Visit, Gate } from '../types';

export const SecurityGateDeskPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();

  const [searchCode, setSearchCode] = useState<string>('');
  const [verifying, setVerifying] = useState<boolean>(false);
  const [verificationResult, setVerificationResult] = useState<PassVerificationResponse | null>(null);
  const [actionMessage, setActionMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [waitingApprovalsCount, setWaitingApprovalsCount] = useState<number>(0);
  const [currentInsideCount, setCurrentInsideCount] = useState<number>(0);
  const [pendingParcelsCount, setPendingParcelsCount] = useState<number>(0);
  const [gates, setGates] = useState<Gate[]>([]);
  const [selectedGateId, setSelectedGateId] = useState<number>(user?.assignedGateId || 1);

  const fetchStats = async () => {
    try {
      const [visitsRes, gatesRes, reportsRes] = await Promise.all([
        api.get<{ data: { items?: Visit[] } | Visit[] }>('/visits?status=PENDING_APPROVAL&limit=100'),
        api.get<{ data: Gate[] }>('/community/gates'),
        api.get<{ data: Record<string, number> }>('/reports/dashboard')
      ]);

      const waiting = Array.isArray(visitsRes.data?.data) ? visitsRes.data?.data : (visitsRes.data?.data?.items || []);
      setWaitingApprovalsCount(waiting.length);

      const gList = gatesRes.data?.data || [];
      setGates(gList);

      const stats = reportsRes.data?.data || {};
      setCurrentInsideCount(stats.currentlyInside || 0);
      setPendingParcelsCount(stats.pendingParcels || 0);
    } catch (err) {
      console.error('Failed to poll gate stats', err);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 10000); // Poll every 10s
    return () => clearInterval(interval);
  }, []);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchCode.trim()) return;

    setVerifying(true);
    setErrorMessage(null);
    setActionMessage(null);
    setVerificationResult(null);

    try {
      const res = await api.post<{ data: PassVerificationResponse }>('/security/verify-pass', {
        passToken: searchCode.trim(),
        gateId: selectedGateId
      });
      setVerificationResult(res.data.data);
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || 'Verification failed. Invalid pass or reference.');
    } finally {
      setVerifying(false);
    }
  };

  const handleCheckIn = async () => {
    if (!verificationResult?.visit) return;
    try {
      await api.post(`/security/visits/${verificationResult.visit.id}/check-in`, {
        gateId: selectedGateId,
        vehicleNumber: verificationResult.visit.vehicleNumber
      });
      setActionMessage(`Visitor ${verificationResult.visit.visitorName} successfully admitted inside.`);
      setVerificationResult(null);
      setSearchCode('');
      fetchStats();
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || 'Failed to check in visitor');
    }
  };

  const handleCheckOut = async () => {
    if (!verificationResult?.visit) return;
    try {
      await api.post(`/security/visits/${verificationResult.visit.id}/check-out`, {
        gateId: selectedGateId
      });
      setActionMessage(`Visitor ${verificationResult.visit.visitorName} marked as exited.`);
      setVerificationResult(null);
      setSearchCode('');
      fetchStats();
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || 'Failed to check out visitor');
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto' }}>
      {/* Top Banner with Gate info and live stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '14px', marginBottom: '24px' }}>
        <div style={{ backgroundColor: '#1E40AF', color: '#FFFFFF', padding: '18px', borderRadius: '12px', boxShadow: '0 2px 4px rgba(30,64,175,0.2)' }}>
          <div style={{ fontSize: '12px', color: '#BFDBFE', fontWeight: 600, textTransform: 'uppercase' }}>Active Gate Terminal</div>
          <div style={{ fontSize: '18px', fontWeight: 700, marginTop: '4px' }}>
            {user?.assignedGateName || 'North Main Gate'}
          </div>
          <div style={{ fontSize: '12px', color: '#DBEAFE', marginTop: '4px' }}>
            Guard: {user?.fullName}
          </div>
        </div>

        <div
          onClick={() => navigate('/waiting-approvals')}
          style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', padding: '18px', borderRadius: '12px', cursor: 'pointer', transition: 'all 0.15s ease' }}
        >
          <div style={{ fontSize: '12px', color: '#64748B', fontWeight: 600 }}>{t('gate.waitingCount')}</div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: waitingApprovalsCount > 0 ? '#DC2626' : '#0F172A', marginTop: '2px' }}>
            {waitingApprovalsCount}
          </div>
          <div style={{ fontSize: '11px', color: '#3B82F6', marginTop: '2px' }}>Click to view waiting queue →</div>
        </div>

        <div
          onClick={() => navigate('/currently-inside')}
          style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', padding: '18px', borderRadius: '12px', cursor: 'pointer' }}
        >
          <div style={{ fontSize: '12px', color: '#64748B', fontWeight: 600 }}>{t('dashboard.visitorsInside')}</div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: '#0F172A', marginTop: '2px' }}>
            {currentInsideCount}
          </div>
          <div style={{ fontSize: '11px', color: '#3B82F6', marginTop: '2px' }}>View occupancy roster →</div>
        </div>

        <div
          onClick={() => navigate('/parcels')}
          style={{ backgroundColor: '#FFFFFF', border: '1px solid #E2E8F0', padding: '18px', borderRadius: '12px', cursor: 'pointer' }}
        >
          <div style={{ fontSize: '12px', color: '#64748B', fontWeight: 600 }}>{t('dashboard.pendingParcels')}</div>
          <div style={{ fontSize: '28px', fontWeight: 700, color: '#0F172A', marginTop: '2px' }}>
            {pendingParcelsCount}
          </div>
          <div style={{ fontSize: '11px', color: '#3B82F6', marginTop: '2px' }}>View parcel locker →</div>
        </div>
      </div>

      {/* Main Scan / Verification Console */}
      <div style={{ backgroundColor: '#FFFFFF', border: '1px solid #CBD5E1', borderRadius: '14px', padding: '28px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', marginBottom: '24px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: 700, color: '#0F172A', margin: '0 0 8px 0' }}>
          {t('gate.quickVerify')}
        </h2>
        <p style={{ fontSize: '14px', color: '#64748B', margin: '0 0 20px 0' }}>
          {t('gate.enterPassOrRef')}
        </p>

        <form onSubmit={handleVerify} style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <input
            type="text"
            required
            autoFocus
            placeholder="Scan QR token, enter Pass Token or Reference (e.g. VIS-20261007-001)"
            value={searchCode}
            onChange={(e) => setSearchCode(e.target.value)}
            style={{
              flex: 1,
              minWidth: '280px',
              padding: '14px 18px',
              fontSize: '16px',
              fontWeight: 500,
              borderRadius: '8px',
              border: '2px solid #3B82F6',
              outline: 'none'
            }}
          />

          <button
            type="submit"
            disabled={verifying}
            style={{
              padding: '14px 28px',
              fontSize: '16px',
              fontWeight: 700,
              color: '#FFFFFF',
              backgroundColor: '#1E40AF',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              boxShadow: '0 2px 4px rgba(30,64,175,0.2)'
            }}
          >
            {verifying ? t('common.loading') : t('gate.verifyBtn')}
          </button>
        </form>

        {/* Feedback messages */}
        {actionMessage && (
          <div style={{ marginTop: '18px', padding: '14px 18px', backgroundColor: '#DEF7EC', border: '1px solid #31C48D', borderRadius: '8px', color: '#03543F', fontSize: '15px', fontWeight: 600 }}>
            ✓ {actionMessage}
          </div>
        )}

        {errorMessage && (
          <div style={{ marginTop: '18px', padding: '14px 18px', backgroundColor: '#FEF2F2', border: '1px solid #F87171', borderRadius: '8px', color: '#991B1B', fontSize: '15px' }}>
            ✕ {errorMessage}
          </div>
        )}

        {/* Verification Result Card */}
        {verificationResult && verificationResult.visit && (
          <div style={{ marginTop: '24px', padding: '24px', backgroundColor: '#F8FAFC', border: '2px solid #CBD5E1', borderRadius: '12px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <span style={{ fontSize: '13px', fontWeight: 700, color: '#1E40AF' }}>
                  REF: {verificationResult.visit.visitReference}
                </span>
                <h3 style={{ fontSize: '22px', fontWeight: 700, color: '#0F172A', margin: '4px 0 0 0' }}>
                  {verificationResult.visit.visitorName}
                </h3>
              </div>

              <span
                style={{
                  fontSize: '13px',
                  fontWeight: 700,
                  padding: '6px 14px',
                  borderRadius: '16px',
                  backgroundColor: verificationResult.valid ? '#DEF7EC' : '#FEF2F2',
                  color: verificationResult.valid ? '#03543F' : '#991B1B'
                }}
              >
                {verificationResult.valid ? 'VALID PASS' : 'INVALID / EXPIRED'}
              </span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px', fontSize: '14px', color: '#334155', marginBottom: '20px' }}>
              <div><strong>Host:</strong> {verificationResult.visit.hostNameSnapshot} ({verificationResult.visit.hostMobileSnapshot})</div>
              <div><strong>Destination Flat:</strong> {verificationResult.visit.destinationBlockName} - {verificationResult.visit.destinationFlatNumber}</div>
              <div><strong>Purpose:</strong> {verificationResult.visit.purpose} ({verificationResult.visit.visitorCategory})</div>
              <div><strong>Vehicle:</strong> {verificationResult.visit.vehicleNumber || 'Pedestrian / None'}</div>
              <div><strong>Arrival Window:</strong> {new Date(verificationResult.visit.arrivalWindowStart).toLocaleTimeString()} - {new Date(verificationResult.visit.arrivalWindowEnd).toLocaleTimeString()}</div>
              <div><strong>Current Status:</strong> <span style={{ fontWeight: 600 }}>{verificationResult.visit.status}</span></div>
            </div>

            {/* Action buttons */}
            <div style={{ display: 'flex', gap: '14px', borderTop: '1px solid #E2E8F0', paddingTop: '16px' }}>
              {verificationResult.canCheckIn && (
                <button
                  onClick={handleCheckIn}
                  style={{
                    flex: 1,
                    padding: '14px',
                    fontSize: '16px',
                    fontWeight: 700,
                    color: '#FFFFFF',
                    backgroundColor: '#059669',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    boxShadow: '0 2px 4px rgba(5,150,105,0.2)'
                  }}
                >
                  ✓ Admit & Check In Visitor
                </button>
              )}

              {verificationResult.canCheckOut && (
                <button
                  onClick={handleCheckOut}
                  style={{
                    flex: 1,
                    padding: '14px',
                    fontSize: '16px',
                    fontWeight: 700,
                    color: '#FFFFFF',
                    backgroundColor: '#DC2626',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    boxShadow: '0 2px 4px rgba(220,38,38,0.2)'
                  }}
                >
                  ✓ Record Visitor Exit & Release
                </button>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Gate Quick Action Buttons */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '14px' }}>
        <button
          onClick={() => navigate('/register-walkin')}
          style={{
            padding: '18px',
            backgroundColor: '#FFFFFF',
            border: '1px solid #E2E8F0',
            borderRadius: '12px',
            textAlign: 'left',
            cursor: 'pointer',
            boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
          }}
        >
          <div style={{ fontSize: '24px', marginBottom: '8px' }}>🚶</div>
          <div style={{ fontSize: '16px', fontWeight: 700, color: '#0F172A' }}>{t('gate.walkInBtn')}</div>
          <div style={{ fontSize: '12px', color: '#64748B', marginTop: '2px' }}>Call host & request fast approval</div>
        </button>

        <button
          onClick={() => navigate('/parcels')}
          style={{
            padding: '18px',
            backgroundColor: '#FFFFFF',
            border: '1px solid #E2E8F0',
            borderRadius: '12px',
            textAlign: 'left',
            cursor: 'pointer',
            boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
          }}
        >
          <div style={{ fontSize: '24px', marginBottom: '8px' }}>📦</div>
          <div style={{ fontSize: '16px', fontWeight: 700, color: '#0F172A' }}>{t('gate.receiveParcelBtn')}</div>
          <div style={{ fontSize: '12px', color: '#64748B', marginTop: '2px' }}>Store delivery & notify resident</div>
        </button>

        <button
          onClick={() => navigate('/domestic-staff')}
          style={{
            padding: '18px',
            backgroundColor: '#FFFFFF',
            border: '1px solid #E2E8F0',
            borderRadius: '12px',
            textAlign: 'left',
            cursor: 'pointer',
            boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
          }}
        >
          <div style={{ fontSize: '24px', marginBottom: '8px' }}>🧹</div>
          <div style={{ fontSize: '16px', fontWeight: 700, color: '#0F172A' }}>{t('gate.staffCheckInBtn')}</div>
          <div style={{ fontSize: '12px', color: '#64748B', marginTop: '2px' }}>Daily maid, cook, driver attendance</div>
        </button>

        <button
          onClick={() => navigate('/shift-handover')}
          style={{
            padding: '18px',
            backgroundColor: '#FFFFFF',
            border: '1px solid #E2E8F0',
            borderRadius: '12px',
            textAlign: 'left',
            cursor: 'pointer',
            boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
          }}
        >
          <div style={{ fontSize: '24px', marginBottom: '8px' }}>📋</div>
          <div style={{ fontSize: '16px', fontWeight: 700, color: '#0F172A' }}>{t('nav.securityHandover')}</div>
          <div style={{ fontSize: '12px', color: '#64748B', marginTop: '2px' }}>Log shift notes & handover items</div>
        </button>
      </div>
    </div>
  );
};
