import { useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { formatCurrency, formatDateTime } from "../lib/format";

export default function Reports() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    apiClient.get("/dashboard/summary")
      .then(({ data }) => setSummary(data))
      .catch(() => setError("Could not load report data."));
  }, []);

  return (
    <div className="space-y-8">
      <div><p className="label-eyebrow">Business intelligence</p><h1 className="font-display text-2xl font-semibold text-ink-900 mt-2">Reports</h1><p className="text-sm text-ink-500 mt-1">A current view of revenue, costs, profit, and stock risk.</p></div>
      {error && <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-4 py-3">{error}</div>}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-5"><div className="ledger-card p-5"><p className="label-eyebrow">Sales today</p><strong className="ledger-figure text-2xl mt-4 block">{formatCurrency(summary?.todaySales)}</strong></div><div className="ledger-card p-5"><p className="label-eyebrow">Expenses today</p><strong className="ledger-figure text-2xl mt-4 block text-signal-red">{formatCurrency(summary?.todayExpenses)}</strong></div><div className="ledger-card p-5"><p className="label-eyebrow">Estimated profit</p><strong className="ledger-figure text-2xl mt-4 block">{formatCurrency(summary?.estimatedProfit)}</strong></div></div>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5"><div className="ledger-card overflow-hidden lg:col-span-2"><div className="p-5 border-b border-ink-200"><h2 className="font-display text-lg font-semibold">Recent activity</h2></div><div className="overflow-x-auto"><table className="data-table"><thead><tr><th>Type</th><th>Reference</th><th>Date</th><th>Amount</th></tr></thead><tbody>{summary?.recentTransactions?.length ? summary.recentTransactions.map((transaction, index) => <tr key={`${transaction.type}-${index}`}><td>{transaction.type}</td><td>{transaction.reference || "—"}</td><td>{formatDateTime(transaction.timestamp)}</td><td>{formatCurrency(transaction.amount)}</td></tr>) : <tr><td colSpan="4" className="empty-cell">No activity to report.</td></tr>}</tbody></table></div></div><div className="ledger-card overflow-hidden"><div className="p-5 border-b border-ink-200"><h2 className="font-display text-lg font-semibold">Stock risk</h2></div><div className="p-5 space-y-4">{summary?.lowStockProducts?.length ? summary.lowStockProducts.map((product) => <div className="flex justify-between gap-4 text-sm" key={product.id}><span>{product.name}</span><strong className="text-signal-red">{product.currentStock} left</strong></div>) : <p className="text-sm text-ink-500">No low-stock products.</p>}</div></div></div>
    </div>
  );
}