import React from 'react';
import { VisitStatus, VerificationStatus, UserStatus } from '@/types';
import {
  Clock,
  CheckCircle2,
  LogIn,
  LogOut,
  XCircle,
  Ban,
  AlertTriangle,
  ShieldCheck,
  UserCheck,
  UserX,
} from 'lucide-react';

interface StatusBadgeProps {
  status: VisitStatus | VerificationStatus | UserStatus | string;
  size?: 'sm' | 'md';
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, size = 'md' }) => {
  const normalized = status.toUpperCase();

  const sizeClasses = {
    sm: 'text-[11px] px-2 py-0.5 gap-1',
    md: 'text-xs px-2.5 py-1 gap-1.5',
  };

  const iconSizes = {
    sm: 'w-3 h-3',
    md: 'w-3.5 h-3.5',
  };

  switch (normalized) {
    case 'PENDING_APPROVAL':
    case 'PENDING':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-pending-bg text-status-pending-text border border-status-pending-border ${sizeClasses[size]}`}
        >
          <Clock className={iconSizes[size]} />
          Pending Approval
        </span>
      );

    case 'APPROVED':
    case 'ACTIVE':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-approved-bg text-status-approved-text border border-status-approved-border ${sizeClasses[size]}`}
        >
          <CheckCircle2 className={iconSizes[size]} />
          {normalized === 'ACTIVE' ? 'Active' : 'Approved'}
        </span>
      );

    case 'INSIDE':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-inside-bg text-status-inside-text border border-status-inside-border animate-pulse ${sizeClasses[size]}`}
        >
          <LogIn className={iconSizes[size]} />
          Inside Premises
        </span>
      );

    case 'EXITED':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-exited-bg text-status-exited-text border border-status-exited-border ${sizeClasses[size]}`}
        >
          <LogOut className={iconSizes[size]} />
          Exited
        </span>
      );

    case 'REJECTED':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-rejected-bg text-status-rejected-text border border-status-rejected-border ${sizeClasses[size]}`}
        >
          <XCircle className={iconSizes[size]} />
          Rejected
        </span>
      );

    case 'CANCELLED':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-cancelled-bg text-status-cancelled-text border border-status-cancelled-border ${sizeClasses[size]}`}
        >
          <Ban className={iconSizes[size]} />
          Cancelled
        </span>
      );

    case 'EXPIRED':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-expired-bg text-status-expired-text border border-status-expired-border ${sizeClasses[size]}`}
        >
          <AlertTriangle className={iconSizes[size]} />
          Expired
        </span>
      );

    case 'SUSPENDED':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-expired-bg text-status-expired-text border border-status-expired-border ${sizeClasses[size]}`}
        >
          <Ban className={iconSizes[size]} />
          Suspended
        </span>
      );

    case 'DEACTIVATED':
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-status-cancelled-bg text-status-cancelled-text border border-status-cancelled-border ${sizeClasses[size]}`}
        >
          <UserX className={iconSizes[size]} />
          Deactivated
        </span>
      );

    default:
      return (
        <span
          className={`inline-flex items-center font-medium rounded-full bg-surface text-text-secondary border border-border ${sizeClasses[size]}`}
        >
          {status}
        </span>
      );
  }
};
