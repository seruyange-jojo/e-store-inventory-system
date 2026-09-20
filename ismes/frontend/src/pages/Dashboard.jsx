import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import StatCard from "../components/StatCard";
import LowStockPanel from "../components/LowStockPanel";
import RecentTransactionsTable from "../components/RecentTransactionsTable";
import { formatCurrency } from "../lib/format";

export default function Dashboard() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function loadDashboard() {
      setLoading(true);
      setLoadError(null);
      try {
        const { data } = await apiClient.get("/dashboard/summary");
        if (!cancelled) setSummary(data);
      } catch {
        if (!cancelled) {
          setLoadError("Could not load dashboard data. Is the backend running?");
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadDashboard();
    return () => {
      cancelled = true;
    };
  }, []);

  const profitTone =
    summary && summary.estimatedProfit < 0
      ? "negative"
      : summary && summary.estimatedProfit > 0
      ? "positive"
      : "neutral";

  return (
    <div className="space-y-8">
      <div>
        <h1 className="font-display text-2xl font-semibold text-ink-900">Dashboard</h1>
        <p className="text-sm text-ink-500 mt-1">Today&apos;s overview at a glance.</p>
      </div>

      {loadError && (
        <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-4 py-3">
          {loadError}
        </div>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5">
        <StatCard
          eyebrow="Today's Sales"
          value={loading || !summary ? "—" : formatCurrency(summary.todaySales)}
          tag={summary ? `${summary.todaySalesCount ?? 0} transactions` : undefined}
        />
        <StatCard
          eyebrow="Today's Expenses"
          value={loading || !summary ? "—" : formatCurrency(summary.todayExpenses)}
          tone="negative"
        />
        <StatCard
          eyebrow="Estimated Profit"
          value={loading || !summary ? "—" : formatCurrency(summary.estimatedProfit)}
          tone={profitTone}
          tag="Sales − cost of goods − expenses"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        <div className="lg:col-span-2">
          <RecentTransactionsTable
            transactions={summary?.recentTransactions}
            loading={loading}
            unavailable={Boolean(loadError)}
          />
        </div>
        <LowStockPanel
          products={summary?.lowStockProducts}
          loading={loading}
          unavailable={Boolean(loadError)}
        />
      </div>
    </div>
  );
}
