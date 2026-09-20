export default function LowStockPanel({ products, loading, unavailable = false }) {
  return (
    <div className="ledger-card p-6 h-full">
      <div className="flex items-center justify-between mb-4">
        <p className="label-eyebrow">Low Stock Alerts</p>
        {products?.length > 0 && (
          <span className="text-xs font-mono bg-signal-amber-bg text-signal-amber px-2 py-0.5 rounded-sm">
            {products.length}
          </span>
        )}
      </div>

      {loading && <p className="text-sm text-ink-500">Loading…</p>}
      {unavailable && <p className="text-sm text-ink-500">Stock information is unavailable.</p>}

      {!loading && !unavailable && (!products || products.length === 0) && (
        <p className="text-sm text-ink-500">All products are above their minimum stock level.</p>
      )}

      {!loading && !unavailable && products?.length > 0 && (
        <ul className="divide-y divide-ink-100">
          {products.map((p) => (
            <li key={p.id} className="py-3 flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-ink-900">{p.name}</p>
                <p className="text-xs text-ink-500 font-mono">{p.productCode}</p>
              </div>
              <div className="text-right">
                <p className="ledger-figure text-sm text-signal-amber font-semibold">
                  {p.currentStock} {p.unit}
                </p>
                <p className="text-xs text-ink-500">min {p.minStockLevel}</p>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
