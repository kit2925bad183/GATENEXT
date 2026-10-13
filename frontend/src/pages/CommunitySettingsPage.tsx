import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { CommunitySettings } from '@/types';
import { Settings, Save, CheckCircle2, AlertCircle } from 'lucide-react';

export const CommunitySettingsPage: React.FC = () => {
  const [settings, setSettings] = useState<CommunitySettings | null>(null);
  const [communityName, setCommunityName] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [postalCode, setPostalCode] = useState('');
  const [timezone, setTimezone] = useState('Asia/Kolkata');
  const [contactPhone, setContactPhone] = useState('');
  const [contactEmail, setContactEmail] = useState('');
  const [validityHours, setValidityHours] = useState(12);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadSettings = async () => {
      try {
        setLoading(true);
        const data = await apiRequest<CommunitySettings>('community/public-settings');
        if (data) {
          setSettings(data);
          setCommunityName(data.communityName);
          setAddress(data.address);
          setCity(data.city);
          setPostalCode(data.postalCode);
          setTimezone(data.defaultTimezone);
          setContactPhone(data.contactPhone || '');
          setContactEmail(data.contactEmail || '');
          setValidityHours(data.visitorPassValidityHours);
        }
      } catch (err: any) {
        setError(err.message || 'Failed to load settings');
      } finally {
        setLoading(false);
      }
    };
    loadSettings();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError(null);
      setMessage(null);
      const updated = await apiRequest<CommunitySettings>('community/settings', {
        method: 'PUT',
        body: JSON.stringify({
          communityName,
          address,
          city,
          postalCode,
          defaultTimezone: timezone,
          contactPhone,
          contactEmail,
          visitorPassValidityHours: Number(validityHours),
        }),
      });
      setSettings(updated);
      setMessage('Community configuration saved successfully.');
    } catch (err: any) {
      setError(err.message || 'Failed to save settings');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6 max-w-3xl">
      {/* Page Header */}
      <div className="pb-4 border-b border-border">
        <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
          <Settings className="w-5 h-5 text-primary" />
          Community & Perimeter Settings
        </h1>
        <p className="text-xs text-text-secondary mt-1">
          Configure estate identification, operational timezone, and pass validity duration.
        </p>
      </div>

      {message && (
        <div className="p-3.5 bg-status-approved-bg border border-status-approved-border rounded-xl text-xs text-status-approved-text flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
          <span>{message}</span>
        </div>
      )}

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="h-64 bg-white border border-border rounded-xl animate-pulse" />
      ) : (
        <form onSubmit={handleSubmit} className="bg-white border border-border rounded-xl p-6 shadow-card space-y-4 text-xs">
          <div>
            <label className="block font-semibold text-text-main mb-1.5">Community / Society Name *</label>
            <input
              type="text"
              required
              value={communityName}
              onChange={(e) => setCommunityName(e.target.value)}
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1.5">Address *</label>
            <input
              type="text"
              required
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1.5">City *</label>
              <input
                type="text"
                required
                value={city}
                onChange={(e) => setCity(e.target.value)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
            <div>
              <label className="block font-semibold text-text-main mb-1.5">Postal Code *</label>
              <input
                type="text"
                required
                value={postalCode}
                onChange={(e) => setPostalCode(e.target.value)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1.5">Timezone</label>
              <select
                value={timezone}
                onChange={(e) => setTimezone(e.target.value)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              >
                <option value="Asia/Kolkata">Asia/Kolkata (IST - UTC+5:30)</option>
                <option value="UTC">UTC</option>
                <option value="America/New_York">America/New_York (EST)</option>
                <option value="Europe/London">Europe/London (GMT/BST)</option>
                <option value="Asia/Dubai">Asia/Dubai (GST)</option>
                <option value="Asia/Singapore">Asia/Singapore (SGT)</option>
              </select>
            </div>
            <div>
              <label className="block font-semibold text-text-main mb-1.5">Pass Default Validity (Hours)</label>
              <input
                type="number"
                min={1}
                max={72}
                required
                value={validityHours}
                onChange={(e) => setValidityHours(Number(e.target.value))}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1.5">Management Contact Phone</label>
              <input
                type="text"
                value={contactPhone}
                onChange={(e) => setContactPhone(e.target.value)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
            <div>
              <label className="block font-semibold text-text-main mb-1.5">Management Email</label>
              <input
                type="email"
                value={contactEmail}
                onChange={(e) => setContactEmail(e.target.value)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
          </div>

          <div className="pt-4 border-t border-border flex justify-end">
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white font-semibold rounded-lg shadow-sm transition-colors text-xs flex items-center gap-1.5 disabled:opacity-50"
            >
              <Save className="w-4 h-4" /> {saving ? 'Saving...' : 'Save Configuration'}
            </button>
          </div>
        </form>
      )}
    </div>
  );
};
