// Product photos are open-licensed images in public/products, one per product,
// named after the product (scripts/fetch-product-images.py writes them and their
// credits, shown on the Credits page). The name is turned into a file name here.
export function getProductImage(name) {
  return `/products/${slugify(name)}.jpg`
}

function slugify(name) {
  return name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
}
