import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        green: {
          50:  '#f0faf3',
          100: '#d8f3dc',
          200: '#b7e4c7',
          300: '#95d5b2',
          400: '#74c69d',
          500: '#52b788',
          600: '#3a8a62',
          700: '#2d6a4f',
          900: '#1a3d2b',
        },
        mint: {
          100: '#d4f4ee',
          200: '#b5ead7',
          400: '#80ded9',
        },
        beige: {
          50:  '#fdfaf5',
          100: '#faf4ec',
          200: '#f3e9d8',
          300: '#e8d5b7',
          500: '#c9a07a',
          700: '#a0856a',
        },
        orange: {
          400: '#f4a261',
          500: '#e76f51',
        },
      },
      borderRadius: {
        sm:  '10px',
        md:  '16px',
        lg:  '24px',
        xl:  '36px',
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        xs: '0 1px 3px rgba(52,120,80,.06)',
        sm: '0 2px 8px rgba(52,120,80,.09)',
        md: '0 4px 20px rgba(52,120,80,.13)',
        lg: '0 8px 40px rgba(52,120,80,.18)',
      },
      animation: {
        'float': 'float 4s ease-in-out infinite',
        'float-delayed': 'float 4s ease-in-out 1.5s infinite',
        'pulse-ring': 'pulseRing 2s infinite',
        'ticker': 'ticker 28s linear infinite',
        'scroll-cards': 'scrollCards 30s linear infinite',
        'fade-down': 'fadeDown .7s ease both',
        'fade-up': 'fadeUp .9s ease both',
        'float-bg': 'floatBg 8s ease-in-out infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%':       { transform: 'translateY(-8px)' },
        },
        pulseRing: {
          '0%, 100%': { boxShadow: '0 0 0 0 rgba(82,183,136,.5)' },
          '60%':       { boxShadow: '0 0 0 8px rgba(82,183,136,0)' },
        },
        ticker: {
          from: { transform: 'translateX(0)' },
          to:   { transform: 'translateX(-50%)' },
        },
        scrollCards: {
          from: { transform: 'translateX(0)' },
          to:   { transform: 'translateX(-50%)' },
        },
        fadeDown: {
          from: { opacity: '0', transform: 'translateY(-20px)' },
          to:   { opacity: '1', transform: 'translateY(0)' },
        },
        fadeUp: {
          from: { opacity: '0', transform: 'translateY(30px)' },
          to:   { opacity: '1', transform: 'translateY(0)' },
        },
        floatBg: {
          '0%, 100%': { transform: 'translate(0, 0)' },
          '50%':       { transform: 'translate(-20px, 20px)' },
        },
      },
    },
  },
  plugins: [],
} satisfies Config
