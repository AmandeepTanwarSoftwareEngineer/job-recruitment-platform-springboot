import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Forward every /api/* request to Spring Boot. The browser only talks to localhost:5173,
    // so the React code can call "/api/auth/login" and no CORS setup is needed in the backend.
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});
