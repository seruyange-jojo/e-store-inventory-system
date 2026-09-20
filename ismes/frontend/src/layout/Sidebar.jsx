import { NavLink } from "react-router-dom";

const navItems = [
  { to: "/", label: "Dashboard", icon: LedgerIcon },
  { to: "/inventory", label: "Inventory", icon: BoxIcon, pending: true },
  { to: "/suppliers", label: "Suppliers", icon: TruckIcon, pending: true },
  { to: "/sales", label: "Sales", icon: TagIcon, pending: true },
  { to: "/expenses", label: "Expenses", icon: ReceiptIcon, pending: true },
  { to: "/reports", label: "Reports", icon: ChartIcon, pending: true },
];

export default function Sidebar() {
  return (
    <aside className="w-60 shrink-0 bg-paper-raised border-r border-ink-200 flex flex-col">
      <div className="px-6 py-6 border-b border-ink-200">
        <span className="font-display font-semibold text-lg text-ink-900 tracking-tight">
          ISMES
        </span>
        <p className="label-eyebrow mt-1">Stock &amp; Sales Ledger</p>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map(({ to, label, icon: Icon, pending }) => pending ? (
          <span
            key={to}
            aria-disabled="true"
            className="flex items-center gap-3 px-3 py-2 text-sm font-medium text-ink-500"
          >
            <Icon className="w-4 h-4 shrink-0" />
            {label}
            <span className="ml-auto text-[10px] uppercase">Soon</span>
          </span>
        ) : (
          <NavLink
            key={to}
            to={to}
            end={to === "/"}
            className={({ isActive }) =>
              [
                "flex items-center gap-3 px-3 py-2 rounded-sm text-sm font-medium transition-colors",
                isActive
                  ? "bg-ink-900 text-paper"
                  : "text-ink-700 hover:bg-ink-100",
              ].join(" ")
            }
          >
            <Icon className="w-4 h-4 shrink-0" />
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

/* Minimal inline icon set — no external icon dependency needed for six glyphs. */
function LedgerIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" {...props}>
      <rect x="4" y="3" width="16" height="18" rx="1" />
      <path d="M8 8h8M8 12h8M8 16h5" />
    </svg>
  );
}
function BoxIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" {...props}>
      <path d="M21 8L12 3 3 8m18 0-9 5m9-5v9l-9 5m0-9L3 8m9 5v9M3 8v9l9 5" />
    </svg>
  );
}
function TruckIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" {...props}>
      <path d="M3 7h11v10H3zM14 11h4l3 3v3h-7z" />
      <circle cx="7.5" cy="18.5" r="1.5" />
      <circle cx="17.5" cy="18.5" r="1.5" />
    </svg>
  );
}
function TagIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" {...props}>
      <path d="M20.6 12.3 12.7 4.4a2 2 0 0 0-1.4-.6H5a2 2 0 0 0-2 2v6.3c0 .5.2 1 .6 1.4l7.9 7.9a2 2 0 0 0 2.8 0l6.3-6.3a2 2 0 0 0 0-2.8Z" />
      <circle cx="8" cy="8" r="1.2" />
    </svg>
  );
}
function ReceiptIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" {...props}>
      <path d="M6 2h12v20l-2.5-1.5L13 22l-2.5-1.5L8 22l-2-1.5V2Z" />
      <path d="M9 7h6M9 11h6M9 15h4" />
    </svg>
  );
}
function ChartIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" {...props}>
      <path d="M4 20V10M12 20V4M20 20v-7" />
    </svg>
  );
}
