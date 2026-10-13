import React from 'react';

interface ApartmentSvgFallbackProps {
  className?: string;
}

export const ApartmentSvgFallback: React.FC<ApartmentSvgFallbackProps> = ({ className = '' }) => {
  return (
    <div className={`w-full h-full flex items-center justify-center bg-surface p-6 select-none ${className}`}>
      <svg
        viewBox="0 0 600 500"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="w-full max-w-lg h-auto drop-shadow-sm"
      >
        {/* Soft Background Grid & Sky */}
        <rect width="600" height="500" rx="16" fill="#F7F9FC" />
        <circle cx="480" cy="90" r="45" fill="#EDF3FF" />

        {/* Landscaping / Ground Planes */}
        <ellipse cx="300" cy="420" rx="260" ry="40" fill="#E3E9F2" opacity="0.6" />
        <path d="M40 420C40 420 180 435 300 435C420 435 560 420 560 420L580 480H20L40 420Z" fill="#EDF3FF" />

        {/* Entrance Gate Road */}
        <polygon points="260,420 340,420 380,480 220,480" fill="#CAD4E2" />
        <line x1="300" y1="425" x2="300" y2="475" stroke="#FFFFFF" strokeWidth="4" strokeDasharray="8 8" />

        {/* Building 1 (Left Tower) */}
        <rect x="90" y="160" width="110" height="250" rx="4" fill="#FFFFFF" stroke="#E3E9F2" strokeWidth="2" />
        <rect x="90" y="150" width="110" height="10" rx="2" fill="#2855D9" />
        {/* Windows Tower 1 */}
        {[0, 1, 2, 3, 4, 5].map((row) => (
          <g key={`b1-row-${row}`}>
            <rect x="105" y={180 + row * 36} width="20" height="22" rx="2" fill="#EDF3FF" stroke="#CAD4E2" strokeWidth="1" />
            <rect x="135" y={180 + row * 36} width="20" height="22" rx="2" fill="#EDF3FF" stroke="#CAD4E2" strokeWidth="1" />
            <rect x="165" y={180 + row * 36} width="20" height="22" rx="2" fill="#087F8C" opacity="0.85" />
          </g>
        ))}

        {/* Building 2 (Center Main Tower - Higher) */}
        <rect x="230" y="90" width="140" height="320" rx="4" fill="#FFFFFF" stroke="#E3E9F2" strokeWidth="2" />
        <rect x="230" y="80" width="140" height="10" rx="2" fill="#087F8C" />
        {/* Center Tower Balconies & Windows */}
        {[0, 1, 2, 3, 4, 5, 6, 7].map((row) => (
          <g key={`b2-row-${row}`}>
            <rect x="250" y={110 + row * 36} width="22" height="22" rx="2" fill="#EDF3FF" stroke="#CAD4E2" strokeWidth="1" />
            <rect x="285" y={110 + row * 36} width="30" height="22" rx="2" fill="#2855D9" opacity="0.9" />
            <rect x="330" y={110 + row * 36} width="22" height="22" rx="2" fill="#EDF3FF" stroke="#CAD4E2" strokeWidth="1" />
            {/* Balcony Railing */}
            <line x1="280" y1={130 + row * 36} x2="320" y2={130 + row * 36} stroke="#CAD4E2" strokeWidth="2" />
          </g>
        ))}

        {/* Building 3 (Right Tower) */}
        <rect x="400" y="180" width="105" height="230" rx="4" fill="#FFFFFF" stroke="#E3E9F2" strokeWidth="2" />
        <rect x="400" y="170" width="105" height="10" rx="2" fill="#2855D9" />
        {/* Windows Tower 3 */}
        {[0, 1, 2, 3, 4].map((row) => (
          <g key={`b3-row-${row}`}>
            <rect x="415" y={200 + row * 38} width="20" height="24" rx="2" fill="#EDF3FF" stroke="#CAD4E2" strokeWidth="1" />
            <rect x="445" y={200 + row * 38} width="20" height="24" rx="2" fill="#087F8C" opacity="0.8" />
            <rect x="475" y={200 + row * 38} width="18" height="24" rx="2" fill="#EDF3FF" stroke="#CAD4E2" strokeWidth="1" />
          </g>
        ))}

        {/* Security Gatehouse at Entry */}
        <rect x="270" y="380" width="60" height="40" rx="3" fill="#FFFFFF" stroke="#2855D9" strokeWidth="2" />
        <polygon points="265,380 300,365 335,380" fill="#2855D9" />
        <rect x="288" y="392" width="24" height="28" fill="#EDF3FF" stroke="#087F8C" strokeWidth="1" />
        <circle cx="300" cy="384" r="3" fill="#087F8C" />

        {/* Gate Barrier Boom */}
        <rect x="225" y="415" width="45" height="5" fill="#2855D9" rx="2" />
        <rect x="330" y="415" width="45" height="5" fill="#2855D9" rx="2" />

        {/* Trees & Vegetation */}
        {/* Tree Left */}
        <rect x="65" y="380" width="6" height="30" fill="#58677D" />
        <ellipse cx="68" cy="370" rx="20" ry="25" fill="#087F8C" opacity="0.9" />
        <ellipse cx="68" cy="365" rx="15" ry="18" fill="#2855D9" opacity="0.2" />

        {/* Tree Right */}
        <rect x="525" y="380" width="6" height="30" fill="#58677D" />
        <ellipse cx="528" cy="370" rx="22" ry="26" fill="#087F8C" opacity="0.9" />
        <ellipse cx="528" cy="365" rx="16" ry="20" fill="#2855D9" opacity="0.2" />

        {/* Small Bushes */}
        <ellipse cx="215" cy="415" rx="14" ry="10" fill="#087F8C" opacity="0.75" />
        <ellipse cx="385" cy="415" rx="14" ry="10" fill="#087F8C" opacity="0.75" />

        {/* Badge Banner */}
        <g transform="translate(185, 450)">
          <rect width="230" height="30" rx="15" fill="#FFFFFF" stroke="#E3E9F2" strokeWidth="1.5" />
          <circle cx="20" cy="15" r="5" fill="#137547" />
          <text x="35" y="19" fill="#17243C" fontSize="12" fontWeight="600" fontFamily="sans-serif">
            Community Gate Access Active
          </text>
        </g>
      </svg>
    </div>
  );
};
