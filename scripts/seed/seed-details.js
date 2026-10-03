// Target databases. scripts/seed-demo-data.sh replaces this line.
const DB = __DB__

// Adds the product-page details (description, sizes, colours, specs, reviews)
// to every product. Safe to re-run: it overwrites these fields only.
const products = db.getSiblingDB(DB.shop).products
const shoppers = db.getSiblingDB(DB.users).users.find({ username: /^shopper_/ }).toArray().map((u) => u.username)

let seed = 7
const rnd = () => ((seed = (seed * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff)
const pick = (arr) => arr[Math.floor(rnd() * arr.length)]
const subset = (arr, n) => [...arr].sort(() => rnd() - 0.5).slice(0, n)

const COLOURS = ['Black', 'Sand', 'Terracotta', 'Olive', 'Navy', 'Silver']
const WARRANTY = { Electronics: '1 year brand warranty', Audio: '1 year brand warranty', Wearables: '1 year brand warranty', Photography: '2 years brand warranty', Gaming: '2 years brand warranty', Fashion: '30-day easy returns', Home: '1 year brand warranty', Fitness: '6 months warranty', Stationery: '30-day easy returns' }
const DESCRIBE = {
  Electronics: 'Built for daily work: quick to set up, quiet, and sturdy enough to carry around.',
  Audio: 'Clear sound and a comfortable fit for long listening sessions.',
  Wearables: 'Tracks your day, shows notifications, and lasts a full week on one charge.',
  Photography: 'Compact body, sharp output, and simple controls for everyday shooting.',
  Gaming: 'Responsive, comfortable, and designed for long sessions at the desk.',
  Fashion: 'Made from carefully chosen materials, cut for an easy everyday fit.',
  Home: 'Simple, well-made, and designed to sit comfortably in a modern home.',
  Fitness: 'Durable and grippy, built for regular training at home or outdoors.',
  Stationery: 'Smooth, durable paper and a clean finish for notes and sketches.',
}
const SPECS = {
  Electronics: ['Connectivity', 'Power', 'Weight', 'Dimensions'],
  Audio: ['Connectivity', 'Battery life', 'Weight', 'Driver'],
  Wearables: ['Display', 'Battery life', 'Water resistance', 'Sensors'],
  Photography: ['Sensor', 'Video', 'Weight', 'Connectivity'],
  Gaming: ['Material', 'Max load', 'Weight', 'Adjustment'],
  Fashion: ['Material', 'Care', 'Origin', 'Fit'],
  Home: ['Material', 'Power', 'Dimensions', 'Finish'],
  Fitness: ['Material', 'Thickness', 'Weight', 'Grip'],
  Stationery: ['Pages', 'Paper weight', 'Binding', 'Size'],
}
const SPEC_VALUES = {
  Connectivity: ['USB-C / Bluetooth 5.3', 'Wired USB-C', 'Bluetooth 5.2'], Power: ['5 V USB charging', '100-240 V', 'Rechargeable battery'],
  Weight: ['180 g', '240 g', '1.2 kg', '520 g'], Dimensions: ['24 x 16 x 3 cm', '30 x 20 x 5 cm'], 'Battery life': ['up to 30 hours', 'up to 8 hours', 'up to 7 days'],
  Driver: ['40 mm dynamic', '10 mm dynamic'], Display: ['1.4 inch AMOLED', '1.9 inch LCD'], 'Water resistance': ['5 ATM', 'IP68'], Sensors: ['Heart rate, SpO2, accelerometer', 'Accelerometer, gyroscope'],
  Sensor: ['24 MP APS-C', '20 MP Micro Four Thirds'], Video: ['4K 30 fps', '1080p 60 fps'], Material: ['Recycled aluminium', 'Full-grain leather', 'Organic cotton', 'Natural rubber', 'Oak and steel', 'Ceramic-coated steel'],
  'Max load': ['120 kg', '150 kg'], Adjustment: ['Height and tilt', 'Full recline'], Care: ['Machine wash cold', 'Wipe with a dry cloth', 'Spot clean only'], Origin: ['India', 'Portugal', 'Vietnam'],
  Fit: ['Regular', 'Slim', 'Relaxed'], Finish: ['Matte', 'Brushed', 'Satin'], Thickness: ['6 mm', '10 mm'], Grip: ['Textured, non-slip', 'Soft-touch'],
  Pages: ['120 sheets', '200 sheets'], 'Paper weight': ['100 gsm', '120 gsm'], Binding: ['Stitched', 'Lay-flat'], Size: ['A5', 'A4'],
}
const REVIEW_BODY = [
  'Exactly as described. Setup took two minutes and it has been reliable every day.',
  'Good value for the money. Packaging was neat and delivery was on time.',
  'Solid build quality. Took a few days to get used to, but I am happy with it.',
  'Does the job well. Would buy again for the price.',
  'Looks nicer in person than in the photos. Very pleased.',
  'Honest product, nothing flashy. Works well for my daily routine.',
]
const REVIEW_TITLE = ['Works as promised', 'Happy with this', 'Good value', 'Solid pick', 'Would recommend', 'Nice quality']

let updated = 0
for (const p of products.find({}).toArray()) {
  const cat = p.category || 'Electronics'
  const colours = subset(COLOURS, 2 + Math.floor(rnd() * 3))
  const sizes = cat === 'Fashion' && /Shoes/i.test(p.name) ? ['7', '8', '9', '10', '11']
    : cat === 'Fashion' && /Jacket/i.test(p.name) ? ['S', 'M', 'L', 'XL'] : []
  const specs = { Brand: 'MaisonCart Select', Warranty: WARRANTY[cat] || '1 year warranty' }
  for (const key of SPECS[cat] || SPECS.Electronics) specs[key] = pick(SPEC_VALUES[key] || ['Standard'])

  const reviews = []
  const n = 3 + Math.floor(rnd() * 4)
  for (let i = 0; i < n; i++) {
    const rating = rnd() < 0.7 ? 4 + Math.floor(rnd() * 2) : 3
    reviews.push({
      username: pick(shoppers), rating, title: pick(REVIEW_TITLE), body: pick(REVIEW_BODY),
      postedAt: new Date(Date.now() - Math.floor(rnd() * 60) * 86400000),
    })
  }

  products.updateOne({ _id: p._id }, { $set: {
    description: `${p.name}. ${DESCRIBE[cat] || DESCRIBE.Electronics}`,
    sizes, colors: colours, specs, reviews, _class: undefined,
  } })
  updated++
}
print(`Updated product details for ${updated} products`)
