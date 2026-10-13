import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { api } from '@/lib/api';
import { Logo } from '@/components/Logo';
import { ApartmentScene } from '@/scenes/ApartmentScene';
import { Lock, KeyRound, Eye, EyeOff, CheckCircle2, AlertCircle, ArrowLeft, Mail } from 'lucide-react';

export const ResetPasswordPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const navigate = useNavigate();

  // Mode: if token is present, we are setting new password; if not, we are requesting a reset link
  const [isResetRequestMode, setIsResetRequestMode] = useState(!token);
  const [requestEmail, setRequestEmail] = useState('');
  const [requestSent, setRequestSent] = useState(false);

  // Set new password state
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    setIsResetRequestMode(!token);
  }, [token]);

  const handleRequestSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!requestEmail) return;

    try {
      setSubmitting(true);
      setError(null);
      await api.post('/auth/forgot-password', { email: requestEmail });
      setRequestSent(true);
    } catch (err: any) {
      // Safe response even on error or missing account
      setRequestSent(true);
    } finally {
      setSubmitting(false);
    }
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) {
      setError('Missing reset token.');
      return;
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      await api.post('/auth/reset-password', { token, newPassword: password });
      setSuccess(true);
    } catch (err: any) {
      setError(err.message || 'Password reset failed. The link may have expired or already been used.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col lg:flex-row bg-white">
      <div className="w-full lg:w-1/2 flex flex-col justify-between p-6 sm:p-12 lg:p-16 max-w-xl mx-auto lg:max-w-none">
        <div className="flex items-center justify-between">
          <Logo size="lg" />
          <Link
            to="/signin"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-text-secondary hover:text-primary transition-colors py-1.5 px-3 rounded-lg hover:bg-surface border border-transparent hover:border-border"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Sign In</span>
          </Link>
        </div>

        <div className="py-8">
          <div className="mb-6">
            <h1 className="text-2xl sm:text-3xl font-bold text-text-main tracking-tight">
              {isResetRequestMode ? 'Recover Your Account' : 'Reset Your Password'}
            </h1>
            <p className="text-xs sm:text-sm text-text-secondary mt-1.5 leading-relaxed">
              {isResetRequestMode
                ? 'Enter your registered email address and we will generate a recovery link if the account exists.'
                : 'Enter and confirm your new password below. All existing active sessions will be terminated for security.'}
            </p>
          </div>

          {error && (
            <div className="mb-5 p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-start gap-2.5">
              <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {isResetRequestMode ? (
            requestSent ? (
              <div className="p-6 bg-teal-50 border border-teal-200 rounded-2xl text-center space-y-4">
                <div className="w-12 h-12 bg-teal-100 text-teal-700 rounded-full flex items-center justify-center mx-auto">
                  <Mail className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="text-sm font-bold text-teal-900">Recovery Instructions Processed</h3>
                  <p className="text-xs text-teal-700 mt-1">
                    If an active account is associated with <strong>{requestEmail}</strong>, instructions or an administrator-assisted token have been dispatched.
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => navigate('/signin')}
                  className="w-full py-2.5 px-4 bg-primary hover:bg-primary-hover text-white font-semibold rounded-xl text-xs shadow-sm transition-colors"
                >
                  Return to Sign In
                </button>
              </div>
            ) : (
              <form onSubmit={handleRequestSubmit} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-text-main mb-1.5">
                    Registered Email Address
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
                    <input
                      type="email"
                      required
                      value={requestEmail}
                      onChange={(e) => setRequestEmail(e.target.value)}
                      placeholder="name@community.com"
                      className="w-full pl-10 pr-3.5 py-2.5 bg-surface border border-border rounded-xl text-xs focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={submitting}
                  className="w-full py-2.5 px-4 bg-primary hover:bg-primary-hover text-white font-semibold rounded-xl text-xs shadow-sm flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
                >
                  <KeyRound className="w-4 h-4" />
                  {submitting ? 'Sending Request...' : 'Send Recovery Link'}
                </button>
              </form>
            )
          ) : success ? (
            <div className="p-6 bg-emerald-50 border border-emerald-200 rounded-2xl text-center space-y-4">
              <div className="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-emerald-900">Password Successfully Reset</h3>
                <p className="text-xs text-emerald-700 mt-1">
                  You may now sign in using your new credentials.
                </p>
              </div>
              <button
                type="button"
                onClick={() => navigate('/signin')}
                className="w-full py-2.5 px-4 bg-primary hover:bg-primary-hover text-white font-semibold rounded-xl text-xs shadow-sm transition-colors"
              >
                Sign In Now
              </button>
            </div>
          ) : (
            <form onSubmit={handlePasswordSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-text-main mb-1.5">
                  New Password
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="At least 8 characters"
                    className="w-full pl-10 pr-10 py-2.5 bg-surface border border-border rounded-xl text-xs focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-main"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </button>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-text-main mb-1.5">
                  Confirm Password
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-text-muted absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Repeat new password"
                    className="w-full pl-10 pr-3.5 py-2.5 bg-surface border border-border rounded-xl text-xs focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                  />
                </div>
              </div>

              <div className="p-3 bg-surface border border-border rounded-xl text-[11px] text-text-secondary space-y-1">
                <p className="font-semibold text-text-main">Password Rules:</p>
                <p className={password.length >= 8 ? 'text-emerald-600 font-medium' : ''}>• Minimum 8 characters</p>
                <p className={password && password === confirmPassword ? 'text-emerald-600 font-medium' : ''}>• Passwords match</p>
              </div>

              <button
                type="submit"
                disabled={submitting || !token}
                className="w-full py-2.5 px-4 bg-primary hover:bg-primary-hover text-white font-semibold rounded-xl text-xs shadow-sm flex items-center justify-center gap-2 transition-colors disabled:opacity-50"
              >
                <KeyRound className="w-4 h-4" />
                {submitting ? 'Updating Password...' : 'Reset & Save Password'}
              </button>
            </form>
          )}
        </div>

        <div className="flex items-center justify-between text-xs text-text-secondary border-t border-border pt-4">
          <span>Remember your password?</span>
          <Link to="/signin" className="font-bold text-primary hover:underline">
            Sign In Directly
          </Link>
        </div>
      </div>

      <div className="hidden lg:block lg:w-1/2 border-l border-border h-screen sticky top-0">
        <ApartmentScene autoRotate={true} />
      </div>
    </div>
  );
};
