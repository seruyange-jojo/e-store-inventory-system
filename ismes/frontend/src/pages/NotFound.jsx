import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="space-y-4">
      <h1 className="font-display text-2xl font-semibold">Page unavailable</h1>
      <p className="text-sm text-ink-500">
        This page is not available. Inventory, Suppliers, Sales, Expenses and Reports
        are still being developed.
      </p>
      <Link to="/" className="inline-block text-sm font-medium underline">
        Return to dashboard
      </Link>
    </div>
  );
}
