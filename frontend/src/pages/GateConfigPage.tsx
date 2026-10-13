import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { Gate } from '@/types';
import { Modal } from '@/components/Modal';
import { DoorOpen, Plus, Shield, CheckCircle2, AlertCircle } from 'lucide-react';

export const GateConfigPage: React.FC = () => {
  const [gates, setGates] = useState<Gate[]>([]);
  const [loading, setLoading] = useState(true);

  // Add Gate Modal
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [gateName, setGateName] = useState('');
  const [gateCode, setGateCode] = useState('');
  const [gateType, setGateType] = useState<'GENERAL' | 'PEDESTRIAN' | 'VEHICLE' | 'SERVICE'>('GENERAL');
  const [description, setDescription] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadGates = async () => {
    try {
      setLoading(true);
      const data = await apiRequest<Gate[]>('community/gates');
      setGates(data || []);
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadGates();
  }, []);

  const handleCreateGate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError(null);
      await apiRequest('community/gates', {
        method: 'POST',
        body: JSON.stringify({
          name: gateName,
          code: gateCode,
          gateType,
          description,
          active: true,
        }),
      });
      setAddModalOpen(false);
      setGateName('');
      setGateCode('');
      setDescription('');
      loadGates();
    } catch (err: any) {
      setError(err.message || 'Failed to create gate');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <DoorOpen className="w-5 h-5 text-primary" />
            Perimeter Security Gates Configuration
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Configure access points, barrier checkpoints, and assign active gate security stations.
          </p>
        </div>

        <button
          onClick={() => setAddModalOpen(true)}
          className="inline-flex items-center gap-2 px-3.5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors self-start"
        >
          <Plus className="w-4 h-4" /> Add Gate Station
        </button>
      </div>

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Gates Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-36 bg-white border border-border rounded-xl animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {gates.map((gate) => (
            <div
              key={gate.id}
              className="bg-white border border-border rounded-xl p-5 shadow-card flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <div>
                    <h3 className="text-sm font-bold text-text-main">{gate.name}</h3>
                    <span className="font-mono text-[11px] font-bold text-primary bg-primary-light px-2 py-0.5 rounded inline-block mt-1">
                      {gate.code}
                    </span>
                  </div>
                  <span className="text-[10px] font-semibold text-text-secondary bg-surface px-2 py-0.5 rounded border border-border">
                    {gate.gateType}
                  </span>
                </div>

                <p className="text-xs text-text-secondary leading-relaxed mt-2 line-clamp-2">
                  {gate.description || 'General entry checkpoint.'}
                </p>
              </div>

              <div className="mt-4 pt-3 border-t border-border-light flex items-center justify-between text-xs text-text-secondary">
                <span className="flex items-center gap-1.5 font-medium">
                  <Shield className="w-3.5 h-3.5 text-primary" />
                  {gate.activeGuardsCount || 0} active guards
                </span>
                <span className="inline-flex items-center gap-1 text-[#137547] text-[11px] font-semibold">
                  <CheckCircle2 className="w-3.5 h-3.5" /> Online
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add Gate Modal */}
      <Modal isOpen={addModalOpen} onClose={() => setAddModalOpen(false)} title="Configure New Gate Point">
        <form onSubmit={handleCreateGate} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-semibold text-text-main mb-1">Gate Name *</label>
            <input
              type="text"
              required
              value={gateName}
              onChange={(e) => setGateName(e.target.value)}
              placeholder="E.g. West Resident Barrier Gate"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">Gate Code *</label>
              <input
                type="text"
                required
                value={gateCode}
                onChange={(e) => setGateCode(e.target.value)}
                placeholder="GATE-WEST-04"
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs uppercase"
              />
            </div>

            <div>
              <label className="block font-semibold text-text-main mb-1">Gate Type</label>
              <select
                value={gateType}
                onChange={(e) => setGateType(e.target.value as any)}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              >
                <option value="GENERAL">General Entry</option>
                <option value="VEHICLE">Vehicle Boom Barrier</option>
                <option value="PEDESTRIAN">Pedestrian Turnstile</option>
                <option value="SERVICE">Service & Deliveries</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Operational Description</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Automated RFID tag reader checkpoint"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-border">
            <button
              type="button"
              onClick={() => setAddModalOpen(false)}
              className="px-3 py-1.5 border border-border rounded-lg font-semibold hover:bg-surface"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-1.5 bg-primary text-white font-semibold rounded-lg hover:bg-primary-hover transition-colors disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Create Gate'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
