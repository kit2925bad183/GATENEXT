import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './AuthContext';
import { I18nProvider } from '@/i18n/I18nContext';
import { PwaProvider } from '@/lib/pwaContext';
import { Layout } from '@/components/Layout';
import { Role } from '@/types';

// Auth Pages
import { WelcomePage } from '@/pages/WelcomePage';
import { LoginPage } from '@/pages/LoginPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { ApplicationStatusPage } from '@/pages/ApplicationStatusPage';
import { ActivateAccountPage } from '@/pages/ActivateAccountPage';
import { ResetPasswordPage } from '@/pages/ResetPasswordPage';

// Admin Pages
import { AdminDashboardPage } from '@/pages/AdminDashboardPage';
import { UserManagementPage } from '@/pages/UserManagementPage';
import { ResidentApprovalQueuePage } from '@/pages/ResidentApprovalQueuePage';
import { ResidentDirectoryPage } from '@/pages/ResidentDirectoryPage';
import { SecurityManagementPage } from '@/pages/SecurityManagementPage';
import { BlocksFlatsPage } from '@/pages/BlocksFlatsPage';
import { GateConfigPage } from '@/pages/GateConfigPage';
import { AllVisitsPage } from '@/pages/AllVisitsPage';
import { AuditLogsPage } from '@/pages/AuditLogsPage';
import { CommunitySettingsPage } from '@/pages/CommunitySettingsPage';

// Security Pages
import { GateDashboardPage } from '@/pages/GateDashboardPage';
import { SecurityGateDeskPage } from '@/pages/SecurityGateDeskPage';
import { RegisterWalkInPage } from '@/pages/RegisterWalkInPage';
import { VerifyPassPage } from '@/pages/VerifyPassPage';
import { WaitingApprovalsPage } from '@/pages/WaitingApprovalsPage';
import { CurrentlyInsidePage } from '@/pages/CurrentlyInsidePage';
import { VisitorHistoryPage } from '@/pages/VisitorHistoryPage';
import { SecurityHandoverPage } from '@/pages/SecurityHandoverPage';

// Resident Pages
import { ResidentDashboardPage } from '@/pages/ResidentDashboardPage';
import { InviteVisitorPage } from '@/pages/InviteVisitorPage';
import { UpcomingInvitationsPage } from '@/pages/UpcomingInvitationsPage';
import { ApprovalInboxPage } from '@/pages/ApprovalInboxPage';
import { MyVisitorsInsidePage } from '@/pages/MyVisitorsInsidePage';
import { MyVisitorHistoryPage } from '@/pages/MyVisitorHistoryPage';
import { SavedContactsPage } from '@/pages/SavedContactsPage';
import { RecurringVisitsPage } from '@/pages/RecurringVisitsPage';
import { GroupInvitationsPage } from '@/pages/GroupInvitationsPage';

// Enterprise Multi-Role Modules
import { ParcelsPage } from '@/pages/ParcelsPage';
import { StaffAttendancePage } from '@/pages/StaffAttendancePage';
import { VehiclesParkingPage } from '@/pages/VehiclesParkingPage';
import { ComplaintsPage } from '@/pages/ComplaintsPage';
import { AnnouncementsPage } from '@/pages/AnnouncementsPage';
import { ReportsAnalyticsPage } from '@/pages/ReportsAnalyticsPage';

// Shared Pages
import { VisitDetailPage } from '@/pages/VisitDetailPage';
import { NotificationsPage } from '@/pages/NotificationsPage';
import { ProfilePage } from '@/pages/ProfilePage';
import { AccessDeniedPage } from '@/pages/AccessDeniedPage';
import { NotFoundPage } from '@/pages/NotFoundPage';

// Protected Route Wrapper
const ProtectedRoute: React.FC<{
  children: React.ReactNode;
  allowedRoles?: Role[];
}> = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center text-xs text-text-secondary">
        <div className="flex flex-col items-center gap-2">
          <div className="w-8 h-8 rounded-full border-2 border-primary border-t-transparent animate-spin" />
          <span>Verifying GateNest security session...</span>
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.status === 'PENDING') {
    return <Navigate to="/auth/application-status" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role) && user.role !== 'ADMINISTRATOR') {
    return <Navigate to="/access-denied" replace />;
  }

  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <I18nProvider>
      <PwaProvider>
        <AuthProvider>
          <BrowserRouter>
          <Routes>
            {/* Public Entry & Auth Routes */}
            <Route path="/" element={<WelcomePage />} />
            <Route path="/signin" element={<LoginPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/auth/activate" element={<ActivateAccountPage />} />
            <Route path="/auth/reset-password" element={<ResetPasswordPage />} />
            <Route path="/auth/forgot-password" element={<ResetPasswordPage />} />
            <Route path="/auth/application-status" element={<ApplicationStatusPage />} />
            <Route path="/access-denied" element={<AccessDeniedPage />} />

            {/* Authenticated Application Shell */}
            <Route
              element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }
            >
              {/* Admin Routes */}
              <Route
                path="/admin/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <AdminDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/users"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <UserManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/resident-approvals"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <ResidentApprovalQueuePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/residents"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <ResidentDirectoryPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/security"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <SecurityManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/blocks-flats"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <BlocksFlatsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/gates"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <GateConfigPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/visits"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <AllVisitsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/audit-logs"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <AuditLogsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/admin/settings"
                element={
                  <ProtectedRoute allowedRoles={['ADMINISTRATOR']}>
                    <CommunitySettingsPage />
                  </ProtectedRoute>
                }
              />

              {/* Security Routes */}
              <Route
                path="/security/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['SECURITY']}>
                    <GateDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/security/gate-desk"
                element={
                  <ProtectedRoute allowedRoles={['SECURITY']}>
                    <SecurityGateDeskPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/security/register-walk-in"
                element={
                  <ProtectedRoute allowedRoles={['SECURITY']}>
                    <RegisterWalkInPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/security/verify-pass"
                element={
                  <ProtectedRoute allowedRoles={['SECURITY']}>
                    <VerifyPassPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/security/waiting-approvals"
                element={
                  <ProtectedRoute allowedRoles={['SECURITY']}>
                    <WaitingApprovalsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/security/inside"
                element={
                  <ProtectedRoute allowedRoles={['SECURITY']}>
                    <CurrentlyInsidePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/security/history"
                element={
                  <ProtectedRoute allowedRoles={['SECURITY']}>
                    <VisitorHistoryPage />
                  </ProtectedRoute>
                }
              />

              {/* Resident Routes */}
              <Route
                path="/resident/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <ResidentDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/invite"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <InviteVisitorPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/upcoming"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <UpcomingInvitationsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/approval-inbox"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <ApprovalInboxPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/visitors-inside"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <MyVisitorsInsidePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/history"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <MyVisitorHistoryPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/saved-contacts"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <SavedContactsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/recurring-rules"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <RecurringVisitsPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/resident/group-invitations"
                element={
                  <ProtectedRoute allowedRoles={['RESIDENT']}>
                    <GroupInvitationsPage />
                  </ProtectedRoute>
                }
              />

              {/* Shared / Cross-Role Enterprise Modules */}
              <Route path="/gate-desk" element={<SecurityGateDeskPage />} />
              <Route path="/parcels" element={<ParcelsPage />} />
              <Route path="/domestic-staff" element={<StaffAttendancePage />} />
              <Route path="/parking" element={<VehiclesParkingPage />} />
              <Route path="/complaints" element={<ComplaintsPage />} />
              <Route path="/announcements" element={<AnnouncementsPage />} />
              <Route path="/handover" element={<SecurityHandoverPage />} />
              <Route path="/reports" element={<ReportsAnalyticsPage />} />
              <Route path="/saved-contacts" element={<SavedContactsPage />} />
              <Route path="/recurring-rules" element={<RecurringVisitsPage />} />
              <Route path="/group-invitations" element={<GroupInvitationsPage />} />

              {/* Shared Authenticated Routes */}
              <Route path="/visits/:id" element={<VisitDetailPage />} />
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>

            {/* 404 Catch-All */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </PwaProvider>
  </I18nProvider>
  );
};
