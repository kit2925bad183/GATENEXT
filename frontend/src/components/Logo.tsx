import React from 'react';

interface LogoProps {
  className?: string;
  showText?: boolean;
  size?: 'sm' | 'md' | 'lg';
}

export const Logo: React.FC<LogoProps> = ({
  className = '',
  showText = true,
  size = 'md',
}) => {
  const iconSizes = {
    sm: 'w-7 h-7',
    md: 'w-9 h-9',
    lg: 'w-12 h-12',
  };

  const textSizes = {
    sm: 'text-lg',
    md: 'text-xl',
    lg: 'text-2xl',
  };

  return (
    <div className={`flex items-center gap-2.5 select-none ${className}`}>
      {/* Custom Original SVG Logo: Roof + Archway Door + Stylized "G" */}
      <div className={`${iconSizes[size]} relative flex items-center justify-center flex-shrink-0`}>
        <svg
          viewBox="0 0 48 48"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          className="w-full h-full"
        >
          {/* Outer Protective Hexagonal Shield / Roof Line */}
          <path
            d="M24 4L42 14V34L24 44L6 34V14L24 4Z"
            fill="#EDF3FF"
            stroke="#2855D9"
            strokeWidth="2.5"
            strokeLinejoin="round"
          />

          {/* Roof Gabled Peak */}
          <path
            d="M13 19L24 11L35 19"
            stroke="#2855D9"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Doorway Archway forming stylized capital 'G' */}
          <path
            d="M32 23.5V20C32 15.5817 28.4183 12 24 12C19.5817 12 16 15.5817 16 20V28C16 32.4183 19.5817 36 24 36C28.4183 36 32 32.4183 32 28V26H24"
            stroke="#087F8C"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          />

          {/* Security Keystone Dot */}
          <circle cx="24" cy="22" r="2.5" fill="#2855D9" />
        </svg>
      </div>

      {showText && (
        <div className="flex flex-col">
          <div className={`font-bold tracking-tight text-text-main flex items-center ${textSizes[size]}`}>
            <span>Gate</span>
            <span className="text-primary font-black">Nest</span>
          </div>
          <span className="text-[10px] font-medium tracking-wider uppercase text-text-secondary -mt-1">
            Visitor Management
          </span>
        </div>
      )}
    </div>
  );
};
