import { formatCurrency, formatDateTime } from "../lib/format";

export default function RecentTransactionsTable({ transactions, loading, unavailable = false }) {
  return (
    <div className="ledger-card p-6">
      <p className="label-eyebrow mb-4">Recent Transactions</p>

      {loading && <p className="text-sm text-ink-500">Loading…</p>}
      {unavailable && <p className="text-sm text-ink-500">Transactions are unavailable.</p>}

      {!loading && !unavailable && (!transactions || transactions.length === 0) && (
        <div className="py-10 text-center">
          <p className="text-sm text-ink-500">
            No transactions yet today. Sales and expenses will appear here as they happen.
          </p>
        </div>
      )}

      {!loading && !unavailable && transactions?.length > 0 && (
        <table className="w-full text-sm">
          <thead>
            <tr className="text-left label-eyebrow border-b border-ink-200">
              <th className="pb-2 font-medium">Type</th>
              <th className="pb-2 font-medium">Reference</th>
              <th className="pb-2 font-medium">Time</th>
              <th className="pb-2 font-medium text-right">Amount</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100">
            {transactions.map((t) => (
              <tr key={`${t.type}-${t.id}`}>
                <td className="py-3">
                  <span
                    className={`text-xs font-medium px-2 py-0.5 rounded-sm ${
                      t.type === "SALE"
                        ? "bg-signal-green-bg text-signal-green"
                        : "bg-signal-red-bg text-signal-red"
                    }`}
                  >
                    {t.type === "SALE" ? "Sale" : "Expense"}
                  </span>
                </td>
                <td className="py-3 font-mono text-ink-700">{t.reference}</td>
                <td className="py-3 text-ink-500">{formatDateTime(t.timestamp)}</td>
                <td className="py-3 text-right ledger-figure font-medium text-ink-900">
                  {formatCurrency(t.amount)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
