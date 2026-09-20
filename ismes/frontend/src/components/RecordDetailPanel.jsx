export default function RecordDetailPanel({ title, loading, error, onClose, children }) {
  return (
    <div className="detail-panel" role="dialog" aria-modal="true" aria-label={`${title} details`}>
      <div className="detail-panel-header">
        <div><p className="label-eyebrow">Record details</p><h2 className="font-display text-xl font-semibold text-ink-900 mt-1">{title}</h2></div>
        <button className="button-secondary" type="button" onClick={onClose}>Close</button>
      </div>
      {loading && <p className="py-8 text-sm text-ink-500">Loading details...</p>}
      {error && <p className="py-8 text-sm text-signal-red">{error}</p>}
      {!loading && !error && <div className="detail-panel-content">{children}</div>}
    </div>
  );
}