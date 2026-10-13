import React, { useState, useEffect } from 'react';
import { useAuth } from '@/app/AuthContext';
import { api, apiRequest } from '@/lib/api';
import {
  User,
  Mail,
  Phone,
  Lock,
  KeyRound,
  Building2,
  Shield,
  CheckCircle2,
  AlertCircle,
  Laptop,
  Smartphone,
  Globe,
  LogOut,
  Bell,
  Sliders,
  Clock,
  ShieldAlert
} from 'lucide-react';
import { format } from 'date-fns';

interface UserSessionItem {
  id: number;
  userId: number;
  ipAddress: string;
  userAgent: string;
  deviceSummary: string;
  createdAt: string;
  lastActivityAt: string;
  active: boolean;
  current: boolean;
}

export const ProfilePage: React.FC = () => {
  const { user, refreshSession } = useAuth();

  // Password state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmNewPassword, setConfirmNewPassword] = useState('');
  const [savingPassword, setSavingPassword] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);

  // Sessions state
  const [sessions, setSessions] = useState<UserSessionItem[]>([]);
  const [loadingSessions, setLoadingSessions] = useState(true);
  const [sessionActionError, setSessionActionError] = useState<string | null>(null);
  const [sessionActionSuccess, setSessionActionSuccess] = useState<string | null>(null);

  // Notification preferences
  const [notifyVisitorArrival, setNotifyVisitorArrival] = useState(() => {
    return localStorage.getItem('pref_notify_visitor') !== 'false';
  });
  const [notifyParcels, setNotifyParcels] = useState(() => {
    return localStorage.getItem('pref_notify_parcels') !== 'false';
  });
  const [notifyAnnouncements, setNotifyAnnouncements] = useState(() => {
    return localStorage.getItem('pref_notify_announcements') !== 'false';
  });
  const [notifyComplaints, setNotifyComplaints] = useState(() => {
    return localStorage.getItem('pref_notify_complaints') !== 'false';
  });
  const [prefSaved, setPrefSaved] = useState(false);

  const fetchSessions = async () => {
    try {
      setLoadingSessions(true);
      const res = await api.get<UserSessionItem[]>('/auth/sessions');
      setSessions(res.data || []);
    } catch (err: any) {
      console.warn('Could not fetch active sessions:', err);
    } finally {
      setLoadingSessions(false);
    }
  };

  useEffect(() => {
    fetchSessions();
  }, []);

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmNewPassword) {
      setPasswordError('New password and confirmation do not match.');
      return;
    }

    try {
      setSavingPassword(true);
      setPasswordError(null);
      setPasswordSuccess(null);
      await apiRequest('auth/change-password', {
        method: 'POST',
        body: JSON.stringify({
          currentPassword,
          newPassword,
          confirmNewPassword,
        }),
      });
      setPasswordSuccess('Password updated successfully. All other sessions have been logged out.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmNewPassword('');
      fetchSessions();
    } catch (err: any) {
      setPasswordError(err.message || 'Failed to update password');
    } finally {
      setSavingPassword(false);
    }
  };

  const handleRevokeSession = async (sessionId: number) => {
    try {
      setSessionActionError(null);
      setSessionActionSuccess(null);
      await api.delete(`/auth/sessions/${sessionId}`);
      setSessionActionSuccess('Session revoked successfully.');
      fetchSessions();
    } catch (err: any) {
      setSessionActionError(err.message || 'Failed to revoke session.');
    }
  };

  const handleRevokeAllOtherSessions = async () => {
    try {
      setSessionActionError(null);
      setSessionActionSuccess(null);
      await api.delete('/auth/sessions/revoke-all');
      setSessionActionSuccess('All other device sessions have been terminated.');
      fetchSessions();
    } catch (err: any) {
      setSessionActionError(err.message || 'Failed to sign out other sessions.');
    }
  };

  const handleSavePreferences = () => {
    localStorage.setItem('pref_notify_visitor', String(notifyVisitorArrival));
    localStorage.setItem('pref_notify_parcels', String(notifyParcels));
    localStorage.setItem('pref_notify_announcements', String(notifyAnnouncements));
    localStorage.setItem('pref_notify_complaints', String(notifyComplaints));
    setPrefSaved(true);
    setTimeout(() => setPrefSaved(false), 3000);
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Page Heading */}
      <div className="pb-4 border-b border-border">
        <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
          <User className="w-5 h-5 text-primary" />
          Account Profile & Security Settings
        </h1>
        <p className="text-xs text-text-secondary mt-1">
          View your verified community identity, active sessions, and manage your authentication credentials.
        </p>
      </div>

      {/* Profile Overview Card */}
      <div className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-4 text-xs">
        <div className="flex items-center gap-3.5 pb-4 border-b border-border">
          <div className="w-12 h-12 rounded-full bg-primary text-white font-bold text-lg flex items-center justify-center shadow-sm">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div>
            <h2 className="text-base font-bold text-text-main">{user?.fullName}</h2>
            <div className="text-text-secondary flex items-center gap-1.5 mt-0.5">
              <span className="font-semibold text-primary">{user?.role}</span>
              <span>• Account Status: <strong className="text-emerald-700 font-semibold">{user?.status}</strong></span>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
          <div className="p-3 bg-surface rounded-xl border border-border-light space-y-1">
            <span className="text-[10px] text-text-muted uppercase font-bold">Registered Email</span>
            <div className="font-semibold text-text-main flex items-center gap-1.5 truncate">
              <Mail className="w-3.5 h-3.5 text-primary flex-shrink-0" /> {user?.email}
            </div>
          </div>

          <div className="p-3 bg-surface rounded-xl border border-border-light space-y-1">
            <span className="text-[10px] text-text-muted uppercase font-bold">Mobile Number</span>
            <div className="font-semibold text-text-main flex items-center gap-1.5">
              <Phone className="w-3.5 h-3.5 text-primary flex-shrink-0" /> {user?.mobile || 'Not specified'}
            </div>
          </div>

          {user?.flatNumber && (
            <div className="p-3 bg-surface rounded-xl border border-border-light space-y-1 sm:col-span-2">
              <span className="text-[10px] text-text-muted uppercase font-bold">Apartment Allocation</span>
              <div className="font-bold text-primary flex items-center gap-1.5">
                <Building2 className="w-3.5 h-3.5" />
                {user.blockName} — Flat {user.flatNumber} ({user.occupancyType})
              </div>
            </div>
          )}

          {user?.assignedGateName && (
            <div className="p-3 bg-surface rounded-xl border border-border-light space-y-1 sm:col-span-2">
              <span className="text-[10px] text-text-muted uppercase font-bold">Assigned Security Gate</span>
              <div className="font-bold text-primary flex items-center gap-1.5">
                <Shield className="w-3.5 h-3.5" />
                {user.assignedGateName} ({user.assignedGateCode})
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Active Device Sessions Card */}
      <div className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-4 text-xs">
        <div className="flex items-center justify-between pb-3 border-b border-border">
          <div className="flex items-center gap-2">
            <Laptop className="w-4 h-4 text-primary" />
            <h2 className="text-sm font-bold text-text-main">Active Sessions & Devices</h2>
          </div>
          {sessions.length > 1 && (
            <button
              type="button"
              onClick={handleRevokeAllOtherSessions}
              className="inline-flex items-center gap-1.5 text-[11px] font-semibold text-rose-600 hover:text-rose-700 bg-rose-50 hover:bg-rose-100 border border-rose-200 px-2.5 py-1 rounded-lg transition-colors"
            >
              <LogOut className="w-3 h-3" />
              <span>Sign Out All Other Devices</span>
            </button>
          )}
        </div>

        <p className="text-text-secondary text-[11px]">
          These devices are currently authenticated to access your GateNest account. Revoking a session will immediately invalidate its access token.
        </p>

        {sessionActionSuccess && (
          <div className="p-3 bg-status-approved-bg border border-status-approved-border rounded-xl text-xs text-status-approved-text flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
            <span>{sessionActionSuccess}</span>
          </div>
        )}

        {sessionActionError && (
          <div className="p-3 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{sessionActionError}</span>
          </div>
        )}

        {loadingSessions ? (
          <div className="py-6 text-center text-text-muted">Loading session data...</div>
        ) : sessions.length === 0 ? (
          <div className="p-4 bg-surface rounded-xl text-center text-text-muted">
            Current browser session active.
          </div>
        ) : (
          <div className="space-y-2.5">
            {sessions.map((sess) => (
              <div
                key={sess.id}
                className={`p-3.5 rounded-xl border flex items-center justify-between gap-3 ${
                  sess.current ? 'bg-blue-50/50 border-blue-200' : 'bg-surface border-border'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className={`w-9 h-9 rounded-lg flex items-center justify-center ${sess.current ? 'bg-primary text-white' : 'bg-slate-200 text-slate-700'}`}>
                    {sess.deviceSummary?.toLowerCase().includes('mobile') ? (
                      <Smartphone className="w-4 h-4" />
                    ) : (
                      <Laptop className="w-4 h-4" />
                    )}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-text-main">{sess.deviceSummary || 'Browser Client'}</span>
                      {sess.current && (
                        <span className="px-2 py-0.5 rounded-full bg-blue-100 text-primary text-[10px] font-bold">
                          This Device
                        </span>
                      )}
                    </div>
                    <div className="text-[11px] text-text-muted flex items-center gap-2 mt-0.5">
                      <span className="flex items-center gap-1">
                        <Globe className="w-3 h-3" /> {sess.ipAddress || '127.0.0.1'}
                      </span>
                      <span>•</span>
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3" /> Last active: {sess.lastActivityAt ? format(new Date(sess.lastActivityAt), 'MMM dd, HH:mm') : 'Just now'}
                      </span>
                    </div>
                  </div>
                </div>

                {!sess.current && (
                  <button
                    type="button"
                    onClick={() => handleRevokeSession(sess.id)}
                    className="text-[11px] font-semibold text-rose-600 hover:text-rose-700 px-2.5 py-1 rounded hover:bg-rose-50 transition-colors"
                  >
                    Revoke
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Notification Preferences Card */}
      <div className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-4 text-xs">
        <div className="flex items-center justify-between pb-3 border-b border-border">
          <div className="flex items-center gap-2">
            <Bell className="w-4 h-4 text-primary" />
            <h2 className="text-sm font-bold text-text-main">Notification Preferences</h2>
          </div>
          {prefSaved && (
            <span className="text-emerald-700 font-bold text-[11px] flex items-center gap-1">
              <CheckCircle2 className="w-3.5 h-3.5" /> Saved
            </span>
          )}
        </div>

        <p className="text-text-secondary text-[11px]">
          Configure real-time alerts for visitor events and property activities. Mandatory operational security alerts will always be delivered.
        </p>

        <div className="space-y-3 pt-1">
          <label className="flex items-center justify-between p-3 bg-surface rounded-xl border border-border-light cursor-pointer hover:bg-slate-50 transition-colors">
            <div>
              <p className="font-semibold text-text-main">Visitor Arrival & Check-In Alerts</p>
              <p className="text-[11px] text-text-muted">Receive instantaneous prompts when visitors or deliveries reach the security gate.</p>
            </div>
            <input
              type="checkbox"
              checked={notifyVisitorArrival}
              onChange={(e) => {
                setNotifyVisitorArrival(e.target.checked);
              }}
              className="w-4 h-4 text-primary rounded border-border focus:ring-primary"
            />
          </label>

          <label className="flex items-center justify-between p-3 bg-surface rounded-xl border border-border-light cursor-pointer hover:bg-slate-50 transition-colors">
            <div>
              <p className="font-semibold text-text-main">Parcel & Courier Drop Notifications</p>
              <p className="text-[11px] text-text-muted">Notices when courier packages are accepted at the security gate desk.</p>
            </div>
            <input
              type="checkbox"
              checked={notifyParcels}
              onChange={(e) => {
                setNotifyParcels(e.target.checked);
              }}
              className="w-4 h-4 text-primary rounded border-border focus:ring-primary"
            />
          </label>

          <label className="flex items-center justify-between p-3 bg-surface rounded-xl border border-border-light cursor-pointer hover:bg-slate-50 transition-colors">
            <div>
              <p className="font-semibold text-text-main">General Community Announcements</p>
              <p className="text-[11px] text-text-muted">Broadcast notices from association administrators and management committee.</p>
            </div>
            <input
              type="checkbox"
              checked={notifyAnnouncements}
              onChange={(e) => {
                setNotifyAnnouncements(e.target.checked);
              }}
              className="w-4 h-4 text-primary rounded border-border focus:ring-primary"
            />
          </label>

          <label className="flex items-center justify-between p-3 bg-surface rounded-xl border border-border-light cursor-pointer hover:bg-slate-50 transition-colors">
            <div>
              <p className="font-semibold text-text-main">Helpdesk & Maintenance Ticket Updates</p>
              <p className="text-[11px] text-text-muted">Status progressions and technician assignments on logged service requests.</p>
            </div>
            <input
              type="checkbox"
              checked={notifyComplaints}
              onChange={(e) => {
                setNotifyComplaints(e.target.checked);
              }}
              className="w-4 h-4 text-primary rounded border-border focus:ring-primary"
            />
          </label>
        </div>

        <div className="pt-2 flex justify-end">
          <button
            type="button"
            onClick={handleSavePreferences}
            className="px-4 py-2 bg-primary hover:bg-primary-hover text-white font-semibold rounded-lg shadow-sm transition-colors text-xs"
          >
            Save Preferences
          </button>
        </div>
      </div>

      {/* Change Password Card */}
      <div id="password" className="bg-white border border-border rounded-2xl p-6 shadow-card space-y-4 text-xs">
        <div className="flex items-center gap-2 pb-3 border-b border-border">
          <KeyRound className="w-4 h-4 text-primary" />
          <h2 className="text-sm font-bold text-text-main">Change Password</h2>
        </div>

        {passwordSuccess && (
          <div className="p-3 bg-status-approved-bg border border-status-approved-border rounded-xl text-xs text-status-approved-text flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
            <span>{passwordSuccess}</span>
          </div>
        )}

        {passwordError && (
          <div className="p-3 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{passwordError}</span>
          </div>
        )}

        <form onSubmit={handlePasswordSubmit} className="space-y-3.5">
          <div>
            <label className="block font-semibold text-text-main mb-1">Current Password *</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="password"
                required
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">New Password *</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  minLength={8}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Min 8 characters"
                  className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
                />
              </div>
            </div>

            <div>
              <label className="block font-semibold text-text-main mb-1">Confirm New Password *</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  minLength={8}
                  value={confirmNewPassword}
                  onChange={(e) => setConfirmNewPassword(e.target.value)}
                  placeholder="Confirm password"
                  className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
                />
              </div>
            </div>
          </div>

          <div className="pt-2 flex justify-end">
            <button
              type="submit"
              disabled={savingPassword}
              className="px-4 py-2 bg-primary hover:bg-primary-hover text-white font-semibold rounded-lg shadow-sm transition-colors text-xs disabled:opacity-50"
            >
              {savingPassword ? 'Updating Password...' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
