import React, { Suspense } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { useI18n } from '@/i18n/I18nContext';
import { Logo } from '@/components/Logo';
import { LanguageSelector } from '@/components/LanguageSelector';
import { ApartmentScene } from '@/scenes/ApartmentScene';
import { ApartmentSvgFallback } from '@/scenes/ApartmentSvgFallback';
import { ArrowRight, Mail, ShieldCheck, ArrowLeftRight, Sparkles } from 'lucide-react';

export const WelcomePage: React.FC = () => {
  const { user, loading } = useAuth();
  const { t } = useI18n();

  // If already authenticated, redirect to their role-specific dashboard
  if (!loading && user) {
    if (user.status === 'PENDING') {
      return <Navigate to="/auth/application-status" replace />;
    }
    if (user.role === 'ADMINISTRATOR') {
      return <Navigate to="/admin/dashboard" replace />;
    }
    if (user.role === 'SECURITY') {
      return <Navigate to="/security/dashboard" replace />;
    }
    return <Navigate to="/resident/dashboard" replace />;
  }

  return (
    <div className="min-h-screen bg-white text-text-main flex flex-col lg:flex-row relative overflow-x-hidden selection:bg-primary selection:text-white">
      {/* Subtle Pale-Blue and Teal Decorative Shapes */}
      <div
        className="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-[#EDF3FF] opacity-70 blur-3xl pointer-events-none"
        aria-hidden="true"
      />
      <div
        className="absolute top-1/2 -right-24 w-80 h-80 rounded-full bg-teal-50/60 opacity-80 blur-3xl pointer-events-none"
        aria-hidden="true"
      />
      <div
        className="absolute -bottom-20 left-1/3 w-72 h-72 rounded-full bg-blue-50/50 opacity-60 blur-2xl pointer-events-none"
        aria-hidden="true"
      />

      {/* Left Column: Hero Brand, Copy & Primary Action */}
      <div className="w-full lg:w-1/2 flex flex-col justify-between p-6 sm:p-10 lg:p-16 z-10 max-w-2xl mx-auto lg:max-w-none">
        {/* Top Bar: Brand Logo & Language Switcher */}
        <header className="flex items-center justify-between gap-4 pb-6 border-b border-border/40">
          <Logo size="md" />
          <div className="flex items-center gap-3">
            <LanguageSelector compact />
          </div>
        </header>

        {/* Main Content Area */}
        <main className="py-8 sm:py-12 my-auto">
          {/* Small Introductory Label */}
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#EDF3FF] border border-[#D5E3FF] text-[11px] font-bold text-[#1E40AF] tracking-wider uppercase mb-5 shadow-xs">
            <Sparkles className="w-3.5 h-3.5 text-[#087F8C]" />
            <span>{t('welcome.label')}</span>
          </div>

          {/* Main Heading */}
          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black text-[#0F172A] tracking-tight leading-[1.15] mb-4">
            {t('welcome.heading')}
          </h1>

          {/* Description */}
          <p className="text-sm sm:text-base lg:text-lg text-[#475569] leading-relaxed mb-8 max-w-xl">
            {t('welcome.description')}
          </p>

          {/* Primary Action Button */}
          <div className="flex flex-col sm:flex-row sm:items-center gap-3 mb-3">
            <Link
              to="/signin"
              id="continue-to-signin-btn"
              className="inline-flex items-center justify-center gap-2.5 px-6 py-3.5 bg-[#1E40AF] hover:bg-[#1D4ED8] active:bg-[#1E3A8A] text-white text-sm sm:text-base font-semibold rounded-xl shadow-sm hover:shadow transition-all focus:outline-none focus:ring-4 focus:ring-blue-100 group"
            >
              <span>{t('welcome.continueBtn')}</span>
              <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
            </Link>

            <Link
              to="/register"
              className="inline-flex items-center justify-center px-4 py-3.5 text-xs sm:text-sm font-semibold text-text-secondary hover:text-primary transition-colors text-center"
            >
              New Resident Registration →
            </Link>
          </div>

          {/* Small Informational Line */}
          <p className="text-xs text-[#64748B] font-medium mb-8">
            {t('welcome.infoLine')}
          </p>

          {/* Three Compact Feature Badges */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5 pt-4 border-t border-border/60">
            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-lg bg-[#F8FAFC] border border-[#E2E8F0]">
              <div className="w-7 h-7 rounded-md bg-[#EDF3FF] text-[#1E40AF] flex items-center justify-center flex-shrink-0">
                <Mail className="w-4 h-4" />
              </div>
              <span className="text-xs font-semibold text-[#1E293B]">
                {t('welcome.feature1')}
              </span>
            </div>

            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-lg bg-[#F8FAFC] border border-[#E2E8F0]">
              <div className="w-7 h-7 rounded-md bg-[#E6F4F1] text-[#087F8C] flex items-center justify-center flex-shrink-0">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <span className="text-xs font-semibold text-[#1E293B]">
                {t('welcome.feature2')}
              </span>
            </div>

            <div className="flex items-center gap-2.5 px-3 py-2.5 rounded-lg bg-[#F8FAFC] border border-[#E2E8F0]">
              <div className="w-7 h-7 rounded-md bg-[#EDF3FF] text-[#1E40AF] flex items-center justify-center flex-shrink-0">
                <ArrowLeftRight className="w-4 h-4" />
              </div>
              <span className="text-xs font-semibold text-[#1E293B]">
                {t('welcome.feature3')}
              </span>
            </div>
          </div>
        </main>

        {/* Footer info */}
        <footer className="pt-6 border-t border-border/40 flex flex-col sm:flex-row items-center justify-between text-[11px] text-[#94A3B8] gap-2">
          <span>© {new Date().getFullYear()} GateNest Security & Visitor Solutions</span>
          <span className="font-mono text-[10px] text-[#64748B]">v2.4.0 • Enterprise Architecture</span>
        </footer>
      </div>

      {/* Right Column: Three.js Apartment Visualization */}
      <div className="w-full lg:w-1/2 h-80 sm:h-96 lg:h-screen lg:sticky lg:top-0 border-t lg:border-t-0 lg:border-l border-border/70 bg-[#F8FAFC] relative overflow-hidden">
        <Suspense fallback={<ApartmentSvgFallback />}>
          <ApartmentScene className="w-full h-full" autoRotate={true} />
        </Suspense>
      </div>
    </div>
  );
};
