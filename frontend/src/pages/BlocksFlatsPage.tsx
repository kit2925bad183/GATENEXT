import React, { useState, useEffect } from 'react';
import { apiRequest } from '@/lib/api';
import { Block, Flat } from '@/types';
import { Modal } from '@/components/Modal';
import { Building2, Plus, Home, Users, Layers, AlertCircle } from 'lucide-react';

export const BlocksFlatsPage: React.FC = () => {
  const [blocks, setBlocks] = useState<Block[]>([]);
  const [flats, setFlats] = useState<Flat[]>([]);
  const [selectedBlockId, setSelectedBlockId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);

  // Modals
  const [addBlockOpen, setAddBlockOpen] = useState(false);
  const [blockName, setBlockName] = useState('');
  const [blockDesc, setBlockDesc] = useState('');
  const [blockFloors, setBlockFloors] = useState(10);

  const [addFlatOpen, setAddFlatOpen] = useState(false);
  const [flatNumber, setFlatNumber] = useState('');
  const [flatFloor, setFlatFloor] = useState(1);
  const [flatExtension, setFlatExtension] = useState('');

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const [bData, fData] = await Promise.all([
        apiRequest<Block[]>('community/blocks'),
        apiRequest<Flat[]>('community/flats'),
      ]);
      setBlocks(bData || []);
      setFlats(fData || []);
      if (bData && bData.length > 0 && selectedBlockId === null) {
        setSelectedBlockId(bData[0].id);
      }
    } catch (err) {
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleCreateBlock = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setError(null);
      await apiRequest('community/blocks', {
        method: 'POST',
        body: JSON.stringify({
          name: blockName,
          description: blockDesc,
          totalFloors: Number(blockFloors),
          active: true,
        }),
      });
      setAddBlockOpen(false);
      setBlockName('');
      setBlockDesc('');
      loadData();
    } catch (err: any) {
      setError(err.message || 'Failed to create block');
    } finally {
      setSaving(false);
    }
  };

  const handleCreateFlat = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBlockId) return;

    try {
      setSaving(true);
      setError(null);
      await apiRequest('community/flats', {
        method: 'POST',
        body: JSON.stringify({
          blockId: selectedBlockId,
          flatNumber,
          floorNumber: Number(flatFloor),
          extensionNumber: flatExtension,
          active: true,
        }),
      });
      setAddFlatOpen(false);
      setFlatNumber('');
      setFlatExtension('');
      loadData();
    } catch (err: any) {
      setError(err.message || 'Failed to create flat');
    } finally {
      setSaving(false);
    }
  };

  const activeFlats = flats.filter((f) => f.blockId === selectedBlockId);
  const selectedBlock = blocks.find((b) => b.id === selectedBlockId);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <h1 className="text-xl font-bold text-text-main flex items-center gap-2">
            <Building2 className="w-5 h-5 text-primary" />
            Apartment Blocks & Flat Allocations
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Configure residential towers, floors, flat units, and manage resident occupancy.
          </p>
        </div>

        <div className="flex items-center gap-2 self-start">
          <button
            onClick={() => setAddBlockOpen(true)}
            className="px-3.5 py-2 bg-surface hover:bg-border text-text-main text-xs font-semibold rounded-lg border border-border transition-colors flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" /> Add Block
          </button>
          <button
            onClick={() => setAddFlatOpen(true)}
            disabled={!selectedBlockId}
            className="px-3.5 py-2 bg-primary hover:bg-primary-hover text-white text-xs font-semibold rounded-lg shadow-sm transition-colors flex items-center gap-1.5 disabled:opacity-50"
          >
            <Plus className="w-4 h-4" /> Add Flat in {selectedBlock?.name || 'Block'}
          </button>
        </div>
      </div>

      {error && (
        <div className="p-3.5 bg-status-rejected-bg border border-status-rejected-border rounded-xl text-xs text-status-rejected-text flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* Block Selector Tabs */}
      <div className="flex items-center gap-2 border-b border-border pb-3 overflow-x-auto">
        {blocks.map((b) => (
          <button
            key={b.id}
            onClick={() => setSelectedBlockId(b.id)}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-2 flex-shrink-0 ${
              selectedBlockId === b.id
                ? 'bg-primary text-white shadow-sm'
                : 'bg-surface text-text-secondary hover:text-text-main border border-border'
            }`}
          >
            <Building2 className="w-3.5 h-3.5" />
            <span>{b.name}</span>
            <span
              className={`text-[10px] px-1.5 py-0.2 rounded-full ${
                selectedBlockId === b.id ? 'bg-white/20 text-white' : 'bg-border text-text-secondary'
              }`}
            >
              {flats.filter((f) => f.blockId === b.id).length} flats
            </span>
          </button>
        ))}
      </div>

      {/* Selected Block Flats Grid */}
      <div>
        <div className="flex items-center justify-between mb-3 text-xs text-text-secondary">
          <span>
            Showing flats in <strong className="text-text-main">{selectedBlock?.name}</strong> (Total Floors: {selectedBlock?.totalFloors})
          </span>
          <span className="text-[11px] text-text-muted">{selectedBlock?.description}</span>
        </div>

        {loading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="h-28 bg-white border border-border rounded-xl animate-pulse" />
            ))}
          </div>
        ) : activeFlats.length === 0 ? (
          <div className="bg-white border border-border rounded-xl p-12 text-center max-w-sm mx-auto shadow-card">
            <Home className="w-8 h-8 text-text-muted mx-auto mb-2" />
            <div className="text-xs font-bold text-text-main">No flats configured yet</div>
            <p className="text-[11px] text-text-secondary mt-1">
              Click 'Add Flat' above to register flat units in this tower.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
            {activeFlats.map((flat) => (
              <div
                key={flat.id}
                className="bg-white border border-border rounded-xl p-3.5 shadow-card hover:border-primary transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-bold text-text-main">{flat.flatNumber}</span>
                    <span className="text-[10px] text-text-muted bg-surface px-1.5 py-0.5 rounded border border-border">
                      Fl. {flat.floorNumber}
                    </span>
                  </div>
                  {flat.extensionNumber && (
                    <div className="text-[10px] text-text-secondary mt-1">
                      Ext: {flat.extensionNumber}
                    </div>
                  )}
                </div>

                <div className="mt-3 pt-2 border-t border-border-light flex items-center justify-between text-[11px]">
                  <span className="flex items-center gap-1 text-text-secondary">
                    <Users className="w-3 h-3 text-primary" />
                    {flat.activeResidentsCount || 0} residents
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Block Modal */}
      <Modal isOpen={addBlockOpen} onClose={() => setAddBlockOpen(false)} title="Create Apartment Block / Tower">
        <form onSubmit={handleCreateBlock} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-semibold text-text-main mb-1">Block Name *</label>
            <input
              type="text"
              required
              value={blockName}
              onChange={(e) => setBlockName(e.target.value)}
              placeholder="E.g. Block D (West Tower)"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Description</label>
            <input
              type="text"
              value={blockDesc}
              onChange={(e) => setBlockDesc(e.target.value)}
              placeholder="Premium high-rise facing clubhouse"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Total Floors</label>
            <input
              type="number"
              min={1}
              required
              value={blockFloors}
              onChange={(e) => setBlockFloors(Number(e.target.value))}
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-border">
            <button
              type="button"
              onClick={() => setAddBlockOpen(false)}
              className="px-3 py-1.5 border border-border rounded-lg font-semibold hover:bg-surface"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-1.5 bg-primary text-white font-semibold rounded-lg hover:bg-primary-hover transition-colors disabled:opacity-50"
            >
              {saving ? 'Creating...' : 'Create Block'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Add Flat Modal */}
      <Modal
        isOpen={addFlatOpen}
        onClose={() => setAddFlatOpen(false)}
        title={`Add Flat in ${selectedBlock?.name}`}
      >
        <form onSubmit={handleCreateFlat} className="space-y-3.5 text-xs">
          <div>
            <label className="block font-semibold text-text-main mb-1">Flat Number *</label>
            <input
              type="text"
              required
              value={flatNumber}
              onChange={(e) => setFlatNumber(e.target.value)}
              placeholder="E.g. A-501 or 502"
              className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block font-semibold text-text-main mb-1">Floor Number *</label>
              <input
                type="number"
                min={1}
                required
                value={flatFloor}
                onChange={(e) => setFlatFloor(Number(e.target.value))}
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
            <div>
              <label className="block font-semibold text-text-main mb-1">Extension Number</label>
              <input
                type="text"
                value={flatExtension}
                onChange={(e) => setFlatExtension(e.target.value)}
                placeholder="E.g. 1501"
                className="w-full px-3 py-2 bg-surface border border-border rounded-lg outline-none focus:border-primary focus:bg-white text-xs"
              />
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-border">
            <button
              type="button"
              onClick={() => setAddFlatOpen(false)}
              className="px-3 py-1.5 border border-border rounded-lg font-semibold hover:bg-surface"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-1.5 bg-primary text-white font-semibold rounded-lg hover:bg-primary-hover transition-colors disabled:opacity-50"
            >
              {saving ? 'Adding...' : 'Add Flat'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
