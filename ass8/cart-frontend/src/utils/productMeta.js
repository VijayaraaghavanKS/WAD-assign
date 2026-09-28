// Deterministic, purely-presentational product flavor (rating, review count,
// "was" price for a strikethrough discount badge) derived from the product's
// own name/id so the same product always renders the same way -- there is no
// rating field on the backend Product model, this is UI chrome only.
function hash(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) {
    h = (h * 31 + str.charCodeAt(i)) >>> 0
  }
  return h
}

export function ratingFor(key) {
  const h = hash(key || '')
  return 3.6 + (h % 15) / 10 // 3.6 .. 5.0
}

export function reviewCountFor(key) {
  const h = hash((key || '') + 'r')
  return 40 + (h % 2400)
}

export function discountPctFor(key) {
  const h = hash((key || '') + 'd')
  const pool = [0, 0, 10, 12, 15, 20, 25, 30]
  return pool[h % pool.length]
}

export function wasPriceFor(price, key) {
  const pct = discountPctFor(key)
  if (pct === 0) return null
  return Math.round(price / (1 - pct / 100))
}

// Broad shopper-facing category derived from the product name, so the
// catalog can be browsed/filtered even though the backend Product model has
// no category field of its own.
const CATEGORY_RULES = [
  ['Wearables', ['watch']],
  ['Audio', ['headphone', 'earphone', 'earbud', 'speaker']],
  ['Cameras', ['camera', 'webcam']],
  ['Fashion', ['shoe', 'sneaker', 'bag', 'backpack']],
  ['Electronics', ['laptop', 'notebook', 'keyboard', 'mouse', 'phone', 'mobile', 'monitor', 'display', 'tablet', 'ipad']],
]

export function categoryFor(name) {
  const lower = (name || '').toLowerCase()
  for (const [category, keywords] of CATEGORY_RULES) {
    if (keywords.some((k) => lower.includes(k))) return category
  }
  return 'More'
}

export const CATEGORIES = ['Electronics', 'Audio', 'Wearables', 'Fashion', 'Cameras', 'More']
