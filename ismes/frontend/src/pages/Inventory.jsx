import { useCallback, useEffect, useMemo, useState } from "react";
import { apiClient } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { formatCurrency, formatNumber } from "../lib/format";

const emptyForm = {
  productCode: "",
  name: "",
  categoryId: "",
  description: "",
  unit: "pcs",
  buyingPrice: "",
  sellingPrice: "",
  openingStock: "",
  minStockLevel: "5",
};

export default function Inventory() {
  const { user } = useAuth();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [search, setSearch] = useState("");
  const [showLowStock, setShowLowStock] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const isAdmin = user?.role === "ADMIN";

  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [{ data: productData }, { data: categoryData }] = await Promise.all([
        apiClient.get("/products", { params: search ? { search } : undefined }),
        apiClient.get("/categories"),
      ]);
      setProducts(productData);
      setCategories(categoryData);
    } catch {
      setError("Could not load inventory data. Please try again.");
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  const visibleProducts = useMemo(
    () => showLowStock ? products.filter((product) => product.lowStock) : products,
    [products, showLowStock],
  );

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function createProduct(event) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    setNotice(null);
    try {
      await apiClient.post("/products", {
        ...form,
        categoryId: form.categoryId ? Number(form.categoryId) : null,
        buyingPrice: Number(form.buyingPrice),
        sellingPrice: Number(form.sellingPrice),
        openingStock: Number(form.openingStock),
        minStockLevel: Number(form.minStockLevel),
      });
      setForm(emptyForm);
      setShowForm(false);
      setNotice("Product added to inventory.");
      await loadProducts();
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Could not save the product.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="label-eyebrow">Stock control</p>
          <h1 className="font-display text-2xl font-semibold text-ink-900 mt-2">Inventory</h1>
          <p className="text-sm text-ink-500 mt-1">Manage products, pricing, and reorder levels.</p>
        </div>
        {isAdmin && (
          <button className="button-primary" type="button" onClick={() => setShowForm((value) => !value)}>
            {showForm ? "Close form" : "Add product"}
          </button>
        )}
      </div>

      {error && <div className="bg-signal-red-bg border border-signal-red/30 text-signal-red text-sm rounded-sm px-4 py-3">{error}</div>}
      {notice && <div className="bg-signal-green-bg border border-signal-green/30 text-signal-green text-sm rounded-sm px-4 py-3">{notice}</div>}

      {showForm && isAdmin && (
        <form className="ledger-card p-6 grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4" onSubmit={createProduct}>
          <label className="field-label">Code<input className="field-input" name="productCode" value={form.productCode} onChange={updateField} required /></label>
          <label className="field-label">Name<input className="field-input" name="name" value={form.name} onChange={updateField} required /></label>
          <label className="field-label">Category<select className="field-input" name="categoryId" value={form.categoryId} onChange={updateField}><option value="">Uncategorised</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>
          <label className="field-label">Unit<input className="field-input" name="unit" value={form.unit} onChange={updateField} required /></label>
          <label className="field-label">Buying price<input className="field-input" type="number" min="0" step="0.01" name="buyingPrice" value={form.buyingPrice} onChange={updateField} required /></label>
          <label className="field-label">Selling price<input className="field-input" type="number" min="0" step="0.01" name="sellingPrice" value={form.sellingPrice} onChange={updateField} required /></label>
          <label className="field-label">Opening stock<input className="field-input" type="number" min="0" name="openingStock" value={form.openingStock} onChange={updateField} required /></label>
          <label className="field-label">Minimum stock<input className="field-input" type="number" min="0" name="minStockLevel" value={form.minStockLevel} onChange={updateField} required /></label>
          <label className="field-label md:col-span-2 xl:col-span-4">Description<textarea className="field-input min-h-20" name="description" value={form.description} onChange={updateField} /></label>
          <div className="md:col-span-2 xl:col-span-4 flex justify-end"><button className="button-primary" disabled={saving}>{saving ? "Saving..." : "Save product"}</button></div>
        </form>
      )}

      <div className="ledger-card overflow-hidden">
        <div className="p-4 border-b border-ink-200 flex flex-wrap gap-3 justify-between">
          <input className="field-input max-w-sm" placeholder="Search by code or name" value={search} onChange={(event) => setSearch(event.target.value)} />
          <button className={showLowStock ? "button-secondary active" : "button-secondary"} type="button" onClick={() => setShowLowStock((value) => !value)}>
            {showLowStock ? "Showing low stock" : "Show low stock"}
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="data-table">
            <thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th></tr></thead>
            <tbody>
              {!loading && visibleProducts.length === 0 && <tr><td colSpan="5" className="empty-cell">No products match this view.</td></tr>}
              {visibleProducts.map((product) => (
                <tr key={product.id}>
                  <td><strong>{product.name}</strong><span>{product.productCode}</span></td>
                  <td>{product.categoryName || "Uncategorised"}</td>
                  <td>{formatCurrency(product.sellingPrice)}</td>
                  <td><strong>{formatNumber(product.currentStock)}</strong> {product.unit}</td>
                  <td><span className={product.lowStock ? "status-badge danger" : "status-badge"}>{product.lowStock ? "Low stock" : "In stock"}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}