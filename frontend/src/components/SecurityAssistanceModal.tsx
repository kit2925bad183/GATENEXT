import React, { useState } from 'react';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { Modal } from './Modal';

interface Props {
  isOpen: boolean;
  onClose: () => void;
}

export const SecurityAssistanceModal: React.FC<Props> = ({ isOpen, onClose }) => {
  const { t } = useI18n();

  const [category, setCategory] = useState<string>('EMERGENCY_HELP');
  const [locationDetails, setLocationDetails] = useState<string>('My Flat');
  const [description, setDescription] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [success, setSuccess] = useState<boolean>(false);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/assistance', {
        category,
        locationDetails,
        description
      });
      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        onClose();
      }, 3000);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to trigger assistance');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal title={t('assistance.trigger')} onClose={onClose}>
      {success ? (
        <div style={{ textAlign: 'center', padding: '24px 10px' }}>
          <div style={{ fontSize: '48px', marginBottom: '12px' }}>🚨</div>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: '#0F172A', margin: '0 0 6px 0' }}>
            Assistance Alert Broadcasted!
          </h3>
          <p style={{ fontSize: '14px', color: '#475569', margin: 0 }}>
            On-duty security personnel at all gates have been notified immediately. A guard is reviewing your alert.
          </p>
        </div>
      ) : (
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #FECACA', padding: '12px', borderRadius: '8px', fontSize: '13px', color: '#991B1B' }}>
            ⚠️ <strong>Notice:</strong> This notifies internal apartment gate security personnel. For severe external emergencies, call emergency services directly.
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
              {t('assistance.category')} *
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
            >
              <option value="EMERGENCY_HELP">{t('assistance.emergency')}</option>
              <option value="NOISE_DISTURBANCE">{t('assistance.noise')}</option>
              <option value="SUSPICIOUS_ACTIVITY">{t('assistance.suspicious')}</option>
              <option value="PARKING_BLOCK">{t('assistance.parkingBlock')}</option>
              <option value="GENERAL">{t('assistance.general')}</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
              {t('assistance.location')} *
            </label>
            <input
              required
              type="text"
              placeholder="e.g. Flat A-101, Corridor, Basement 1 Parking"
              value={locationDetails}
              onChange={(e) => setLocationDetails(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
              Additional Details (Optional)
            </label>
            <textarea
              rows={3}
              placeholder="Provide any helpful context for the security guard..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px', fontFamily: 'inherit' }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
            <button
              type="button"
              onClick={onClose}
              style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
            >
              {t('common.cancel')}
            </button>
            <button
              type="submit"
              disabled={submitting}
              style={{
                padding: '8px 20px',
                borderRadius: '6px',
                border: 'none',
                backgroundColor: '#DC2626',
                color: '#FFFFFF',
                fontWeight: 700,
                cursor: 'pointer',
                boxShadow: '0 2px 4px rgba(220,38,38,0.25)'
              }}
            >
              {submitting ? t('common.loading') : '🚨 Send Assistance Alert'}
            </button>
          </div>
        </form>
      )}
    </Modal>
  );
};
