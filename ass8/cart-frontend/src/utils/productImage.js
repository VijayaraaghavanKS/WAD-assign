// Maps a product's name to a real product photo based on keyword matching,
// so newly admin-added products ("Gaming Mouse", "Bluetooth Speaker", ...)
// automatically get a sensible photo without hardcoding by ID.
const images = import.meta.glob('../assets/products/*.jpg', { eager: true, import: 'default' })

const CATEGORY_KEYWORDS = [
  ['laptop', ['laptop', 'notebook', 'macbook']],
  ['headphones', ['headphone', 'headset', 'earphone', 'earbud']],
  ['keyboard', ['keyboard']],
  ['mouse', ['mouse']],
  ['phone', ['phone', 'mobile']],
  ['watch', ['watch']],
  ['shoes', ['shoe', 'sneaker']],
  ['bag', ['bag', 'backpack']],
  ['speaker', ['speaker']],
  ['camera', ['camera']],
  ['monitor', ['monitor', 'display', 'screen']],
  ['tablet', ['tablet', 'ipad']],
]

export function getProductImage(name) {
  const lower = (name || '').toLowerCase()
  for (const [category, keywords] of CATEGORY_KEYWORDS) {
    if (keywords.some((k) => lower.includes(k))) {
      return images[`../assets/products/${category}.jpg`]
    }
  }
  return images['../assets/products/generic.jpg']
}
