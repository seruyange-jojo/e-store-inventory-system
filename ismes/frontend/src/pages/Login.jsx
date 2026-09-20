import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { user, login, error, loading } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  if (user) return <Navigate to="/" replace />;

  async function handleSubmit(e) {
    e.preventDefault();
    const success = await login(username, password);
    if (success) navigate("/");
  }

  return (
    <div className="min-h-screen bg-paper flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <span className="font-display font-semibold text-2xl text-ink-900 tracking-tight">
            ISMES
          </span>
          <p className="label-eyebrow mt-2">Inventory · Sales · Expense Ledger</p>
        </div>

        <form onSubmit={handleSubmit} className="ledger-card p-8 space-y-5">
          <div>
            <label htmlFor="username" className="label-eyebrow block mb-1.5">
              Username
            </label>
            <input
              id="username"
              type="text"
              autoComplete="username"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full border border-ink-200 rounded-sm px-3 py-2 text-sm text-ink-900 bg-paper-raised focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink-900"
              placeholder="admin"
            />
          </div>

          <div>
            <label htmlFor="password" className="label-eyebrow block mb-1.5">
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border border-ink-200 rounded-sm px-3 py-2 text-sm text-ink-900 bg-paper-raised focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ink-900"
              placeholder="••••••••"
            />
          </div>

          {error && (
            <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-3 py-2">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-ink-900 text-paper font-medium text-sm rounded-sm py-2.5 hover:bg-ink-700 transition-colors disabled:opacity-60"
          >
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>

        <p className="text-center text-xs text-ink-500 mt-6">
          Access is restricted to authorized shop staff.
        </p>
      </div>
    </div>
  );
}
