import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { useI18n } from '@/i18n/I18nContext';
import { usePwa } from '@/lib/pwaContext';
import { Logo } from './Logo';
import { AboutModal } from './AboutModal';
import {
  LayoutDashboard,
  UserCheck,
  Users,
  Shield,
  Building2,
  DoorOpen,
  Calendar,
  FileSpreadsheet,
  Settings,
  UserPlus,
  QrCode,
  Clock,
  LogIn,
  History,
  Send,
  Inbox,
  Package,
  Car,
  LifeBuoy,
  Megaphone,
  BarChart3,
  Bookmark,
  Repeat,
  Users2,
  ClipboardList,
  Contact,
  HardHat,
  Download,
  Info,
} from 'lucide-react';

interface SidebarProps {
  isOpen?: boolean;
  onClose?: () => void;
}

interface NavSection {
  title?: string;
  items: {
    to: string;
    label: string;
    icon: React.ElementType;
    badge?: string | number;
  }[];
}

export const Sidebar: React.FC<SidebarProps> = ({ isOpen = false, onClose }) => {
  const { user, isAdmin, isSecurity } = useAuth();
  const { t } = useI18n();
  const { isInstalled, promptInstall } = usePwa();
  const [showAboutModal, setShowAboutModal] = useState<boolean>(false);

  const adminSections: NavSection[] = [
    {
      title: t('dashboard') || 'Overview',
      items: [
        { to: '/admin/dashboard', label: t('dashboard'), icon: LayoutDashboard },
        { to: '/reports', label: t('reportsAnalytics'), icon: BarChart3 },
        { to: '/admin/visits', label: t('allVisits'), icon: Calendar },
      ],
    },
    {
      title: t('management') || 'Management',
      items: [
        { to: '/admin/users', label: 'User Accounts & Access', icon: Users },
        { to: '/admin/resident-approvals', label: t('residentApprovals'), icon: UserCheck },
        { to: '/admin/residents', label: t('residentDirectory'), icon: Building2 },
        { to: '/admin/security', label: t('securityManagement'), icon: Shield },
        { to: '/domestic-staff', label: t('domesticStaff'), icon: HardHat },
      ],
    },
    {
      title: t('operations') || 'Operations',
      items: [
        { to: '/parcels', label: t('parcels'), icon: Package },
        { to: '/parking', label: t('vehiclesParking'), icon: Car },
        { to: '/complaints', label: t('complaints'), icon: LifeBuoy },
        { to: '/announcements', label: t('announcements'), icon: Megaphone },
        { to: '/handover', label: t('shiftHandover'), icon: ClipboardList },
      ],
    },
    {
      title: t('settings') || 'System Setup',
      items: [
        { to: '/admin/blocks-flats', label: t('blocksFlats'), icon: Building2 },
        { to: '/admin/gates', label: t('gates'), icon: DoorOpen },
        { to: '/admin/audit-logs', label: t('auditLogs'), icon: FileSpreadsheet },
        { to: '/admin/settings', label: t('communitySettings'), icon: Settings },
      ],
    },
  ];

  const securitySections: NavSection[] = [
    {
      title: t('gateOperations') || 'Gate Controls',
      items: [
        { to: '/security/dashboard', label: t('gateOverview') || 'Gate Dashboard', icon: LayoutDashboard },
        { to: '/gate-desk', label: t('gateDeskConsole') || 'Gate Desk', icon: DoorOpen },
        { to: '/security/verify-pass', label: t('verifyPass'), icon: QrCode },
        { to: '/security/register-walk-in', label: t('registerWalkIn'), icon: UserPlus },
      ],
    },
    {
      title: t('traffic') || 'Live Traffic',
      items: [
        { to: '/security/waiting-approvals', label: t('waitingApprovals'), icon: Clock },
        { to: '/security/inside', label: t('currentlyInside'), icon: LogIn },
        { to: '/security/history', label: t('visitorHistory'), icon: History },
      ],
    },
    {
      title: t('services') || 'Desk Services',
      items: [
        { to: '/parcels', label: t('parcels'), icon: Package },
        { to: '/domestic-staff', label: t('domesticStaff'), icon: HardHat },
        { to: '/parking', label: t('vehiclesParking'), icon: Car },
        { to: '/announcements', label: t('announcements'), icon: Megaphone },
        { to: '/handover', label: t('shiftHandover'), icon: ClipboardList },
      ],
    },
  ];

  const residentSections: NavSection[] = [
    {
      title: t('visitorPasses') || 'Visitor Passes',
      items: [
        { to: '/resident/dashboard', label: t('dashboard'), icon: LayoutDashboard },
        { to: '/resident/invite', label: t('inviteVisitor'), icon: Send },
        { to: '/saved-contacts', label: t('savedContacts'), icon: Bookmark },
        { to: '/recurring-rules', label: t('recurringVisits'), icon: Repeat },
        { to: '/group-invitations', label: t('groupInvitations'), icon: Users2 },
      ],
    },
    {
      title: t('activity') || 'Activity',
      items: [
        { to: '/resident/upcoming', label: t('upcomingPasses'), icon: Calendar },
        { to: '/resident/approval-inbox', label: t('approvalInbox'), icon: Inbox },
        { to: '/resident/visitors-inside', label: t('visitorsInside'), icon: LogIn },
        { to: '/resident/history', label: t('myVisitHistory'), icon: History },
      ],
    },
    {
      title: t('communityServices') || 'Community Services',
      items: [
        { to: '/parcels', label: t('parcels'), icon: Package },
        { to: '/domestic-staff', label: t('domesticStaff'), icon: Contact },
        { to: '/parking', label: t('vehiclesParking'), icon: Car },
        { to: '/complaints', label: t('complaints'), icon: LifeBuoy },
        { to: '/announcements', label: t('announcements'), icon: Megaphone },
      ],
    },
  ];

  const sections = isAdmin ? adminSections : isSecurity ? securitySections : residentSections;

  return (
    <>
      {/* Mobile Backdrop */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-text-main/20 backdrop-blur-sm z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-50 w-64 bg-white border-r border-border flex flex-col transition-transform duration-200 ease-in-out lg:translate-x-0 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Brand Header */}
        <div className="h-16 px-5 border-b border-border flex items-center justify-between">
          <Logo size="md" />
        </div>

        {/* User Context Pill */}
        <div className="px-4 py-3 bg-surface border-b border-border">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-primary-light text-primary font-bold flex items-center justify-center text-xs flex-shrink-0">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="overflow-hidden">
              <div className="text-xs font-semibold text-text-main truncate">{user?.fullName}</div>
              <div className="text-[10px] text-text-secondary flex items-center gap-1">
                <span className="w-1.5 h-1.5 rounded-full bg-primary" />
                <span className="font-medium tracking-wide capitalize">{user?.role?.toLowerCase()}</span>
                {user?.flatNumber && <span>• {user.blockName} {user.flatNumber}</span>}
              </div>
            </div>
          </div>
        </div>

        {/* Navigation Links Grouped */}
        <nav className="flex-1 overflow-y-auto px-3 py-3 space-y-4">
          {sections.map((section, sIdx) => (
            <div key={sIdx} className="space-y-1">
              {section.title && (
                <div className="px-3 text-[10px] font-bold text-text-muted uppercase tracking-wider mb-1">
                  {section.title}
                </div>
              )}
              {section.items.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    onClick={onClose}
                    className={({ isActive }) =>
                      `flex items-center justify-between px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                        isActive
                          ? 'bg-primary-light text-primary font-semibold'
                          : 'text-text-secondary hover:text-text-main hover:bg-surface'
                      }`
                    }
                  >
                    <div className="flex items-center gap-2.5 truncate">
                      <Icon className="w-4 h-4 flex-shrink-0" />
                      <span className="truncate">{item.label}</span>
                    </div>
                    {item.badge !== undefined && (
                      <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-primary-light text-primary">
                        {item.badge}
                      </span>
                    )}
                  </NavLink>
                );
              })}
            </div>
          ))}
        </nav>

        {/* PWA Install & Footer Meta */}
        <div className="p-3 border-t border-border space-y-2 bg-surface/50">
          <button
            onClick={promptInstall}
            className="w-full py-1.5 px-3 bg-primary/10 hover:bg-primary/20 text-primary rounded-lg text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors border border-primary/20"
          >
            <Download className="w-3.5 h-3.5" />
            <span>{isInstalled ? 'App Installed' : 'Install GateNest App'}</span>
          </button>

          <div className="flex items-center justify-between text-[11px] text-text-secondary pt-1">
            <button
              onClick={() => setShowAboutModal(true)}
              className="hover:text-primary transition-colors flex items-center gap-1 text-[10px] font-mono text-text-muted hover:underline"
            >
              <Info className="w-3 h-3" />
              v1.2.0-pwa
            </button>
            <span className="inline-flex items-center gap-1 text-[#137547] font-medium text-[10px]">
              <span className="w-1.5 h-1.5 rounded-full bg-[#137547] animate-pulse" />
              Secure
            </span>
          </div>
        </div>
      </aside>

      <AboutModal isOpen={showAboutModal} onClose={() => setShowAboutModal(false)} />
    </>
  );
};
