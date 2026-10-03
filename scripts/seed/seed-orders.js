// Target databases. scripts/seed-demo-data.sh replaces this line.
const DB = __DB__

// Dummy sales history for the admin dashboard and the shopper order pages.
// Writes straight into the orders database so each order can carry a past date
// (the API always stamps "now"). Re-running replaces only seeded: true orders.
const orders = db.getSiblingDB(DB.orders).orders
const products = db.getSiblingDB(DB.shop).products.find({}).toArray()
const shoppers = db.getSiblingDB(DB.users).users.find({ username: /^shopper_/ }).toArray()

orders.deleteMany({ seeded: true })

let seed = 42
const rnd = () => ((seed = (seed * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff)
const pick = (arr) => arr[Math.floor(rnd() * arr.length)]
const COUPONS = { SAVE10: 10, WELCOME20: 20 }
const PAYMENTS = ['UPI', 'UPI', 'CARD', 'COD']
const CITIES = [
  ['Chennai', 'Tamil Nadu', '600028'], ['Bengaluru', 'Karnataka', '560034'], ['Mumbai', 'Maharashtra', '400053'],
  ['Hyderabad', 'Telangana', '500081'], ['Pune', 'Maharashtra', '411014'], ['Kochi', 'Kerala', '682017'],
  ['Delhi', 'Delhi', '110019'], ['Coimbatore', 'Tamil Nadu', '641011'],
]
const STREETS = ['12 Lake View Road', '48 Anna Nagar Main Street', '7 MG Road Apartments', '221 Palm Grove Lane', '36 Temple Street', '5 Park Avenue Flats']
const addressFor = (user, i) => {
  const [city, state, pin] = CITIES[i % CITIES.length]
  return { name: user.username.replace('shopper_', '').replace(/^./, (c) => c.toUpperCase()) + ' Kumar',
    phone: '98' + String(10000000 + i * 1234567).slice(0, 8), line1: STREETS[i % STREETS.length], city, state, pin }
}
const addresses = new Map(shoppers.map((u, i) => [u.username, addressFor(u, i)]))

const now = Date.now()
const docs = []
for (let daysAgo = 59; daysAgo >= 0; daysAgo--) {
  const busy = daysAgo < 14 ? 4 : 2
  const count = 1 + Math.floor(rnd() * busy)
  for (let k = 0; k < count; k++) {
    const user = pick(shoppers)
    const lineCount = 1 + Math.floor(rnd() * 3)
    const chosen = new Set()
    while (chosen.size < lineCount) chosen.add(pick(products))
    const items = [...chosen].map((p) => ({
      productId: p._id.toString(), productName: p.name,
      price: Math.round(p.price * (100 - (p.discountPercent || 0))) / 100,
      quantity: 1 + Math.floor(rnd() * 2),
    }))
    const subtotal = Math.round(items.reduce((s, l) => s + l.price * l.quantity, 0) * 100) / 100
    const code = rnd() < 0.2 ? Object.keys(COUPONS)[Math.floor(rnd() * 2)] : null
    const discount = code ? Math.round(subtotal * COUPONS[code]) / 100 : 0
    const afterDiscount = subtotal - discount
    const deliveryFee = afterDiscount >= 5000 ? 0 : 99
    const total = Math.round((afterDiscount + deliveryFee) * 100) / 100
    const placedAt = new Date(now - daysAgo * 86400000 - Math.floor(rnd() * 12) * 3600000)
    const status = daysAgo >= 5 ? 'DELIVERED' : daysAgo >= 2 ? 'SHIPPED' : daysAgo === 1 ? 'PACKED' : pick(['PLACED', 'PACKED'])
    docs.push({
      userId: user._id.toString(), username: user.username, items,
      subtotal, discount, couponCode: code, deliveryFee, tax: Math.round(total * 18 / 118 * 100) / 100, total,
      status, placedAt, shippingAddress: addresses.get(user.username), paymentMethod: pick(PAYMENTS),
      invoiceNumber: 'INV-' + String(10000000 + docs.length * 137).slice(-8), seeded: true,
    })
  }
}
orders.insertMany(docs)
print(`Inserted ${docs.length} seeded orders for ${shoppers.length} shoppers`)
