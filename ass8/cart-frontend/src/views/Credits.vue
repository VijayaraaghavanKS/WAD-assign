<script setup>
import { onMounted, ref } from 'vue'

// Photo credits for the product images. The list is written by
// scripts/fetch-product-images.py next to the photos.
const credits = ref([])
const error = ref('')

onMounted(async () => {
  try {
    const data = await fetch('/products/credits.json').then((r) => r.json())
    credits.value = Object.entries(data).map(([product, c]) => ({ product, ...c }))
  } catch (e) {
    error.value = 'Credits could not be loaded.'
  }
})
</script>

<template>
  <section class="credits">
    <h1>Photo credits</h1>
    <p class="intro">
      Product photos come from <a href="https://commons.wikimedia.org" target="_blank" rel="noopener">Wikimedia Commons</a>,
      a free library of openly licensed images. Each photo is used under the licence shown below.
    </p>
    <p v-if="error" class="error">{{ error }}</p>

    <div class="table-scroll">
      <table>
        <thead><tr><th>Product</th><th>Photo</th><th>Author</th><th>Licence</th></tr></thead>
        <tbody>
          <tr v-for="c in credits" :key="c.product">
            <td>{{ c.product }}</td>
            <td><a :href="c.page" target="_blank" rel="noopener">{{ c.title }}</a></td>
            <td>{{ c.author }}</td>
            <td><a v-if="c.licenseUrl" :href="c.licenseUrl" target="_blank" rel="noopener">{{ c.license }}</a><span v-else>{{ c.license }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
h1 { margin: 0 0 10px; font-size: 2rem; }
.intro { color: var(--ink-soft); max-width: 680px; margin-bottom: 18px; }
.intro a, td a { color: var(--link); }
.table-scroll { overflow-x: auto; background: var(--paper); border-radius: var(--radius); box-shadow: var(--shadow-card); padding: 6px 14px; }
table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
th, td { text-align: left; padding: 9px 6px; border-bottom: 1px solid var(--border); vertical-align: top; }
th { font-size: 0.74rem; text-transform: uppercase; letter-spacing: 0.06em; color: var(--ink-soft); }
.error { color: var(--danger); }
</style>
