import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { useI18n } from '@/i18n/I18nContext';
import {
  LayoutDashboard,
  ShieldCheck,
  UserPlus,
  Package,
  QrCode,
  Users,
  Bell,
  MessageSquareWarning,
} from 'lucide-react';

export const MobileBottomNav: React.FC = () => {
  const { user, isAdmin, isSecurity, isResident } = useAuth();
  const { t } = useI18n();

  if (!user) return null;

  return (
    <nav
      aria-label="Mobile Navigation"
      className="lg:hidden fixed bottom-0 left-0 right-0 h-16 bg-white border-t border-border z-40 px-2 flex items-center justify-around shadow-lg pb-safe"
    >
      {/* Home / Dashboard */}
      <NavLink
        to={isAdmin ? '/admin' : isSecurity ? '/security' : '/resident'}
        className={({ isActive }) =>
          `flex flex-col items-center justify-center flex-1 h-full text-[10px] font-medium transition-colors ${
            isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-main'
          }`
        }
      >
        <LayoutDashboard className="w-5 h-5 mb-0.5" />
        <span>{t('dashboard') || 'Home'}</span>
      </NavLink>

      {/* Security: Gate Actions | Resident: Invite Visitor | Admin: Visits */}
      {isSecurity && (
        <NavLink
          to="/security"
          className={({ isActive }) =>
            `flex flex-col items-center justify-center flex-1 h-full text-[10px] font-medium transition-colors ${
              isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-main'
            }`
          }
        >
          <QrCode className="w-5 h-5 mb-0.5" />
          <span>{t('verifyPass') || 'Gate Desk'}</span>
        </NavLink>
      )}

      {isResident && (
        <NavLink
          to="/resident/invite"
          className={({ isActive }) =>
            `flex flex-col items-center justify-center flex-1 h-full text-[10px] font-medium transition-colors ${
              isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-main'
            }`
          }
        >
          <UserPlus className="w-5 h-5 mb-0.5" />
          <span>{t('preInviteVisitor') || 'Invite'}</span>
        </NavLink>
      )}

      {isAdmin && (
        <NavLink
          to="/admin/residents"
          className={({ isActive }) =>
            `flex flex-col items-center justify-center flex-1 h-full text-[10px] font-medium transition-colors ${
              isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-main'
            }`
          }
        >
          <Users className="w-5 h-5 mb-0.5" />
          <span>{t('residents') || 'Residents'}</span>
        </NavLink>
      )}

      {/* Parcels (Resident/Security) or Approvals (Resident) or Security Desk */}
      <NavLink
        to={isSecurity ? '/security/parcels' : isResident ? '/resident/parcels' : '/admin/reports'}
        className={({ isActive }) =>
          `flex flex-col items-center justify-center flex-1 h-full text-[10px] font-medium transition-colors ${
            isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-main'
          }`
        }
      >
        <Package className="w-5 h-5 mb-0.5" />
        <span>{t('parcels') || 'Parcels'}</span>
      </NavLink>

      {/* Complaints / Tickets or Staff */}
      <NavLink
        to={isResident ? '/resident/complaints' : isSecurity ? '/security/handover' : '/admin/complaints'}
        className={({ isActive }) =>
          `flex flex-col items-center justify-center flex-1 h-full text-[10px] font-medium transition-colors ${
            isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-main'
          }`
        }
      >
        <MessageSquareWarning className="w-5 h-5 mb-0.5" />
        <span>{isSecurity ? 'Handover' : t('complaints') || 'Tickets'}</span>
      </NavLink>

      {/* Notifications */}
      <NavLink
        to="/notifications"
        className={({ isActive }) =>
          `flex flex-col items-center justify-center flex-1 h-full text-[10px] font-medium transition-colors ${
            isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-main'
          }`
        }
      >
        <Bell className="w-5 h-5 mb-0.5" />
        <span>{t('notifications') || 'Alerts'}</span>
      </NavLink>
    </nav>
  );
};
