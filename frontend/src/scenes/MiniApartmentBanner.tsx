import React from 'react';

export const MiniApartmentBanner: React.FC<{ title?: string; subtitle?: string; badge?: string }> = ({
  title = 'Community Visitor Security',
  subtitle = 'Monitored access & verified entry across all gates and resident towers.',
  badge = 'Active Gate Monitoring',
}) => {
  return (
    <div className="relative overflow-hidden bg-gradient-to-r from-primary-light via-white to-surface border border-border rounded-xl p-6 mb-6 shadow-sm">
      <div className="max-w-xl relative z-10">
        <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary text-white mb-2.5">
          <span className="w-1.5 h-1.5 rounded-full bg-white animate-pulse" />
          {badge}
        </span>
        <h2 className="text-xl font-bold text-text-main tracking-tight mb-1">{title}</h2>
        <p className="text-sm text-text-secondary leading-relaxed">{subtitle}</p>
      </div>

      {/* Decorative Miniature SVG Complex Graphic at the Right */}
      <div className="absolute -right-4 -bottom-6 w-56 h-36 opacity-85 select-none pointer-events-none hidden md:block">
        <svg viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg" className="w-full h-full">
          {/* Ground */}
          <ellipse cx="100" cy="110" rx="90" ry="12" fill="#E3E9F2" />
          {/* Tower 1 */}
          <rect x="35" y="40" width="35" height="70" rx="2" fill="#FFFFFF" stroke="#CAD4E2" />
          <rect x="35" y="38" width="35" height="4" fill="#2855D9" />
          <rect x="42" y="48" width="8" height="8" fill="#EDF3FF" />
          <rect x="54" y="48" width="8" height="8" fill="#087F8C" />
          <rect x="42" y="62" width="8" height="8" fill="#087F8C" />
          <rect x="54" y="62" width="8" height="8" fill="#EDF3FF" />
          {/* Tower 2 Center */}
          <rect x="75" y="20" width="45" height="90" rx="2" fill="#FFFFFF" stroke="#CAD4E2" />
          <rect x="75" y="17" width="45" height="5" fill="#087F8C" />
          <rect x="85" y="28" width="10" height="10" fill="#2855D9" />
          <rect x="100" y="28" width="10" height="10" fill="#EDF3FF" />
          <rect x="85" y="45" width="10" height="10" fill="#EDF3FF" />
          <rect x="100" y="45" width="10" height="10" fill="#2855D9" />
          <rect x="85" y="62" width="10" height="10" fill="#2855D9" />
          <rect x="100" y="62" width="10" height="10" fill="#EDF3FF" />
          {/* Tower 3 Right */}
          <rect x="125" y="45" width="32" height="65" rx="2" fill="#FFFFFF" stroke="#CAD4E2" />
          <rect x="125" y="43" width="32" height="4" fill="#2855D9" />
          <rect x="132" y="55" width="8" height="8" fill="#EDF3FF" />
          <rect x="143" y="55" width="8" height="8" fill="#087F8C" />
          {/* Gatehouse Front */}
          <rect x="86" y="95" width="22" height="15" rx="1" fill="#FFFFFF" stroke="#2855D9" />
          <rect x="92" y="100" width="10" height="10" fill="#EDF3FF" />
        </svg>
      </div>
    </div>
  );
};
