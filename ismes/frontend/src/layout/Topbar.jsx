import { useAuth } from "../context/AuthContext";

export default function Topbar() {
  const { user, logout } = useAuth();

  const today = new Date().toLocaleDateString("en-UG", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });

  return (
    <header className="h-16 shrink-0 border-b border-ink-200 bg-paper-raised flex items-center justify-between px-8">
      <p className="text-sm text-ink-500">{today}</p>

      <div className="flex items-center gap-4">
        <div className="text-right">
          <p className="text-sm font-medium text-ink-900">{user?.fullName}</p>
          <p className="label-eyebrow">{user?.role}</p>
        </div>
        <div className="w-9 h-9 rounded-full bg-ink-900 text-paper flex items-center justify-center font-display text-sm font-semibold">
          {user?.fullName?.charAt(0) ?? "?"}
        </div>
        <button
          onClick={logout}
          className="text-sm font-medium text-ink-500 hover:text-signal-red transition-colors ml-2"
        >
          Sign out
        </button>
      </div>
    </header>
  );
}
