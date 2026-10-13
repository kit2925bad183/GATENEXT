import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        background: '#FFFFFF',
        surface: '#F7F9FC',
        primary: {
          DEFAULT: '#2855D9',
          hover: '#1D42B0',
          light: '#EDF3FF',
        },
        teal: {
          DEFAULT: '#087F8C',
          light: '#E6F4F6',
          dark: '#06636E',
        },
        accent: {
          blue: '#EDF3FF',
          lavender: '#F1EEFF',
        },
        text: {
          main: '#17243C',
          secondary: '#58677D',
          muted: '#8A97A8',
        },
        border: {
          DEFAULT: '#E3E9F2',
          light: '#EEF2F7',
          dark: '#CAD4E2',
        },
        status: {
          pending: {
            bg: '#FFF8E6',
            text: '#995B00',
            border: '#FFE3A3',
          },
          approved: {
            bg: '#EBF9F1',
            text: '#137547',
            border: '#B6E8CA',
          },
          inside: {
            bg: '#EDF3FF',
            text: '#1D42B0',
            border: '#C2D6FF',
          },
          exited: {
            bg: '#F1F4F8',
            text: '#58677D',
            border: '#D5DFEC',
          },
          rejected: {
            bg: '#FDF0ED',
            text: '#C02B10',
            border: '#F9CECB',
          },
          cancelled: {
            bg: '#F5F5F7',
            text: '#687385',
            border: '#E1E4EA',
          },
          expired: {
            bg: '#FFF0F0',
            text: '#B31B1B',
            border: '#F8C8C8',
          },
        },
      },
      fontFamily: {
        sans: ['Inter', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
      },
      boxShadow: {
        card: '0 1px 3px 0 rgba(23, 36, 60, 0.05), 0 1px 2px -1px rgba(23, 36, 60, 0.05)',
        'card-hover': '0 4px 12px 0 rgba(23, 36, 60, 0.08), 0 2px 4px -2px rgba(23, 36, 60, 0.04)',
        dropdown: '0 10px 25px -5px rgba(23, 36, 60, 0.1), 0 8px 10px -6px rgba(23, 36, 60, 0.06)',
      },
    },
  },
  plugins: [],
} satisfies Config;
