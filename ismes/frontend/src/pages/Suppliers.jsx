import { useCallback, useEffect, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../context/AuthContext";

const emptyForm = { name: "", contactPerson: "", phone: "", email: "", address: "" };

export default function Suppliers() {
  const { user } = useAuth();
  const [suppliers, setSuppliers] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const loadSuppliers = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await apiClient.get("/suppliers");
      setSuppliers(data);
    } catch {
      setError("Could not load suppliers.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadSuppliers(); }, [loadSuppliers]);

  async function saveSupplier(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await apiClient.post("/suppliers", form);
      setForm(emptyForm);
      setShowForm(false);
      setNotice("Supplier added.");
      await loadSuppliers();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Could not save supplier.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div><p className="label-eyebrow">Procurement</p><h1 className="font-display text-2xl font-semibold text-ink-900 mt-2">Suppliers</h1><p className="text-sm text-ink-500 mt-1">Keep supplier contacts ready for purchase records.</p></div>
        {user?.role === "ADMIN" && <button className="button-primary" type="button" onClick={() => setShowForm((value) => !value)}>{showForm ? "Close form" : "Add supplier"}</button>}
      </div>
      {error && <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-4 py-3">{error}</div>}
      {notice && <div className="bg-signal-green-bg border border-signal-green/30 text-signal-green text-sm rounded-sm px-4 py-3">{notice}</div>}
      {showForm && <form className="ledger-card p-6 grid grid-cols-1 md:grid-cols-2 gap-4" onSubmit={saveSupplier}>
        <label className="field-label">Company name<input className="field-input" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required /></label>
        <label className="field-label">Contact person<input className="field-input" value={form.contactPerson} onChange={(event) => setForm({ ...form, contactPerson: event.target.value })} /></label>
        <label className="field-label">Phone<input className="field-input" value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} /></label>
        <label className="field-label">Email<input className="field-input" type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
        <label className="field-label md:col-span-2">Address<textarea className="field-input min-h-20" value={form.address} onChange={(event) => setForm({ ...form, address: event.target.value })} /></label>
        <div className="md:col-span-2 flex justify-end"><button className="button-primary" disabled={saving}>{saving ? "Saving..." : "Save supplier"}</button></div>
      </form>}
      <div className="ledger-card overflow-hidden"><div className="overflow-x-auto"><table className="data-table"><thead><tr><th>Supplier</th><th>Contact</th><th>Phone</th><th>Email</th><th>Address</th></tr></thead><tbody>
        {!loading && suppliers.length === 0 && <tr><td colSpan="5" className="empty-cell">No suppliers have been added.</td></tr>}
        {suppliers.map((supplier) => <tr key={supplier.id}><td><strong>{supplier.name}</strong></td><td>{supplier.contactPerson || "—"}</td><td>{supplier.phone || "—"}</td><td>{supplier.email || "—"}</td><td>{supplier.address || "—"}</td></tr>)}
      </tbody></table></div></div>
    </div>
  );
}