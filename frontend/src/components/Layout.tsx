import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { TopBar } from './TopBar';
import { OfflineBanner } from './OfflineBanner';
import { PwaUpdateToast } from './PwaUpdateToast';
import { PwaInstallGuideModal } from './PwaInstallGuideModal';
import { MobileBottomNav } from './MobileBottomNav';

export const Layout: React.FC = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Offline Status Alert Banner */}
      <OfflineBanner />

      <div className="flex-1 flex">
        {/* Sidebar Navigation (Desktop + Mobile Drawer) */}
        <Sidebar isOpen={mobileMenuOpen} onClose={() => setMobileMenuOpen(false)} />

        {/* Main Content Area */}
        <div className="flex-1 lg:pl-64 flex flex-col min-w-0">
          <TopBar onOpenMobileMenu={() => setMobileMenuOpen(true)} />
          <main className="flex-1 p-4 sm:p-6 lg:p-8 bg-surface/50 pb-20 lg:pb-8">
            <div className="max-w-7xl mx-auto">
              <Outlet />
            </div>
          </main>
        </div>
      </div>

      {/* Mobile Bottom Navigation */}
      <MobileBottomNav />

      {/* PWA Update Ready Toast */}
      <PwaUpdateToast />

      {/* PWA Step-by-Step Installation Modal */}
      <PwaInstallGuideModal />
    </div>
  );
};
