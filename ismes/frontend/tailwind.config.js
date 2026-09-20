/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        paper: {
          DEFAULT: "#EEF1EF", // cool pale sage-grey base, not warm cream
          raised: "#FFFFFF",
        },
        ink: {
          900: "#1B2430", // primary text / headers, deep navy-charcoal
          700: "#3A4454",
          500: "#697485",
          300: "#A7AFB9",
          200: "#D8DCE1",
          100: "#E9EBEE",
        },
        signal: {
          amber: "#C87F3A", // functional only: stock alerts, warnings
          "amber-bg": "#FBEEE0",
          green: "#2F7D5D", // functional only: profit, positive
          "green-bg": "#E6F1EC",
          red: "#B5473B", // functional only: expense, below-cost, danger
          "red-bg": "#F7E9E7",
        },
      },
      fontFamily: {
        display: ["'Space Grotesk'", "sans-serif"],
        body: ["'Inter'", "sans-serif"],
        mono: ["'IBM Plex Mono'", "monospace"],
      },
    },
  },
  plugins: [],
};
