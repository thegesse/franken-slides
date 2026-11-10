// Set VITE_API_BASE_URL in the frontend environment to the deployed backend URL.
// The local fallback keeps local backend development convenient.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
