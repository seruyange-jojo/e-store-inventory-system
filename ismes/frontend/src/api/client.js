import axios from "axios";
import { clearSession, readSession } from "../lib/session.js";

const baseURL = import.meta.env?.VITE_API_BASE_URL || "http://localhost:8080/api";

export const apiClient = axios.create({ baseURL });

apiClient.interceptors.request.use((config) => {
  if (config.url === "/auth/login") {
    config.headers.delete("Authorization");
  } else {
    const session = readSession();
    if (session) config.headers.Authorization = `Bearer ${session.token}`;
  }
  return config;
});

// The auth provider subscribes to session clearing and updates route guards.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearSession();
    }
    return Promise.reject(error);
  }
);
