import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@/app/AuthContext';
import { Logo } from '@/components/Logo';
import { ApartmentScene } from '@/scenes/ApartmentScene';
import { Mail, Lock, LogIn, AlertCircle, Sparkles, ArrowLeft, ShieldAlert } from 'lucide-react';
import { useI18n } from '@/i18n/I18nContext';
import { apiRequest } from '@/lib/api';

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isDemoMode, setIsDemoMode] = useState(false);

  useEffect(() => {
    apiRequest<{ communityName?: string; demoMode?: boolean }>('community/public-settings')
      .then((res) => {
        if (res) {
          setIsDemoMode(Boolean(res.demoMode || res.communityName?.toLowerCase().includes('demo')));
        }
      })
      .catch(() => {});
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setError('Please provide both email and password.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      const user = await login(email, password);

      if (user.status === 'PENDING') {
        navigate('/auth/application-status');
      } else if (user.role === 'ADMINISTRATOR') {
        navigate('/admin/dashboard');
      } else if (user.role === 'SECURITY') {
        navigate('/security/dashboard');
      } else {
        navigate('/resident/dashboard');
      }
    } catch (err: any) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setSubmitting(false);
    }
  };

  const fillDemo = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setError(null);
  };

  return (
    <div className="min-h-screen flex flex-col lg:flex-row bg-white">
      {/* Left Column: Login Form */}
      <div className="w-full lg:w-1/2 flex flex-col justify-between p-6 sm:p-12 lg:p-16 max-w-xl mx-auto lg:max-w-none">
        {/* Header Branding & Back to Welcome link */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Logo size="lg" />
            {isDemoMode && (
              <span className="hidden sm:inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-900 border border-amber-300">
                <span className="w-1.5 h-1.5 rounded-full bg-amber-500 animate-pulse" />
                Demo Environment
              </span>
            )}
          </div>
          <Link
            to="/"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-text-secondary hover:text-primary transition-colors py-1.5 px-3 rounded-lg hover:bg-surface border border-transparent hover:border-border"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>{t('welcome.backToWelcome')}</span>
          </Link>
        </div>

        {/* Center Form */}
        <div className="py-8">
          <div className="mb-6">
            <h1 className="text-2xl sm:text-3xl font-bold text-text-main tracking-tight">
              Sign In to Community Desk
            </h1>
            <p className="text-xs sm:text-sm text-text-secondary mt-1.5 leading-relaxed">
              Enter your verified credentials to access passes, gate entries, and visitor permissions.
            </p>
          </div>

          {error && (
            <div className="mb-5 p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-text-main mb-1.5">
                Email Address
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@community.com"
                  className="w-full pl-10 pr-3.5 py-2.5 bg-surface border border-border rounded-xl text-xs focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="block text-xs font-semibold text-text-main">
                  Password
                </label>
                <Link
                  to="/auth/reset-password"
                  className="text-[11px] font-medium text-primary hover:underline"
                >
                  Forgot password?
                </Link>
              </div>
              <div className="relative">
                <Lock className="w-4 h-4 text-text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-3.5 py-2.5 bg-surface border border-border rounded-xl text-xs focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-2.5 px-4 bg-primary hover:bg-primary-hover text-white font-semibold rounded-xl text-xs shadow-sm flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
            >
              <LogIn className="w-4 h-4" />
              {submitting ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>

          {/* Quick Demo Credentials */}
          <div className="mt-8 pt-6 border-t border-border">
            <div className="text-[11px] font-bold text-text-secondary uppercase tracking-wider mb-2.5 flex items-center justify-between">
              <span className="flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5 text-primary" />
                {isDemoMode ? 'Presentation Demo Accounts' : 'Quick Demo Accounts'}
              </span>
              {isDemoMode && (
                <span className="text-[10px] text-amber-800 bg-amber-50 px-2 py-0.5 rounded font-mono font-bold">
                  Bluehaven Demo
                </span>
              )}
            </div>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => fillDemo(isDemoMode ? 'admin@gatenest.example' : 'admin@greenfield.com', isDemoMode ? 'DemoPass2026!' : 'admin1234')}
                className="px-2.5 py-2 text-[11px] font-medium bg-surface hover:bg-accent-blue hover:text-primary border border-border rounded-lg text-left transition-colors truncate"
              >
                <strong>Admin</strong>
                <span className="block text-[10px] text-text-muted truncate">
                  {isDemoMode ? 'admin@gatenest.example' : 'admin@greenfield.com'}
                </span>
              </button>
              <button
                type="button"
                onClick={() => fillDemo(isDemoMode ? 'guard.main@gatenest.example' : 'guard.north@greenfield.com', isDemoMode ? 'DemoPass2026!' : 'guard1234')}
                className="px-2.5 py-2 text-[11px] font-medium bg-surface hover:bg-accent-blue hover:text-primary border border-border rounded-lg text-left transition-colors truncate"
              >
                <strong>Security</strong>
                <span className="block text-[10px] text-text-muted truncate">
                  {isDemoMode ? 'guard.main@...' : 'guard.north@...'}
                </span>
              </button>
              <button
                type="button"
                onClick={() => fillDemo(isDemoMode ? 'resident@gatenest.example' : 'resident.101@greenfield.com', isDemoMode ? 'DemoPass2026!' : 'resident1234')}
                className="px-2.5 py-2 text-[11px] font-medium bg-surface hover:bg-accent-blue hover:text-primary border border-border rounded-lg text-left transition-colors truncate"
              >
                <strong>Resident</strong>
                <span className="block text-[10px] text-text-muted truncate">
                  {isDemoMode ? 'resident@...' : 'resident.101@...'}
                </span>
              </button>
            </div>
          </div>
        </div>

        {/* Footer Links */}
        <div className="flex items-center justify-between text-xs text-text-secondary border-t border-border pt-4">
          <span>New resident in community?</span>
          <Link to="/register" className="font-bold text-primary hover:underline">
            Register Apartment
          </Link>
        </div>
      </div>

      {/* Right Column: 3D Scene */}
      <div className="hidden lg:block lg:w-1/2 border-l border-border h-screen sticky top-0">
        <ApartmentScene autoRotate={true} />
      </div>
    </div>
  );
};
