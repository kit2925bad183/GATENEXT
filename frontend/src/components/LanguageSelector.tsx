import React from 'react';
import { useI18n } from '../i18n/I18nContext';

export const LanguageSelector: React.FC<{ compact?: boolean }> = ({ compact = false }) => {
  const { language, setLanguage } = useI18n();

  return (
    <div style={{ display: 'inline-flex', alignItems: 'center', backgroundColor: '#F1F5F9', borderRadius: '8px', padding: '3px', border: '1px solid #E2E8F0' }}>
      <button
        type="button"
        onClick={() => setLanguage('en')}
        style={{
          padding: compact ? '3px 8px' : '5px 12px',
          fontSize: compact ? '12px' : '13px',
          fontWeight: language === 'en' ? 600 : 400,
          color: language === 'en' ? '#1E40AF' : '#64748B',
          backgroundColor: language === 'en' ? '#FFFFFF' : 'transparent',
          border: 'none',
          borderRadius: '6px',
          cursor: 'pointer',
          boxShadow: language === 'en' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none',
          transition: 'all 0.15s ease'
        }}
        title="English"
      >
        EN
      </button>
      <button
        type="button"
        onClick={() => setLanguage('ta')}
        style={{
          padding: compact ? '3px 8px' : '5px 12px',
          fontSize: compact ? '12px' : '13px',
          fontWeight: language === 'ta' ? 600 : 400,
          color: language === 'ta' ? '#1E40AF' : '#64748B',
          backgroundColor: language === 'ta' ? '#FFFFFF' : 'transparent',
          border: 'none',
          borderRadius: '6px',
          cursor: 'pointer',
          boxShadow: language === 'ta' ? '0 1px 3px rgba(0,0,0,0.08)' : 'none',
          transition: 'all 0.15s ease'
        }}
        title="தமிழ் (Tamil)"
      >
        தமிழ்
      </button>
    </div>
  );
};
