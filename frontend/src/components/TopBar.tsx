import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { useI18n } from '@/i18n/I18nContext';
import { apiRequest } from '@/lib/api';
import { Notification } from '@/types';
import { LanguageSelector } from './LanguageSelector';
import { SecurityAssistanceModal } from './SecurityAssistanceModal';
import {
  Menu,
  Bell,
  Check,
  Clock,
  User as UserIcon,
  LogOut,
  KeyRound,
  AlertTriangle,
  ShieldAlert,
} from 'lucide-react';
import { format } from 'date-fns';

interface TopBarProps {
  onOpenMobileMenu: () => void;
}

export const TopBar: React.FC<TopBarProps> = ({ onOpenMobileMenu }) => {
  const { user, logout, isResident } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();

  const [currentTime, setCurrentTime] = useState<string>('');
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [showNotifications, setShowNotifications] = useState<boolean>(false);
  const [showProfileMenu, setShowProfileMenu] = useState<boolean>(false);
  const [showAssistanceModal, setShowAssistanceModal] = useState<boolean>(false);
  const [isDemoMode, setIsDemoMode] = useState<boolean>(false);

  const notifRef = useRef<HTMLDivElement>(null);
  const profileRef = useRef<HTMLDivElement>(null);

  // Community Settings & Demo Badge Status
  useEffect(() => {
    apiRequest<{ communityName?: string; demoMode?: boolean }>('community/public-settings')
      .then((res) => {
        if (res) {
          const isDemo = Boolean(res.demoMode || res.communityName?.toLowerCase().includes('demo'));
          setIsDemoMode(isDemo);
        }
      })
      .catch(() => {});
  }, []);

  // Live Clock
  useEffect(() => {
    const updateTime = () => {
      setCurrentTime(format(new Date(), 'EEE, d MMM yyyy • hh:mm:ss a'));
    };
    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  // Poll Notifications every 15s (paused if document is hidden)
  useEffect(() => {
    if (!user) return;

    const fetchNotifications = async () => {
      if (document.hidden) return;
      try {
        const countRes = await apiRequest<{ unreadCount: number }>('notifications/unread-count');
        setUnreadCount(countRes.unreadCount || 0);

        const list = await apiRequest<Notification[]>('notifications?limit=8');
        setNotifications(list || []);
      } catch (err) {
        // Silently handle polling errors
      }
    };

    fetchNotifications();
    const interval = setInterval(fetchNotifications, 15000);
    return () => clearInterval(interval);
  }, [user]);

  // Click outside listener for dropdowns
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (notifRef.current && !notifRef.current.contains(e.target as Node)) {
        setShowNotifications(false);
      }
      if (profileRef.current && !profileRef.current.contains(e.target as Node)) {
        setShowProfileMenu(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleMarkAsRead = async (id: number, linkUrl?: string) => {
    try {
      await apiRequest(`notifications/${id}/read`, { method: 'POST' });
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
      if (linkUrl) {
        setShowNotifications(false);
        navigate(linkUrl);
      }
    } catch (err) {}
  };

  const handleMarkAllRead = async () => {
    try {
      await apiRequest('notifications/mark-all-read', { method: 'POST' });
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnreadCount(0);
    } catch (err) {}
  };

  return (
    <>
      <header className="h-16 bg-white border-b border-border sticky top-0 z-30 px-4 sm:px-6 flex items-center justify-between">
        {/* Left Area: Mobile Menu & Live Clock & Demo Badge */}
        <div className="flex items-center gap-3">
          <button
            onClick={onOpenMobileMenu}
            className="lg:hidden p-2 rounded-lg text-text-secondary hover:text-text-main hover:bg-surface"
            aria-label="Open sidebar navigation"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="hidden sm:flex items-center gap-2 text-xs text-text-secondary font-medium bg-surface px-3 py-1.5 rounded-lg border border-border">
            <Clock className="w-3.5 h-3.5 text-primary" />
            <span>{currentTime || 'Loading clock...'}</span>
          </div>

          {isDemoMode && (
            <div className="flex items-center gap-1.5 px-3 py-1 bg-amber-50 border border-amber-300 text-amber-900 rounded-full text-[11px] font-bold shadow-xs">
              <span className="w-2 h-2 rounded-full bg-amber-500 animate-pulse flex-shrink-0" />
              <span className="hidden md:inline">Demo Environment — Fictional Data</span>
              <span className="md:hidden">Demo Environment</span>
            </div>
          )}
        </div>

        {/* Right Area: Language Switcher, Assistance, Notifications & Profile Menu */}
        <div className="flex items-center gap-2.5 sm:gap-3.5">
          {/* Tamil/English Language Switcher */}
          <LanguageSelector />

          {/* Resident Emergency / Assistance Button */}
          {isResident && (
            <button
              onClick={() => setShowAssistanceModal(true)}
              className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 bg-red-50 text-red-700 border border-red-200 rounded-lg text-xs font-semibold hover:bg-red-100 transition-colors shadow-sm"
              title="Request immediate gate security assistance"
            >
              <ShieldAlert className="w-4 h-4 text-red-600" />
              <span>{t('securityAssistance') || 'Security Assistance'}</span>
            </button>
          )}

          {/* Notification Bell */}
          <div className="relative" ref={notifRef}>
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="relative p-2 rounded-lg text-text-secondary hover:text-text-main hover:bg-surface transition-colors"
              aria-label="Notifications"
            >
              <Bell className="w-5 h-5" />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 min-w-[18px] h-[18px] text-[10px] font-bold bg-primary text-white rounded-full flex items-center justify-center px-1">
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              )}
            </button>

            {/* Notifications Dropdown */}
            {showNotifications && (
              <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-xl shadow-dropdown border border-border py-2 z-50 animate-in fade-in zoom-in-95 duration-100">
                <div className="px-4 py-2 border-b border-border flex items-center justify-between">
                  <span className="text-xs font-bold text-text-main uppercase tracking-wider">
                    {t('notifications') || 'Notifications'}
                  </span>
                  {unreadCount > 0 && (
                    <button
                      onClick={handleMarkAllRead}
                      className="text-xs text-primary hover:underline font-medium flex items-center gap-1"
                    >
                      <Check className="w-3 h-3" /> Mark all read
                    </button>
                  )}
                </div>

                <div className="max-h-80 overflow-y-auto divide-y divide-border-light">
                  {notifications.length === 0 ? (
                    <div className="p-6 text-center text-xs text-text-secondary">
                      No new notifications
                    </div>
                  ) : (
                    notifications.map((n) => (
                      <div
                        key={n.id}
                        onClick={() => handleMarkAsRead(n.id, n.linkUrl)}
                        className={`p-3.5 cursor-pointer transition-colors hover:bg-surface ${
                          !n.read ? 'bg-primary-light/40 font-medium' : ''
                        }`}
                      >
                        <div className="flex items-start justify-between gap-2 mb-1">
                          <span className="text-xs font-semibold text-text-main">
                            {n.title}
                          </span>
                          {!n.read && (
                            <span className="w-2 h-2 rounded-full bg-primary flex-shrink-0 mt-1" />
                          )}
                        </div>
                        <p className="text-xs text-text-secondary line-clamp-2 leading-relaxed">
                          {n.message}
                        </p>
                        <span className="text-[10px] text-text-muted mt-1 block">
                          {format(new Date(n.createdAt), 'MMM d, h:mm a')}
                        </span>
                      </div>
                    ))
                  )}
                </div>

                <div className="border-t border-border px-3 py-2 text-center bg-surface/50">
                  <button
                    onClick={() => {
                      setShowNotifications(false);
                      navigate('/notifications');
                    }}
                    className="text-xs text-primary font-medium hover:underline"
                  >
                    View All Notifications →
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Profile Avatar & Dropdown */}
          <div className="relative" ref={profileRef}>
            <button
              onClick={() => setShowProfileMenu(!showProfileMenu)}
              className="flex items-center gap-2 p-1.5 rounded-lg hover:bg-surface transition-colors"
            >
              <div className="w-8 h-8 rounded-full bg-primary text-white font-semibold flex items-center justify-center text-xs shadow-sm">
                {user?.fullName?.charAt(0) || 'U'}
              </div>
              <div className="hidden md:block text-left">
                <div className="text-xs font-semibold text-text-main truncate max-w-[120px]">
                  {user?.fullName}
                </div>
                <div className="text-[10px] text-text-secondary capitalize">{user?.role?.toLowerCase()}</div>
              </div>
            </button>

            {/* Profile Dropdown Menu */}
            {showProfileMenu && (
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-dropdown border border-border py-1.5 z-50">
                <div className="px-4 py-2 border-b border-border">
                  <div className="text-xs font-semibold text-text-main truncate">{user?.fullName}</div>
                  <div className="text-[11px] text-text-secondary truncate">{user?.email}</div>
                </div>

                <button
                  onClick={() => {
                    setShowProfileMenu(false);
                    navigate('/profile');
                  }}
                  className="w-full px-4 py-2 text-left text-xs text-text-main hover:bg-surface flex items-center gap-2"
                >
                  <UserIcon className="w-3.5 h-3.5 text-text-secondary" />
                  {t('profile') || 'Profile Settings'}
                </button>

                <button
                  onClick={() => {
                    setShowProfileMenu(false);
                    navigate('/profile#password');
                  }}
                  className="w-full px-4 py-2 text-left text-xs text-text-main hover:bg-surface flex items-center gap-2"
                >
                  <KeyRound className="w-3.5 h-3.5 text-text-secondary" />
                  Change Password
                </button>

                <div className="border-t border-border my-1" />

                <button
                  onClick={logout}
                  className="w-full px-4 py-2 text-left text-xs text-status-rejected-text hover:bg-status-rejected-bg flex items-center gap-2 font-medium"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  {t('logout') || 'Sign Out'}
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Floating Emergency Assistance Modal */}
      <SecurityAssistanceModal
        isOpen={showAssistanceModal}
        onClose={() => setShowAssistanceModal(false)}
      />
    </>
  );
};
