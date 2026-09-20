import { useCallback, useEffect, useMemo, useState } from "react";
import { apiClient } from "../api/client";
import { formatCurrency } from "../lib/format";

const emptyLine = { productId: "", quantity: "1", unitCost: "" };
const emptyForm = { supplierId: "", purchaseDate: new Date().toISOString().slice(0, 10), referenceNumber: "", notes: "" };

export default function Purchases() {
  const [products, setProducts] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [purchases, setPurchases] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [line, setLine] = useState(emptyLine);
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const loadPurchases = useCallback(async () => {
    setLoading(true);
    try {
      const [{ data: productData }, { data: supplierData }, { data: purchaseData }] = await Promise.all([
        apiClient.get("/products"),
        apiClient.get("/suppliers"),
        apiClient.get("/purchases"),
      ]);
      setProducts(productData);
      setSuppliers(supplierData);
      setPurchases(purchaseData);
    } catch {
      setError("Could not load purchase data.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadPurchases(); }, [loadPurchases]);

  const total = useMemo(() => cart.reduce((sum, item) => sum + item.unitCost * item.quantity, 0), [cart]);

  function selectProduct(event) {
    const product = products.find((item) => String(item.id) === event.target.value);
    setLine({ ...line, productId: event.target.value, unitCost: product?.buyingPrice ?? "" });
  }

  function addLine(event) {
    event.preventDefault();
    const product = products.find((item) => String(item.id) === line.productId);
    if (!product || Number(line.quantity) < 1 || Number(line.unitCost) < 0) return;
    setError(null);
    setCart([...cart, { productId: product.id, name: product.name, quantity: Number(line.quantity), unitCost: Number(line.unitCost) }]);
    setLine(emptyLine);
  }

  async function completePurchase(event) {
    event.preventDefault();
    if (!form.supplierId || cart.length === 0) return;
    setSaving(true);
    setError(null);
    try {
      const { data } = await apiClient.post("/purchases", { ...form, supplierId: Number(form.supplierId), items: cart });
      setNotice(`Purchase ${data.referenceNumber || `#${data.id}`} recorded and stock updated.`);
      setCart([]);
      setForm({ ...emptyForm, purchaseDate: new Date().toISOString().slice(0, 10) });
      await loadPurchases();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Could not complete purchase.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-8">
      <div><p className="label-eyebrow">Stock in</p><h1 className="font-display text-2xl font-semibold text-ink-900 mt-2">Purchases</h1><p className="text-sm text-ink-500 mt-1">Receive stock from suppliers and keep buying costs current.</p></div>
      {error && <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-4 py-3">{error}</div>}
      {notice && <div className="bg-signal-green-bg border border-signal-green/30 text-signal-green text-sm rounded-sm px-4 py-3">{notice}</div>}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
        <form className="ledger-card p-6 space-y-5" onSubmit={completePurchase}>
          <div className="flex justify-between items-center"><h2 className="font-display text-lg font-semibold">New purchase</h2><span className="label-eyebrow">{cart.length} lines</span></div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3"><select className="field-input" value={form.supplierId} onChange={(event) => setForm({ ...form, supplierId: event.target.value })} required><option value="">Select supplier</option>{suppliers.map((supplier) => <option value={supplier.id} key={supplier.id}>{supplier.name}</option>)}</select><input className="field-input" type="date" value={form.purchaseDate} onChange={(event) => setForm({ ...form, purchaseDate: event.target.value })} required /></div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3"><select className="field-input md:col-span-2" value={line.productId} onChange={selectProduct}><option value="">Select product</option>{products.map((product) => <option value={product.id} key={product.id}>{product.name}</option>)}</select><input className="field-input" type="number" min="1" value={line.quantity} onChange={(event) => setLine({ ...line, quantity: event.target.value })} /></div>
          <div className="flex gap-3"><input className="field-input" type="number" min="0" step="0.01" value={line.unitCost} onChange={(event) => setLine({ ...line, unitCost: event.target.value })} placeholder="Unit cost" /><button className="button-secondary" type="button" onClick={addLine}>Add line</button></div>
          <div className="border-y border-ink-200 divide-y divide-ink-200">{cart.length === 0 && <p className="py-8 text-center text-sm text-ink-500">Add products to begin a purchase.</p>}{cart.map((item, index) => <div className="py-3 flex justify-between text-sm" key={`${item.productId}-${index}`}><span>{item.name} × {item.quantity}</span><strong>{formatCurrency(item.unitCost * item.quantity)}</strong></div>)}</div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3"><input className="field-input" placeholder="Invoice or reference number" value={form.referenceNumber} onChange={(event) => setForm({ ...form, referenceNumber: event.target.value })} /><input className="field-input" placeholder="Notes" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} /></div>
          <div className="flex items-center justify-between"><strong className="text-lg">Total {formatCurrency(total)}</strong><button className="button-primary" disabled={saving || cart.length === 0}>{saving ? "Processing..." : "Record purchase"}</button></div>
        </form>
        <div className="ledger-card overflow-hidden"><div className="p-5 border-b border-ink-200"><h2 className="font-display text-lg font-semibold">Recent purchases</h2></div><div className="overflow-x-auto"><table className="data-table"><thead><tr><th>Date</th><th>Supplier</th><th>Reference</th><th>Total</th></tr></thead><tbody>{!loading && purchases.length === 0 && <tr><td colSpan="4" className="empty-cell">No purchases recorded.</td></tr>}{purchases.map((purchase) => <tr key={purchase.id}><td>{purchase.purchaseDate}</td><td>{purchase.supplierName}</td><td>{purchase.referenceNumber || "—"}</td><td>{formatCurrency(purchase.totalAmount)}</td></tr>)}</tbody></table></div></div>
      </div>
    </div>
  );
}