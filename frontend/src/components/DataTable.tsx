import React from 'react';
import { ChevronLeft, ChevronRight, Search, Inbox } from 'lucide-react';

interface Column<T> {
  header: string;
  accessor?: keyof T;
  render?: (item: T) => React.ReactNode;
  className?: string;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  loading?: boolean;
  total?: number;
  page?: number;
  limit?: number;
  onPageChange?: (newPage: number) => void;
  searchValue?: string;
  onSearchChange?: (val: string) => void;
  searchPlaceholder?: string;
  emptyTitle?: string;
  emptyDescription?: string;
  extraActions?: React.ReactNode;
}

export function DataTable<T extends { id?: string | number }>({
  columns,
  data,
  loading = false,
  total = 0,
  page = 1,
  limit = 15,
  onPageChange,
  searchValue,
  onSearchChange,
  searchPlaceholder = 'Search records...',
  emptyTitle = 'No records found',
  emptyDescription = 'Try adjusting your filters or search criteria.',
  extraActions,
}: DataTableProps<T>) {
  const totalPages = Math.ceil(total / limit) || 1;

  return (
    <div className="bg-white border border-border rounded-xl shadow-card overflow-hidden flex flex-col">
      {/* Table Toolbar */}
      {(onSearchChange || extraActions) && (
        <div className="p-4 border-b border-border flex flex-col sm:flex-row items-center justify-between gap-3 bg-surface/30">
          {onSearchChange && (
            <div className="relative w-full sm:w-72">
              <Search className="w-4 h-4 text-text-muted absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={searchValue || ''}
                onChange={(e) => onSearchChange(e.target.value)}
                placeholder={searchPlaceholder}
                className="w-full pl-9 pr-3 py-1.5 text-xs bg-white border border-border rounded-lg focus:border-primary focus:ring-1 focus:ring-primary outline-none transition-all placeholder:text-text-muted"
              />
            </div>
          )}

          {extraActions && <div className="flex items-center gap-2 w-full sm:w-auto justify-end">{extraActions}</div>}
        </div>
      )}

      {/* Table Area */}
      <div className="overflow-x-auto min-h-[220px]">
        <table className="w-full text-left border-collapse text-xs">
          <thead>
            <tr className="bg-surface border-b border-border text-text-secondary font-semibold uppercase tracking-wider text-[10px]">
              {columns.map((col, idx) => (
                <th key={idx} className={`py-3 px-4 ${col.className || ''}`}>
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border-light text-text-main">
            {loading ? (
              // Skeleton Loading Rows
              Array.from({ length: 5 }).map((_, rIdx) => (
                <tr key={`skel-${rIdx}`} className="animate-pulse">
                  {columns.map((_, cIdx) => (
                    <td key={`skel-c-${cIdx}`} className="py-3 px-4">
                      <div className="h-4 bg-border/60 rounded w-4/5" />
                    </td>
                  ))}
                </tr>
              ))
            ) : data.length === 0 ? (
              // Empty State
              <tr>
                <td colSpan={columns.length} className="py-12 px-4 text-center">
                  <div className="max-w-xs mx-auto flex flex-col items-center">
                    <div className="w-12 h-12 rounded-full bg-accent-blue flex items-center justify-center text-primary mb-3">
                      <Inbox className="w-6 h-6" />
                    </div>
                    <div className="text-sm font-bold text-text-main mb-1">{emptyTitle}</div>
                    <p className="text-xs text-text-secondary">{emptyDescription}</p>
                  </div>
                </td>
              </tr>
            ) : (
              data.map((item, rowIdx) => (
                <tr
                  key={item.id ? String(item.id) : `row-${rowIdx}`}
                  className="hover:bg-surface/60 transition-colors"
                >
                  {columns.map((col, colIdx) => (
                    <td key={colIdx} className={`py-3 px-4 ${col.className || ''}`}>
                      {col.render
                        ? col.render(item)
                        : col.accessor
                        ? String(item[col.accessor] ?? '')
                        : null}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Footer */}
      {onPageChange && total > 0 && (
        <div className="p-3 border-t border-border bg-surface/30 flex items-center justify-between text-xs text-text-secondary">
          <div>
            Showing <strong className="text-text-main">{(page - 1) * limit + 1}</strong> to{' '}
            <strong className="text-text-main">{Math.min(page * limit, total)}</strong> of{' '}
            <strong className="text-text-main">{total}</strong> results
          </div>

          <div className="flex items-center gap-1">
            <button
              onClick={() => onPageChange(page - 1)}
              disabled={page <= 1}
              className="p-1.5 rounded-lg border border-border bg-white text-text-main hover:bg-surface disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              aria-label="Previous page"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="px-2 font-medium">
              Page {page} of {totalPages}
            </span>
            <button
              onClick={() => onPageChange(page + 1)}
              disabled={page >= totalPages}
              className="p-1.5 rounded-lg border border-border bg-white text-text-main hover:bg-surface disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              aria-label="Next page"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
