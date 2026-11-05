/**
 * LinkUp Backend API Configuration
 *
 * All configuration is environment-driven. Never commit secrets;
 * the real values live in .env (gitignored).
 */

// Base URL for the REST API.
// In production, point this to your deployed backend domain.
export const API_BASE_URL =
  process.env.REACT_APP_API_URL || "http://localhost:5000/api";

// WebSocket / ngrok tunnel base URL (for the Java chat service overrides).
// Provide via env var NGROK_URL; fall back to the local dev tunnel.
export const CHAT_WS_BASE_URL =
  process.env.NGROK_URL || "https://217b7332d13d.ngrok-free.app";
