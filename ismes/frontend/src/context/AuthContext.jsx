import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { apiClient } from "../api/client";
import { clearSession, onSessionCleared, readSession, saveSession } from "../lib/session";
import { loginErrorMessage } from "../lib/loginError";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => readSession()?.user ?? null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const unsubscribe = onSessionCleared(() => setUser(null));
    // Also catch a session invalidated before this subscription was installed.
    setUser(readSession()?.user ?? null);
    const syncSession = () => setUser(readSession()?.user ?? null);
    window.addEventListener("storage", syncSession);
    return () => {
      unsubscribe();
      window.removeEventListener("storage", syncSession);
    };
  }, []);

  const login = useCallback(async (username, password) => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await apiClient.post("/auth/login", { username, password });
      const loggedInUser = {
        username: data.username,
        fullName: data.fullName,
        role: data.role,
      };
      saveSession(data.token, loggedInUser);
      setUser(loggedInUser);
      return true;
    } catch (err) {
      setError(loginErrorMessage(err));
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setUser(null);
    setError(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout, error, loading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
  return ctx;
}
