import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Logo } from '@/components/Logo';
import { ApartmentScene } from '@/scenes/ApartmentScene';
import { apiRequest } from '@/lib/api';
import { Block, Flat, OccupancyType } from '@/types';
import { User, Mail, Phone, Lock, Building2, AlertCircle, CheckCircle2 } from 'lucide-react';

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();

  const [blocks, setBlocks] = useState<Block[]>([]);
  const [flats, setFlats] = useState<Flat[]>([]);
  const [selectedBlockId, setSelectedBlockId] = useState<string>('');

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [mobile, setMobile] = useState('');
  const [flatId, setFlatId] = useState('');
  const [occupancyType, setOccupancyType] = useState<OccupancyType>('OWNER');
  const [emergencyContact, setEmergencyContact] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    const loadStructure = async () => {
      try {
        const structure = await apiRequest<{ blocks: Block[]; flats: Flat[] }>('community/public-structure');
        setBlocks(structure.blocks || []);
        setFlats(structure.flats || []);
        if (structure.blocks && structure.blocks.length > 0) {
          setSelectedBlockId(String(structure.blocks[0].id));
        }
      } catch (err) {
        // Fallback loading
      }
    };
    loadStructure();
  }, []);

  const filteredFlats = flats.filter(
    (f) => String(f.blockId) === selectedBlockId
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (!flatId) {
      setError('Please select your flat number.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      await apiRequest('auth/register', {
        method: 'POST',
        body: JSON.stringify({
          fullName,
          email,
          mobile,
          flatId: Number(flatId),
          occupancyType,
          emergencyContact,
          password,
          confirmPassword,
        }),
      });
      setSuccess(true);
    } catch (err: any) {
      setError(err.message || 'Registration failed. Please check your information.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col lg:flex-row bg-white">
      {/* Left Column: Registration Form */}
      <div className="w-full lg:w-1/2 flex flex-col justify-between p-6 sm:p-10 lg:p-12 max-w-xl mx-auto lg:max-w-none overflow-y-auto">
        <div>
          <Logo size="lg" />
        </div>

        <div className="py-6">
          <div className="mb-6">
            <h1 className="text-2xl font-bold text-text-main tracking-tight">
              Resident Account Registration
            </h1>
            <p className="text-xs text-text-secondary mt-1">
              Register your household to generate visitor passes and receive real-time arrival notifications.
            </p>
          </div>

          {error && (
            <div className="mb-5 p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-start gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {success ? (
            <div className="p-6 bg-status-approved-bg border border-status-approved-border rounded-2xl text-center space-y-3">
              <div className="w-12 h-12 rounded-full bg-status-approved-border flex items-center justify-center text-status-approved-text mx-auto">
                <CheckCircle2 className="w-6 h-6" />
              </div>
              <h2 className="text-base font-bold text-text-main">Registration Submitted</h2>
              <p className="text-xs text-text-secondary leading-relaxed">
                Your application has been received and queued for administrator verification. Once reviewed, you will be able to log in and manage your passes.
              </p>
              <div className="pt-2">
                <Link
                  to="/login"
                  className="inline-block px-4 py-2 bg-primary text-white text-xs font-semibold rounded-lg hover:bg-primary-hover transition-colors"
                >
                  Return to Login
                </Link>
              </div>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-3.5 text-xs">
              <div>
                <label className="block font-semibold text-text-main mb-1">Full Name</label>
                <div className="relative">
                  <User className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder="E.g. Priya Sharma"
                    className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-text-main mb-1">Email</label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="priya@example.com"
                      className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                    />
                  </div>
                </div>

                <div>
                  <label className="block font-semibold text-text-main mb-1">Mobile Number</label>
                  <div className="relative">
                    <Phone className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="tel"
                      required
                      value={mobile}
                      onChange={(e) => setMobile(e.target.value)}
                      placeholder="+91 98765 43210"
                      className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                    />
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block font-semibold text-text-main mb-1">Block / Tower</label>
                  <select
                    value={selectedBlockId}
                    onChange={(e) => {
                      setSelectedBlockId(e.target.value);
                      setFlatId('');
                    }}
                    className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                  >
                    {blocks.map((b) => (
                      <option key={b.id} value={String(b.id)}>
                        {b.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-text-main mb-1">Flat Number</label>
                  <select
                    required
                    value={flatId}
                    onChange={(e) => setFlatId(e.target.value)}
                    className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                  >
                    <option value="">Select Flat...</option>
                    {filteredFlats.map((f) => (
                      <option key={f.id} value={String(f.id)}>
                        {f.flatNumber} (Floor {f.floorNumber})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-text-main mb-1">Occupancy</label>
                  <select
                    value={occupancyType}
                    onChange={(e) => setOccupancyType(e.target.value as OccupancyType)}
                    className="w-full px-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                  >
                    <option value="OWNER">Owner</option>
                    <option value="TENANT">Tenant</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-text-main mb-1">Password</label>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="password"
                      required
                      minLength={8}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Min 8 characters"
                      className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                    />
                  </div>
                </div>

                <div>
                  <label className="block font-semibold text-text-main mb-1">Confirm Password</label>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                      type="password"
                      required
                      minLength={8}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Confirm password"
                      className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary focus:bg-white"
                    />
                  </div>
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-2.5 bg-primary hover:bg-primary-hover text-white font-semibold rounded-lg shadow-sm transition-colors text-xs disabled:opacity-50 mt-2"
              >
                {loading ? 'Submitting Registration...' : 'Submit Registration Application'}
              </button>
            </form>
          )}
        </div>

        <div className="border-t border-border pt-4 text-xs text-text-secondary flex justify-between">
          <span>Already registered?</span>
          <Link to="/login" className="font-bold text-primary hover:underline">
            Sign In Here
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
