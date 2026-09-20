import { useCallback, useEffect, useMemo, useState } from "react";
import { apiClient } from "../api/client";
import { formatCurrency, formatDateTime } from "../lib/format";
import RecordDetailPanel from "../components/RecordDetailPanel";

const emptyLine = { productId: "", quantity: "1", unitPrice: "" };

export default function Sales() {
  const [products, setProducts] = useState([]);
  const [sales, setSales] = useState([]);
  const [line, setLine] = useState(emptyLine);
  const [cart, setCart] = useState([]);
  const [customerName, setCustomerName] = useState("");
  const [paymentMethod, setPaymentMethod] = useState("CASH");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [selectedSale, setSelectedSale] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState(null);

  async function openSale(sale) {
    setSelectedSale(sale); setDetailLoading(true); setDetailError(null);
    try { setSelectedSale((await apiClient.get(`/sales/${sale.id}`)).data); }
    catch { setDetailError("Could not load sale details."); }
    finally { setDetailLoading(false); }
  }

  const loadSales = useCallback(async () => {
    setLoading(true);
    try {
      const [{ data: productData }, { data: saleData }] = await Promise.all([
        apiClient.get("/products"),
        apiClient.get("/sales"),
      ]);
      setProducts(productData);
      setSales(saleData);
    } catch {
      setError("Could not load sales data.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadSales(); }, [loadSales]);

  const total = useMemo(() => cart.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0), [cart]);

  function selectProduct(event) {
    const product = products.find((item) => String(item.id) === event.target.value);
    setLine({ ...line, productId: event.target.value, unitPrice: product?.sellingPrice ?? "" });
  }

  function addLine(event) {
    event.preventDefault();
    const product = products.find((item) => String(item.id) === line.productId);
    if (!product || Number(line.quantity) < 1) return;
    if (Number(line.quantity) > product.currentStock) {
      setError(`Only ${product.currentStock} ${product.unit} available for ${product.name}.`);
      return;
    }
    setError(null);
    setCart([...cart, { productId: product.id, name: product.name, quantity: Number(line.quantity), unitPrice: Number(line.unitPrice) }]);
    setLine(emptyLine);
  }

  async function completeSale(event) {
    event.preventDefault();
    if (cart.length === 0) return;
    setSaving(true);
    setError(null);
    try {
      const { data } = await apiClient.post("/sales", { customerName, paymentMethod, items: cart });
      setNotice(`Sale ${data.receiptNumber} recorded.`);
      setCart([]);
      setCustomerName("");
      await loadSales();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Could not complete sale.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-8">
      <div><p className="label-eyebrow">Stock out</p><h1 className="font-display text-2xl font-semibold text-ink-900 mt-2">Sales</h1><p className="text-sm text-ink-500 mt-1">Create receipts while keeping inventory accurate.</p></div>
      {error && <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-4 py-3">{error}</div>}
      {notice && <div className="bg-signal-green-bg border border-signal-green/30 text-signal-green text-sm rounded-sm px-4 py-3">{notice}</div>}
      {selectedSale && <RecordDetailPanel title={selectedSale.receiptNumber} loading={detailLoading} error={detailError} onClose={() => setSelectedSale(null)}><dl className="detail-grid"><div><dt>Customer</dt><dd>{selectedSale.customerName || "Walk-in customer"}</dd></div><div><dt>Date</dt><dd>{formatDateTime(selectedSale.saleDate)}</dd></div><div><dt>Payment</dt><dd>{selectedSale.paymentMethod}</dd></div><div><dt>Total</dt><dd>{formatCurrency(selectedSale.totalAmount)}</dd></div></dl><div><p className="label-eyebrow mb-2">Items sold</p>{selectedSale.items?.map((item) => <div className="flex justify-between border-t border-ink-200 py-2 text-sm" key={item.productId}><span>{item.productName} × {item.quantity}</span><strong>{formatCurrency(item.subtotal)}</strong></div>)}</div></RecordDetailPanel>}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-5">
        <form className="ledger-card p-6 space-y-5" onSubmit={completeSale}>
          <div className="flex justify-between items-center"><h2 className="font-display text-lg font-semibold">New sale</h2><span className="label-eyebrow">{cart.length} lines</span></div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3"><select className="field-input md:col-span-2" value={line.productId} onChange={selectProduct}><option value="">Select product</option>{products.filter((product) => product.currentStock > 0).map((product) => <option value={product.id} key={product.id}>{product.name} · {product.currentStock} available</option>)}</select><input className="field-input" type="number" min="1" value={line.quantity} onChange={(event) => setLine({ ...line, quantity: event.target.value })} /></div>
          <div className="flex gap-3"><input className="field-input" type="number" min="0" step="0.01" value={line.unitPrice} onChange={(event) => setLine({ ...line, unitPrice: event.target.value })} placeholder="Unit price" /><button className="button-secondary" type="button" onClick={addLine}>Add line</button></div>
          <div className="border-y border-ink-200 divide-y divide-ink-200">{cart.length === 0 && <p className="py-8 text-center text-sm text-ink-500">Add products to begin a receipt.</p>}{cart.map((item, index) => <div className="py-3 flex justify-between text-sm" key={`${item.productId}-${index}`}><span>{item.name} × {item.quantity}</span><strong>{formatCurrency(item.unitPrice * item.quantity)}</strong></div>)}</div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3"><input className="field-input" placeholder="Customer name (optional)" value={customerName} onChange={(event) => setCustomerName(event.target.value)} /><select className="field-input" value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}><option value="CASH">Cash</option><option value="MOBILE_MONEY">Mobile money</option><option value="BANK">Bank</option><option value="OTHER">Other</option></select></div>
          <div className="flex items-center justify-between"><strong className="text-lg">Total {formatCurrency(total)}</strong><button className="button-primary" disabled={saving || cart.length === 0}>{saving ? "Processing..." : "Complete sale"}</button></div>
        </form>
        <div className="ledger-card overflow-hidden"><div className="p-5 border-b border-ink-200"><h2 className="font-display text-lg font-semibold">Recent receipts</h2></div><div className="overflow-x-auto"><table className="data-table"><thead><tr><th>Receipt</th><th>Date</th><th>Payment</th><th>Total</th></tr></thead><tbody>{!loading && sales.length === 0 && <tr><td colSpan="4" className="empty-cell">No sales recorded.</td></tr>}{sales.map((sale) => <tr key={sale.id} className="clickable-row" onClick={() => openSale(sale)}><td><strong>{sale.receiptNumber}</strong><span>{sale.customerName || "Walk-in customer"}</span></td><td>{formatDateTime(sale.saleDate)}</td><td>{sale.paymentMethod}</td><td>{formatCurrency(sale.totalAmount)}</td></tr>)}</tbody></table></div></div>
      </div>
    </div>
  );
}