import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { RecurringVisitRule } from '../types';
import { Modal } from '../components/Modal';

export const RecurringVisitsPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [rules, setRules] = useState<RecurringVisitRule[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Modal & Form
  const [showModal, setShowModal] = useState<boolean>(false);
  const [visitorName, setVisitorName] = useState<string>('');
  const [visitorMobile, setVisitorMobile] = useState<string>('');
  const [purpose, setPurpose] = useState<string>('Milk Delivery');
  const [validFromDate, setValidFromDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [validToDate, setValidToDate] = useState<string>(new Date(Date.now() + 90 * 86400000).toISOString().split('T')[0]);
  const [allowedStartTime, setAllowedStartTime] = useState<string>('06:00');
  const [allowedEndTime, setAllowedEndTime] = useState<string>('09:00');
  const [expectedDurationMinutes, setExpectedDurationMinutes] = useState<number>(30);
  const [allowedWeekdays, setAllowedWeekdays] = useState<string[]>(['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']);

  const isSecurityOrAdmin = user?.role === 'ADMINISTRATOR' || user?.role === 'SECURITY';

  const fetchRules = async () => {
    setLoading(true);
    setError(null);
    try {
      const endpoint = isSecurityOrAdmin ? '/recurring-rules/active' : '/recurring-rules/my-rules';
      const res = await api.get<{ data: RecurringVisitRule[] }>(endpoint);
      setRules(res.data?.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load recurring rules');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, [user]);

  const handleWeekdayToggle = (day: string) => {
    setAllowedWeekdays(prev =>
      prev.includes(day) ? prev.filter(d => d !== day) : [...prev, day]
    );
  };

  const handleCreateRule = async (e: React.FormEvent) => {
    e.preventDefault();
    if (allowedWeekdays.length === 0) {
      alert('Please select at least one allowed weekday');
      return;
    }

    try {
      await api.post('/recurring-rules', {
        visitorName,
        visitorMobile,
        visitorCategory: 'SERVICE',
        purpose,
        validFromDate,
        validToDate,
        allowedStartTime,
        allowedEndTime,
        expectedDurationMinutes,
        allowedWeekdays: allowedWeekdays.join(',')
      });
      setShowModal(false);
      fetchRules();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create recurring rule');
    }
  };

  const handleToggleStatus = async (id: number, currentActive: boolean) => {
    try {
      await api.patch(`/recurring-rules/${id}/status?active=${!currentActive}`);
      fetchRules();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update status');
    }
  };

  const daysOfWeek = [
    { code: 'MON', label: 'Mon' },
    { code: 'TUE', label: 'Tue' },
    { code: 'WED', label: 'Wed' },
    { code: 'THU', label: 'Thu' },
    { code: 'FRI', label: 'Fri' },
    { code: 'SAT', label: 'Sat' },
    { code: 'SUN', label: 'Sun' }
  ];

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('nav.recurringVisits')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Pre-authorized daily/weekly service visits (Newspapers, Milk, Water, Tutors)
          </p>
        </div>

        {!isSecurityOrAdmin && (
          <button
            onClick={() => setShowModal(true)}
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
            + Create Recurring Rule
          </button>
        )}
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : rules.length === 0 ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '48px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '40px', marginBottom: '12px' }}>🔄</div>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#1E293B', margin: '0 0 6px 0' }}>No recurring visit rules</h3>
          <p style={{ fontSize: '14px', color: '#64748B', margin: 0 }}>
            Set up schedules for milkmen, newspaper delivery, or tutors so security can admit them automatically.
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
          {rules.map((rule) => (
            <div
              key={rule.id}
              style={{
                backgroundColor: '#FFFFFF',
                border: '1px solid #E2E8F0',
                borderRadius: '12px',
                padding: '20px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.02)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                  <span style={{ fontSize: '12px', fontWeight: 600, color: '#0F766E', backgroundColor: '#F0FDFA', padding: '2px 8px', borderRadius: '4px' }}>
                    {rule.purpose}
                  </span>
                  <span
                    style={{
                      fontSize: '11px',
                      fontWeight: 600,
                      padding: '2px 8px',
                      borderRadius: '10px',
                      backgroundColor: rule.active ? '#DEF7EC' : '#F1F5F9',
                      color: rule.active ? '#03543F' : '#64748B'
                    }}
                  >
                    {rule.active ? 'Active Schedule' : 'Deactivated'}
                  </span>
                </div>

                <h3 style={{ fontSize: '17px', fontWeight: 600, color: '#0F172A', margin: '0 0 4px 0' }}>
                  {rule.visitorName}
                </h3>
                <div style={{ fontSize: '13px', color: '#64748B', marginBottom: '12px' }}>
                  📞 {rule.visitorMobile}
                </div>

                <div style={{ fontSize: '13px', color: '#475569', lineHeight: '1.6' }}>
                  <div><strong>Permitted Hours:</strong> {rule.allowedStartTime} to {rule.allowedEndTime}</div>
                  <div><strong>Allowed Days:</strong> {rule.allowedWeekdays}</div>
                  <div><strong>Validity:</strong> {rule.validFromDate} to {rule.validToDate}</div>
                  {rule.flatNumber && <div><strong>Destination:</strong> {rule.blockName} - {rule.flatNumber}</div>}
                </div>
              </div>

              {!isSecurityOrAdmin && (
                <div style={{ marginTop: '16px', borderTop: '1px solid #F1F5F9', paddingTop: '12px' }}>
                  <button
                    onClick={() => handleToggleStatus(rule.id, rule.active)}
                    style={{
                      width: '100%',
                      padding: '6px 12px',
                      fontSize: '12px',
                      fontWeight: 600,
                      color: rule.active ? '#DC2626' : '#059669',
                      backgroundColor: rule.active ? '#FEF2F2' : '#DEF7EC',
                      border: 'none',
                      borderRadius: '6px',
                      cursor: 'pointer'
                    }}
                  >
                    {rule.active ? 'Revoke / Deactivate Rule' : 'Re-activate Rule'}
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Create Recurring Rule Modal */}
      {showModal && (
        <Modal title="Create Recurring Visit Authorization" onClose={() => setShowModal(false)}>
          <form onSubmit={handleCreateRule} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Service Provider / Visitor Name *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Milkman Senthil"
                value={visitorName}
                onChange={(e) => setVisitorName(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Mobile Number *
              </label>
              <input
                required
                type="text"
                placeholder="+91 98 4500 1122"
                value={visitorMobile}
                onChange={(e) => setVisitorMobile(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Purpose of Routine Visits *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Daily Fresh Milk Delivery"
                value={purpose}
                onChange={(e) => setPurpose(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Valid From Date
                </label>
                <input
                  required
                  type="date"
                  value={validFromDate}
                  onChange={(e) => setValidFromDate(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Valid Until Date
                </label>
                <input
                  required
                  type="date"
                  value={validToDate}
                  onChange={(e) => setValidToDate(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Allowed Entry From Time
                </label>
                <input
                  required
                  type="time"
                  value={allowedStartTime}
                  onChange={(e) => setAllowedStartTime(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Allowed Entry Until Time
                </label>
                <input
                  required
                  type="time"
                  value={allowedEndTime}
                  onChange={(e) => setAllowedEndTime(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '6px' }}>
                Allowed Weekdays
              </label>
              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                {daysOfWeek.map((day) => {
                  const selected = allowedWeekdays.includes(day.code);
                  return (
                    <button
                      key={day.code}
                      type="button"
                      onClick={() => handleWeekdayToggle(day.code)}
                      style={{
                        padding: '6px 12px',
                        fontSize: '13px',
                        fontWeight: 600,
                        borderRadius: '6px',
                        border: selected ? '1px solid #1E40AF' : '1px solid #CBD5E1',
                        backgroundColor: selected ? '#1E40AF' : '#FFFFFF',
                        color: selected ? '#FFFFFF' : '#475569',
                        cursor: 'pointer'
                      }}
                    >
                      {day.label}
                    </button>
                  );
                })}
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Save Recurring Schedule
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
