import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiRequest } from '@/lib/api';
import { Notification } from '@/types';
import { Bell, Check, Clock, CheckCircle2 } from 'lucide-react';
import { format } from 'date-fns';

export const NotificationsPage: React.FC = () => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const data = await apiRequest<Notification[]>('notifications?limit=50');
      setNotifications(data || []);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkAsRead = async (id: number, linkUrl?: string) => {
    try {
      await apiRequest(`notifications/${id}/read`, { method: 'POST' });
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
      if (linkUrl) {
        navigate(linkUrl);
      }
    } catch (err) {}
  };

  const handleMarkAllRead = async () => {
    try {
      await apiRequest('notifications/mark-all-read', { method: 'POST' });
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (err) {}
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Page Heading */}
      <div className="flex items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <Bell className="w-5 h-5 text-primary" />
            In-App Notifications
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Real-time notifications for arrivals, approvals, physical gate check-in, and cancellations.
          </p>
        </div>

        <button
          onClick={handleMarkAllRead}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors self-start shadow-sm"
        >
          <Check className="w-3.5 h-3.5 text-primary" /> Mark All Read
        </button>
      </div>

      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-20 bg-white border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : notifications.length === 0 ? (
        <div className="bg-white border border-border rounded-xl p-12 text-center max-w-md mx-auto shadow-card">
          <CheckCircle2 className="w-8 h-8 text-text-muted mx-auto mb-2" />
          <h2 className="text-sm font-bold text-text-main mb-1">No Notifications</h2>
          <p className="text-xs text-text-secondary">You have no notification alerts recorded.</p>
        </div>
      ) : (
        <div className="bg-white border border-border rounded-xl shadow-card divide-y divide-border-light overflow-hidden">
          {notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => handleMarkAsRead(n.id, n.linkUrl)}
              className={`p-4 cursor-pointer transition-colors hover:bg-surface flex items-start justify-between gap-4 ${
                !n.read ? 'bg-primary-light/30' : ''
              }`}
            >
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-text-main">{n.title}</span>
                  {!n.read && (
                    <span className="w-2 h-2 rounded-full bg-primary flex-shrink-0" />
                  )}
                </div>
                <p className="text-xs text-text-secondary leading-relaxed">{n.message}</p>
                <div className="text-[10px] text-text-muted flex items-center gap-1 pt-1">
                  <Clock className="w-3 h-3" />
                  {format(new Date(n.createdAt), 'dd MMM yyyy • hh:mm:ss a')}
                </div>
              </div>

              {n.linkUrl && (
                <span className="text-xs font-semibold text-primary hover:underline flex-shrink-0">
                  View →
                </span>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
