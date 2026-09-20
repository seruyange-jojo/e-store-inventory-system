import { useCallback, useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { formatCurrency } from "../lib/format";
import RecordDetailPanel from "../components/RecordDetailPanel";

const today = new Date().toISOString().slice(0, 10);
const emptyForm = { categoryId: "", description: "", amount: "", expenseDate: today };

export default function Expenses() {
  const [expenses, setExpenses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [selectedExpense, setSelectedExpense] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);

  async function openExpense(expense) {
    setSelectedExpense(expense); setDetailLoading(true); setDetailError(null);
    try { setSelectedExpense((await apiClient.get(`/expenses/${expense.id}`)).data); }
    catch { setDetailError("Could not load expense details."); }
    finally { setDetailLoading(false); }
  }

  const loadExpenses = useCallback(async () => {
    setLoading(true);
    try {
      const [{ data: expenseData }, { data: categoryData }] = await Promise.all([
        apiClient.get("/expenses"),
        apiClient.get("/expenses/categories"),
      ]);
      setExpenses(expenseData);
      setCategories(categoryData);
    } catch {
      setError("Could not load expenses.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadExpenses(); }, [loadExpenses]);

  async function saveExpense(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await apiClient.post("/expenses", { ...form, categoryId: form.categoryId ? Number(form.categoryId) : null, amount: Number(form.amount) });
      setForm(emptyForm);
      setShowForm(false);
      await loadExpenses();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Could not save expense.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4"><div><p className="label-eyebrow">Outgoings</p><h1 className="font-display text-2xl font-semibold text-ink-900 mt-2">Expenses</h1><p className="text-sm text-ink-500 mt-1">Record operating costs and keep profit reporting accurate.</p></div><button className="button-primary" type="button" onClick={() => setShowForm((value) => !value)}>{showForm ? "Close form" : "Record expense"}</button></div>
      {error && <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-4 py-3">{error}</div>}
      {selectedExpense && <RecordDetailPanel title={selectedExpense.description} loading={detailLoading} error={detailError} onClose={() => setSelectedExpense(null)}><dl className="detail-grid"><div><dt>Date</dt><dd>{selectedExpense.expenseDate}</dd></div><div><dt>Category</dt><dd>{selectedExpense.categoryName || "Uncategorised"}</dd></div><div><dt>Amount</dt><dd>{formatCurrency(selectedExpense.amount)}</dd></div></dl></RecordDetailPanel>}
      {showForm && <form className="ledger-card p-6 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4" onSubmit={saveExpense}><label className="field-label">Description<input className="field-input" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} required /></label><label className="field-label">Category<select className="field-input" value={form.categoryId} onChange={(event) => setForm({ ...form, categoryId: event.target.value })}><option value="">Uncategorised</option>{categories.map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}</select></label><label className="field-label">Amount<input className="field-input" type="number" min="0.01" step="0.01" value={form.amount} onChange={(event) => setForm({ ...form, amount: event.target.value })} required /></label><label className="field-label">Date<input className="field-input" type="date" value={form.expenseDate} onChange={(event) => setForm({ ...form, expenseDate: event.target.value })} required /></label><div className="xl:col-span-4 flex justify-end"><button className="button-primary" disabled={saving}>{saving ? "Saving..." : "Save expense"}</button></div></form>}
      <div className="ledger-card overflow-hidden"><div className="overflow-x-auto"><table className="data-table"><thead><tr><th>Date</th><th>Description</th><th>Category</th><th>Amount</th></tr></thead><tbody>{!loading && expenses.length === 0 && <tr><td colSpan="4" className="empty-cell">No expenses recorded.</td></tr>}{expenses.map((expense) => <tr key={expense.id} className="clickable-row" onClick={() => openExpense(expense)}><td>{expense.expenseDate}</td><td><strong>{expense.description}</strong></td><td>{expense.categoryName || "Uncategorised"}</td><td>{formatCurrency(expense.amount)}</td></tr>)}</tbody></table></div></div>
    </div>
  );
}