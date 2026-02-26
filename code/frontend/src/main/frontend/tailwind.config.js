/** @type {import('tailwindcss').Config} */
/*eslint-env node*/
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      screens: {
        "2xl": "1500px",
      },
      fontFamily: {
        sans: [
          "Roboto",
          "ui-sans-serif",
          "system-ui",
          "sans-serif",
          "Apple Color Emoji",
          "Segoe UI Emoji",
          "Segoe UI Symbol",
          "Noto Color Emoji",
        ],
      },
      colors: {
        "home-blue": "#2792fd",
        "prod-primary": "var(--prod-primary)",
        "prod-accent": "var(--prod-accent)",
        "prod-warn": "var(--prod-warn)",
        "confirm-green": "var(--confirm-green)",
        "confirm-green-hover": "var(--confirm-green-hover)",
        "table-row-hover": "var(--table-row-hover)",
        "iemdm-white": "var(--iemdm-white)",
        "iemdm-yellow": "var(--iemdm-yellow)",
        "iemdm-red": "var(--iemdm-red)",
        "iemdm-text-grey": "var(--iemdm-text-grey)",
        "table-row-even": "var(--table-row-even)",
        "table-row-odd": "var(--table-row-odd)",
        "disabled-action": "var(--disabled-action)",
        "disabled-button": "var(--disabled-button)",
        "disabled-text": "var(--disabled-text)",
        "enabled-action": "var(--enabled-action)",
      },
      spacing: {
        8.5: "2.125rem",
      },
    },
  },
  plugins: [],
};
