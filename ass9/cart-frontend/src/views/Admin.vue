<script setup>
import { onMounted, reactive, ref } from 'vue'
import { PackagePlus, Pencil, Trash2, Check, X, LayoutGrid, Boxes } from 'lucide-vue-next'
import { api } from '../api/client'
import { getProductImage } from '../utils/productImage'

const products = ref([])
const newProduct = reactive({ name: '', price: null, quantity: null })
const editingId = ref(null)
const editDraft = reactive({ name: '', price: null, quantity: null })

async function refresh() {
  products.value = await api.getProducts()
}

async function addProduct() {
  if (!newProduct.name || newProduct.price == null) return
  await api.createProduct({
    name: newProduct.name,
    price: Number(newProduct.price),
    quantity: newProduct.quantity == null || newProduct.quantity === '' ? 0 : Number(newProduct.quantity),
  })
  newProduct.name = ''
  newProduct.price = null
  newProduct.quantity = null
  await refresh()
}

function startEdit(p) {
  editingId.value = p.id
  editDraft.name = p.name
  editDraft.price = p.price
  editDraft.quantity = p.quantity
}

async function saveEdit(id) {
  await api.updateProduct(id, {
    name: editDraft.name,
    price: Number(editDraft.price),
    quantity: Number(editDraft.quantity),
  })
  editingId.value = null
  await refresh()
}

async function remove(id) {
  await api.deleteProduct(id)
  await refresh()
}

function stockClass(qty) {
  if (qty === undefined || qty === null) return ''
  if (qty === 0) return 'pill-danger'
  if (qty <= 5) return 'pill-warn'
  return 'pill-success'
}

function stockLabel(qty) {
  if (qty === undefined || qty === null) return '—'
  if (qty === 0) return 'Out of stock'
  return `${qty} in stock`
}

onMounted(refresh)
</script>

<template>
  <div class="admin">
    <div class="admin-head">
      <h1><LayoutGrid :size="22" /> Manage Inventory</h1>
      <span class="subtitle">Seller dashboard &mdash; add, edit, and remove catalog listings</span>
    </div>

    <div class="stat-strip">
      <div class="stat">
        <Boxes :size="18" />
        <div>
          <div class="stat-value">{{ products.length }}</div>
          <div class="stat-label">Active listings</div>
        </div>
      </div>
    </div>

    <form class="add-form" @submit.prevent="addProduct">
      <input v-model="newProduct.name" placeholder="Product name" required />
      <input v-model="newProduct.price" type="number" min="0" placeholder="Price (₹)" required />
      <input v-model="newProduct.quantity" type="number" min="0" placeholder="Stock quantity" />
      <button type="submit" class="btn btn-buy"><PackagePlus :size="16" /> Add Product</button>
    </form>

    <div class="table-card">
      <table>
        <thead>
          <tr>
            <th></th>
            <th>Product</th>
            <th>Price</th>
            <th>Stock</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in products" :key="p.id">
            <td><img :src="getProductImage(p.name)" :alt="p.name" class="thumb" /></td>
            <template v-if="editingId === p.id">
              <td><input v-model="editDraft.name" /></td>
              <td><input v-model="editDraft.price" type="number" min="0" /></td>
              <td><input v-model="editDraft.quantity" type="number" min="0" /></td>
              <td class="actions">
                <button class="btn btn-outline" @click="saveEdit(p.id)"><Check :size="15" /> Save</button>
                <button class="btn btn-outline" @click="editingId = null"><X :size="15" /></button>
              </td>
            </template>
            <template v-else>
              <td class="pname">{{ p.name }}</td>
              <td class="price-cell">₹{{ p.price.toLocaleString() }}</td>
              <td><span class="pill" :class="stockClass(p.quantity)">{{ stockLabel(p.quantity) }}</span></td>
              <td class="actions">
                <button class="btn btn-outline" @click="startEdit(p)"><Pencil :size="14" /> Edit</button>
                <button class="btn btn-danger-outline" @click="remove(p.id)"><Trash2 :size="14" /> Delete</button>
              </td>
            </template>
          </tr>
          <tr v-if="products.length === 0">
            <td colspan="5" class="empty-row">No products yet. Add your first listing above.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.admin-head {
  margin-bottom: 16px;
}

.admin-head h1 {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 0 0 4px;
  font-size: 1.35rem;
}

.subtitle {
  color: var(--ink-soft);
  font-size: 0.9rem;
}

.stat-strip {
  display: flex;
  gap: 14px;
  margin-bottom: 18px;
}

.stat {
  background: var(--paper);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 14px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--navy);
}

.stat-value {
  font-size: 1.3rem;
  font-weight: 800;
  line-height: 1.1;
}

.stat-label {
  font-size: 0.78rem;
  color: var(--ink-soft);
}

.add-form {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  background: var(--paper);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  padding: 14px;
}

.add-form input {
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 0.92rem;
}

.add-form input:focus {
  outline: none;
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(255, 153, 0, 0.25);
}

.add-form input[placeholder='Product name'] {
  flex: 1;
}

.table-card {
  background: var(--paper);
  border-radius: var(--radius);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 12px 14px;
  background: #f7f8f8;
  color: var(--ink-soft);
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  border-bottom: 1px solid var(--border);
}

td {
  text-align: left;
  padding: 12px 14px;
  border-bottom: 1px solid var(--border);
  vertical-align: middle;
}

tbody tr:hover {
  background: #fafbfb;
}

tbody tr:last-child td {
  border-bottom: none;
}

.thumb {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  display: block;
}

.pname {
  font-weight: 600;
}

.price-cell {
  font-weight: 700;
  color: var(--price);
}

td input {
  padding: 7px 9px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  width: 100%;
}

.pill-warn {
  background: #fff3cd;
  color: #92650a;
}

.actions {
  text-align: right;
  white-space: nowrap;
}

.actions .btn {
  padding: 6px 12px;
  font-size: 0.82rem;
  margin-left: 6px;
  border-radius: var(--radius-sm);
}

.empty-row {
  text-align: center;
  color: var(--ink-soft);
  padding: 32px 0;
}
</style>
