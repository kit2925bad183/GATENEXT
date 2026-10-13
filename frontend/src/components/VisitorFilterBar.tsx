import React, { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api } from '@/lib/api';
import { useI18n } from '@/i18n/I18nContext';
import {
  Search,
  Filter,
  X,
  Calendar,
  Building,
  DoorClosed,
  Clock,
  UserCheck,
  Shield,
  RotateCcw,
  SlidersHorizontal,
  BookmarkPlus,
  Trash2
} from 'lucide-react';

export interface FilterState {
  blockId?: string;
  floorNumber?: string;
  flatId?: string;
  entryGateId?: string;
  exitGateId?: string;
  securityStaffId?: string;
  status?: string;
  category?: string;
  isWalkIn?: string;
  isOverdue?: string;
  query?: string;
  datePreset?: string;
  dateBasis?: 'ACTUAL_CHECK_IN' | 'ACTUAL_CHECK_OUT' | 'SCHEDULED_ARRIVAL' | 'RECORD_CREATION';
  fromDate?: string;
  toDate?: string;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

interface BlockOption {
  id: number;
  name: string;
  totalFloors: number;
}

interface FlatOption {
  id: number;
  blockId: number;
  flatNumber: string;
  floorNumber: number;
}

interface GateOption {
  id: number;
  name: string;
  code: string;
}

interface VisitorFilterBarProps {
  onFilterChange: (filters: FilterState) => void;
  showApartmentFilters?: boolean;
  showGateFilters?: boolean;
  defaultDateBasis?: 'ACTUAL_CHECK_IN' | 'ACTUAL_CHECK_OUT' | 'SCHEDULED_ARRIVAL' | 'RECORD_CREATION';
  onExportCsv?: () => void;
  storagePresetKey?: string;
}

export const VisitorFilterBar: React.FC<VisitorFilterBarProps> = ({
  onFilterChange,
  showApartmentFilters = true,
  showGateFilters = true,
  defaultDateBasis = 'ACTUAL_CHECK_IN',
  onExportCsv,
  storagePresetKey = 'gatenest_filter_preset'
}) => {
  const { t } = useI18n();
  const [searchParams, setSearchParams] = useSearchParams();

  // Filter Drawer State
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);

  // Metadata dropdown options
  const [blocks, setBlocks] = useState<BlockOption[]>([]);
  const [flats, setFlats] = useState<FlatOption[]>([]);
  const [gates, setGates] = useState<GateOption[]>([]);

  // Local Filter State
  const [filters, setFilters] = useState<FilterState>(() => {
    return {
      query: searchParams.get('query') || '',
      blockId: searchParams.get('blockId') || '',
      floorNumber: searchParams.get('floorNumber') || '',
      flatId: searchParams.get('flatId') || '',
      entryGateId: searchParams.get('entryGateId') || '',
      exitGateId: searchParams.get('exitGateId') || '',
      securityStaffId: searchParams.get('securityStaffId') || '',
      status: searchParams.get('status') || '',
      category: searchParams.get('category') || '',
      isWalkIn: searchParams.get('isWalkIn') || '',
      isOverdue: searchParams.get('isOverdue') || '',
      datePreset: searchParams.get('datePreset') || 'all',
      dateBasis: (searchParams.get('dateBasis') as any) || defaultDateBasis,
      fromDate: searchParams.get('fromDate') || '',
      toDate: searchParams.get('toDate') || '',
      sortBy: searchParams.get('sortBy') || 'id',
      sortDirection: (searchParams.get('sortDirection') as any) || 'DESC'
    };
  });

  // Debounce search query
  const [debouncedQuery, setDebouncedQuery] = useState(filters.query || '');

  useEffect(() => {
    const handler = setTimeout(() => {
      setFilters(prev => ({ ...prev, query: debouncedQuery }));
    }, 350);
    return () => clearTimeout(handler);
  }, [debouncedQuery]);

  // Load Blocks, Flats, Gates
  useEffect(() => {
    const loadMetadata = async () => {
      try {
        const [bRes, fRes, gRes] = await Promise.all([
          api.get<{ data: BlockOption[] }>('/community/blocks'),
          api.get<{ data: FlatOption[] }>('/community/flats'),
          api.get<{ data: GateOption[] }>('/community/gates')
        ]);
        setBlocks(bRes.data?.data || []);
        setFlats(fRes.data?.data || []);
        setGates(gRes.data?.data || []);
      } catch (err) {
        console.error('Failed to load filter dropdown metadata', err);
      }
    };
    loadMetadata();
  }, []);

  // Filter available floors based on selected block
  const availableFloors = useMemo(() => {
    if (!filters.blockId) return [];
    const selectedBlock = blocks.find(b => String(b.id) === filters.blockId);
    if (!selectedBlock) return [];
    const floors: number[] = [];
    for (let i = 1; i <= selectedBlock.totalFloors; i++) {
      floors.push(i);
    }
    return floors;
  }, [filters.blockId, blocks]);

  // Filter available flats based on selected block and floor
  const availableFlats = useMemo(() => {
    return flats.filter(f => {
      if (filters.blockId && String(f.blockId) !== filters.blockId) return false;
      if (filters.floorNumber && String(f.floorNumber) !== filters.floorNumber) return false;
      return true;
    });
  }, [flats, filters.blockId, filters.floorNumber]);

  // Calculate inclusive start and exclusive end dates for time presets
  const calculateDateRange = (preset: string): { from: string; to: string } => {
    const now = new Date();
    const pad = (n: number) => n.toString().padStart(2, '0');
    const toYmd = (d: Date) => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

    if (preset === 'today') {
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      return { from: today.toISOString(), to: tomorrow.toISOString() };
    }
    if (preset === 'yesterday') {
      const yesterday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1);
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      return { from: yesterday.toISOString(), to: today.toISOString() };
    }
    if (preset === 'this_week') {
      // Monday through Sunday
      const d = new Date(now);
      const day = d.getDay();
      const diff = d.getDate() - day + (day === 0 ? -6 : 1); // adjust when day is sunday
      const mon = new Date(d.setDate(diff));
      mon.setHours(0, 0, 0, 0);
      const nextMon = new Date(mon);
      nextMon.setDate(nextMon.getDate() + 7);
      return { from: mon.toISOString(), to: nextMon.toISOString() };
    }
    if (preset === 'last_week') {
      const d = new Date(now);
      const day = d.getDay();
      const diff = d.getDate() - day + (day === 0 ? -6 : 1) - 7;
      const lastMon = new Date(d.setDate(diff));
      lastMon.setHours(0, 0, 0, 0);
      const thisMon = new Date(lastMon);
      thisMon.setDate(thisMon.getDate() + 7);
      return { from: lastMon.toISOString(), to: thisMon.toISOString() };
    }
    if (preset === 'this_month') {
      const first = new Date(now.getFullYear(), now.getMonth(), 1);
      const nextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);
      return { from: first.toISOString(), to: nextMonth.toISOString() };
    }
    if (preset === 'last_month') {
      const firstLastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
      const firstThisMonth = new Date(now.getFullYear(), now.getMonth(), 1);
      return { from: firstLastMonth.toISOString(), to: firstThisMonth.toISOString() };
    }
    return { from: '', to: '' };
  };

  // Synchronize state changes with URL and trigger parent callback
  useEffect(() => {
    let effectiveFrom = filters.fromDate;
    let effectiveTo = filters.toDate;

    if (filters.datePreset && filters.datePreset !== 'all' && filters.datePreset !== 'custom') {
      const range = calculateDateRange(filters.datePreset);
      effectiveFrom = range.from;
      effectiveTo = range.to;
    }

    const updatedFilters: FilterState = {
      ...filters,
      fromDate: effectiveFrom,
      toDate: effectiveTo
    };

    // Update URL query params without reloading
    const params = new URLSearchParams();
    Object.entries(updatedFilters).forEach(([key, val]) => {
      if (val) params.set(key, String(val));
    });
    setSearchParams(params, { replace: true });

    onFilterChange(updatedFilters);
  }, [filters]);

  // Handle dependent filter resets
  const handleBlockChange = (blockId: string) => {
    setFilters(prev => ({
      ...prev,
      blockId,
      floorNumber: '',
      flatId: ''
    }));
  };

  const handleFloorChange = (floorNumber: string) => {
    setFilters(prev => ({
      ...prev,
      floorNumber,
      flatId: ''
    }));
  };

  const handleDatePresetChange = (preset: string) => {
    if (preset === 'custom') {
      setFilters(prev => ({ ...prev, datePreset: 'custom' }));
    } else if (preset === 'all') {
      setFilters(prev => ({ ...prev, datePreset: 'all', fromDate: '', toDate: '' }));
    } else {
      const range = calculateDateRange(preset);
      setFilters(prev => ({
        ...prev,
        datePreset: preset,
        fromDate: range.from,
        toDate: range.to
      }));
    }
  };

  const handleClearAll = () => {
    setDebouncedQuery('');
    setFilters({
      query: '',
      blockId: '',
      floorNumber: '',
      flatId: '',
      entryGateId: '',
      exitGateId: '',
      securityStaffId: '',
      status: '',
      category: '',
      isWalkIn: '',
      isOverdue: '',
      datePreset: 'all',
      dateBasis: defaultDateBasis,
      fromDate: '',
      toDate: '',
      sortBy: 'id',
      sortDirection: 'DESC'
    });
  };

  const handleSavePreset = () => {
    localStorage.setItem(storagePresetKey, JSON.stringify(filters));
    alert('Filter preferences saved successfully!');
  };

  const handleLoadPreset = () => {
    const saved = localStorage.getItem(storagePresetKey);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        setFilters(parsed);
        setDebouncedQuery(parsed.query || '');
      } catch (e) {
        console.error(e);
      }
    }
  };

  // Count active filters
  const activeFilterCount = useMemo(() => {
    let count = 0;
    if (filters.blockId) count++;
    if (filters.floorNumber) count++;
    if (filters.flatId) count++;
    if (filters.entryGateId) count++;
    if (filters.exitGateId) count++;
    if (filters.status) count++;
    if (filters.category) count++;
    if (filters.isWalkIn) count++;
    if (filters.isOverdue) count++;
    if (filters.datePreset && filters.datePreset !== 'all') count++;
    return count;
  }, [filters]);

  return (
    <div className="bg-white border border-border rounded-xl p-4 mb-5 shadow-xs">
      {/* Top Search & Filter Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
        {/* Search Input */}
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={debouncedQuery}
            onChange={(e) => setDebouncedQuery(e.target.value)}
            placeholder="Search visitor, phone, flat, ref..."
            className="w-full pl-9 pr-3 py-2 bg-surface border border-border rounded-lg text-xs focus:bg-white focus:border-primary outline-none transition-all"
          />
          {debouncedQuery && (
            <button
              onClick={() => setDebouncedQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-main"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Quick Actions & Filter Trigger */}
        <div className="flex items-center gap-2 w-full sm:w-auto justify-between sm:justify-end">
          <button
            type="button"
            onClick={() => setIsDrawerOpen(!isDrawerOpen)}
            className={`inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-lg border transition-all ${
              isDrawerOpen || activeFilterCount > 0
                ? 'bg-blue-50 border-primary text-primary'
                : 'bg-white border-border text-text-secondary hover:bg-surface'
            }`}
          >
            <Filter className="w-3.5 h-3.5" />
            <span>Filters</span>
            {activeFilterCount > 0 && (
              <span className="w-4 h-4 rounded-full bg-primary text-white text-[10px] flex items-center justify-center font-bold">
                {activeFilterCount}
              </span>
            )}
          </button>

          {activeFilterCount > 0 && (
            <button
              type="button"
              onClick={handleClearAll}
              className="inline-flex items-center gap-1 text-xs text-text-muted hover:text-red-600 transition-colors py-1.5 px-2"
              title="Reset all filters"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Reset</span>
            </button>
          )}

          {onExportCsv && (
            <button
              type="button"
              onClick={onExportCsv}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-primary bg-accent-blue/40 hover:bg-accent-blue border border-border rounded-lg transition-colors"
            >
              📥 Export CSV
            </button>
          )}
        </div>
      </div>

      {/* Expanded Filter Panel */}
      {isDrawerOpen && (
        <div className="mt-4 pt-4 border-t border-border grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5 text-xs">
          {/* 1. Apartment Filters */}
          {showApartmentFilters && (
            <>
              <div>
                <label className="block font-semibold text-text-main mb-1 flex items-center gap-1">
                  <Building className="w-3.5 h-3.5 text-primary" />
                  Block
                </label>
                <select
                  value={filters.blockId || ''}
                  onChange={(e) => handleBlockChange(e.target.value)}
                  className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
                >
                  <option value="">All Blocks</option>
                  {blocks.map(b => (
                    <option key={b.id} value={String(b.id)}>
                      {b.name} ({b.totalFloors} floors)
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-text-main mb-1">Floor</label>
                <select
                  disabled={!filters.blockId}
                  value={filters.floorNumber || ''}
                  onChange={(e) => handleFloorChange(e.target.value)}
                  className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary disabled:opacity-50"
                >
                  <option value="">All Floors</option>
                  {availableFloors.map(fl => (
                    <option key={fl} value={String(fl)}>
                      Floor {fl}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-text-main mb-1 flex items-center gap-1">
                  <DoorClosed className="w-3.5 h-3.5 text-teal-600" />
                  Flat Number
                </label>
                <select
                  disabled={!filters.blockId}
                  value={filters.flatId || ''}
                  onChange={(e) => setFilters(prev => ({ ...prev, flatId: e.target.value }))}
                  className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary disabled:opacity-50"
                >
                  <option value="">All Flats in Selection</option>
                  {availableFlats.map(f => (
                    <option key={f.id} value={String(f.id)}>
                      Flat {f.flatNumber} (Fl {f.floorNumber})
                    </option>
                  ))}
                </select>
              </div>
            </>
          )}

          {/* 2. Status & Category */}
          <div>
            <label className="block font-semibold text-text-main mb-1">Visit Status</label>
            <select
              value={filters.status || ''}
              onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
              className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
            >
              <option value="">All Statuses</option>
              <option value="PENDING_APPROVAL">Pending Approval</option>
              <option value="APPROVED">Approved</option>
              <option value="INSIDE">Inside Now</option>
              <option value="EXITED">Exited / Completed</option>
              <option value="REJECTED">Rejected</option>
              <option value="CANCELLED">Cancelled</option>
              <option value="EXPIRED">Expired</option>
            </select>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1">Visitor Category</label>
            <select
              value={filters.category || ''}
              onChange={(e) => setFilters(prev => ({ ...prev, category: e.target.value }))}
              className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
            >
              <option value="">All Categories</option>
              <option value="GUEST">Guest</option>
              <option value="DELIVERY">Delivery</option>
              <option value="SERVICE">Service & Repair</option>
              <option value="CAB">Taxi / Cab</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          {/* 3. Gate Filters */}
          {showGateFilters && (
            <>
              <div>
                <label className="block font-semibold text-text-main mb-1">Entry Gate</label>
                <select
                  value={filters.entryGateId || ''}
                  onChange={(e) => setFilters(prev => ({ ...prev, entryGateId: e.target.value }))}
                  className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
                >
                  <option value="">All Entry Gates</option>
                  {gates.map(g => (
                    <option key={g.id} value={String(g.id)}>
                      {g.name} ({g.code})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-text-main mb-1">Exit Gate</label>
                <select
                  value={filters.exitGateId || ''}
                  onChange={(e) => setFilters(prev => ({ ...prev, exitGateId: e.target.value }))}
                  className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
                >
                  <option value="">All Exit Gates</option>
                  {gates.map(g => (
                    <option key={g.id} value={String(g.id)}>
                      {g.name} ({g.code})
                    </option>
                  ))}
                </select>
              </div>
            </>
          )}

          {/* 4. Time Presets & Date Basis */}
          <div>
            <label className="block font-semibold text-text-main mb-1 flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5 text-primary" />
              Time Range
            </label>
            <select
              value={filters.datePreset || 'all'}
              onChange={(e) => handleDatePresetChange(e.target.value)}
              className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
            >
              <option value="all">All Time</option>
              <option value="today">Today</option>
              <option value="yesterday">Yesterday</option>
              <option value="this_week">This Week (Mon-Sun)</option>
              <option value="last_week">Last Week</option>
              <option value="this_month">This Month</option>
              <option value="last_month">Last Month</option>
              <option value="custom">Custom Date Range</option>
            </select>
          </div>

          <div>
            <label className="block font-semibold text-text-main mb-1 flex items-center gap-1">
              <Clock className="w-3.5 h-3.5 text-text-muted" />
              Date Filter Based On
            </label>
            <select
              value={filters.dateBasis || defaultDateBasis}
              onChange={(e) => setFilters(prev => ({ ...prev, dateBasis: e.target.value as any }))}
              className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary font-medium text-text-main"
            >
              <option value="ACTUAL_CHECK_IN">Actual Check-In Time</option>
              <option value="ACTUAL_CHECK_OUT">Actual Check-Out Time</option>
              <option value="SCHEDULED_ARRIVAL">Scheduled Arrival Window</option>
              <option value="RECORD_CREATION">Record Creation Date</option>
            </select>
          </div>

          {filters.datePreset === 'custom' && (
            <>
              <div>
                <label className="block font-semibold text-text-main mb-1">From Date</label>
                <input
                  type="datetime-local"
                  value={filters.fromDate?.slice(0, 16) || ''}
                  onChange={(e) => setFilters(prev => ({ ...prev, fromDate: e.target.value ? new Date(e.target.value).toISOString() : '' }))}
                  className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block font-semibold text-text-main mb-1">To Date</label>
                <input
                  type="datetime-local"
                  value={filters.toDate?.slice(0, 16) || ''}
                  onChange={(e) => setFilters(prev => ({ ...prev, toDate: e.target.value ? new Date(e.target.value).toISOString() : '' }))}
                  className="w-full px-2.5 py-1.5 bg-surface border border-border rounded-lg text-xs outline-none focus:border-primary"
                />
              </div>
            </>
          )}

          {/* 5. Special Flags */}
          <div className="flex items-center gap-4 pt-2 col-span-full">
            <label className="inline-flex items-center gap-1.5 cursor-pointer text-text-secondary hover:text-text-main">
              <input
                type="checkbox"
                checked={filters.isOverdue === 'true'}
                onChange={(e) => setFilters(prev => ({ ...prev, isOverdue: e.target.checked ? 'true' : '' }))}
                className="rounded border-border text-primary focus:ring-primary"
              />
              <span className="font-semibold text-red-600">Overdue Visits Only</span>
            </label>

            <label className="inline-flex items-center gap-1.5 cursor-pointer text-text-secondary hover:text-text-main">
              <input
                type="checkbox"
                checked={filters.isWalkIn === 'true'}
                onChange={(e) => setFilters(prev => ({ ...prev, isWalkIn: e.target.checked ? 'true' : '' }))}
                className="rounded border-border text-primary focus:ring-primary"
              />
              <span>Walk-In Visits Only</span>
            </label>

            {/* Presets storage buttons */}
            <div className="ml-auto flex items-center gap-2">
              <button
                type="button"
                onClick={handleSavePreset}
                className="text-[11px] font-semibold text-primary hover:underline inline-flex items-center gap-1"
              >
                <BookmarkPlus className="w-3 h-3" />
                Save Preset
              </button>
              <button
                type="button"
                onClick={handleLoadPreset}
                className="text-[11px] font-semibold text-text-secondary hover:underline"
              >
                Load Preset
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Active Filter Chips */}
      {activeFilterCount > 0 && (
        <div className="flex flex-wrap items-center gap-1.5 mt-3 pt-2.5 border-t border-border/60">
          <span className="text-[11px] font-bold text-text-muted uppercase tracking-wider mr-1">Active:</span>

          {filters.blockId && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-blue-50 text-[#1E40AF] text-[11px] font-semibold border border-blue-100">
              Block: {blocks.find(b => String(b.id) === filters.blockId)?.name || filters.blockId}
              <button onClick={() => handleBlockChange('')}><X className="w-3 h-3" /></button>
            </span>
          )}

          {filters.floorNumber && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-blue-50 text-[#1E40AF] text-[11px] font-semibold border border-blue-100">
              Floor {filters.floorNumber}
              <button onClick={() => setFilters(p => ({ ...p, floorNumber: '', flatId: '' }))}><X className="w-3 h-3" /></button>
            </span>
          )}

          {filters.flatId && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-teal-50 text-teal-700 text-[11px] font-semibold border border-teal-100">
              Flat: {flats.find(f => String(f.id) === filters.flatId)?.flatNumber || filters.flatId}
              <button onClick={() => setFilters(p => ({ ...p, flatId: '' }))}><X className="w-3 h-3" /></button>
            </span>
          )}

          {filters.status && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-gray-100 text-gray-800 text-[11px] font-semibold">
              Status: {filters.status}
              <button onClick={() => setFilters(p => ({ ...p, status: '' }))}><X className="w-3 h-3" /></button>
            </span>
          )}

          {filters.category && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-gray-100 text-gray-800 text-[11px] font-semibold">
              Category: {filters.category}
              <button onClick={() => setFilters(p => ({ ...p, category: '' }))}><X className="w-3 h-3" /></button>
            </span>
          )}

          {filters.datePreset && filters.datePreset !== 'all' && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-indigo-50 text-indigo-700 text-[11px] font-semibold border border-indigo-100">
              Period: {filters.datePreset.replace('_', ' ')} (by {filters.dateBasis || defaultDateBasis})
              <button onClick={() => handleDatePresetChange('all')}><X className="w-3 h-3" /></button>
            </span>
          )}

          {filters.isOverdue === 'true' && (
            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md bg-red-50 text-red-700 text-[11px] font-semibold border border-red-100">
              Overdue Only
              <button onClick={() => setFilters(p => ({ ...p, isOverdue: '' }))}><X className="w-3 h-3" /></button>
            </span>
          )}

          <button
            onClick={handleClearAll}
            className="text-[11px] text-text-muted hover:text-red-600 underline font-medium ml-1"
          >
            Clear All
          </button>
        </div>
      )}
    </div>
  );
};
