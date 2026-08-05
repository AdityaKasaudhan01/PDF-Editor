import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#1f2933",
        canvas: "#f7f8fa",
        accent: "#2563eb"
      }
    }
  },
  plugins: []
} satisfies Config;
