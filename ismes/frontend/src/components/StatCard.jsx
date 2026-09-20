const toneStyles = {
  neutral: { text: "text-ink-900" },
  positive: { text: "text-signal-green" },
  negative: { text: "text-signal-red" },
  warning: { text: "text-signal-amber" },
};

export default function StatCard({ eyebrow, value, tag, tone = "neutral" }) {
  const styles = toneStyles[tone] ?? toneStyles.neutral;

  return (
    <div className="ledger-card p-6">
      <p className="label-eyebrow">{eyebrow}</p>
      <p className={`ledger-figure text-3xl font-semibold mt-3 ${styles.text}`}>{value}</p>
      {tag && <p className="text-xs text-ink-500 mt-2">{tag}</p>}
    </div>
  );
}
