<script setup>
defineProps({
  categories: {
    type: Array,
    required: true
  },
  activeCategory: {
    type: String,
    required: true
  },
  searchTerm: {
    type: String,
    required: true
  }
})

defineEmits(['select-category', 'search'])
</script>

<template>
  <header class="site-header">
    <div class="brand">
      <h1>TechNova Symposium</h1>
      <p>College Symposium Management System</p>
    </div>

    <div class="controls">
      <input
        class="search-box"
        type="text"
        placeholder="Search events by name..."
        :value="searchTerm"
        @input="$emit('search', $event.target.value)"
      />

      <select
        class="category-filter"
        :value="activeCategory"
        @change="$emit('select-category', $event.target.value)"
      >
        <option v-for="cat in categories" :key="cat" :value="cat">
          {{ cat }}
        </option>
      </select>
    </div>
  </header>
</template>

<!-- Normal (non-scoped) CSS, applies globally as required for Header.vue -->
<style>
.site-header {
  background: #23264a;
  color: #fff;
  padding: 20px 32px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.site-header .brand h1 {
  margin: 0;
  font-size: 1.6rem;
}

.site-header .brand p {
  margin: 4px 0 0;
  color: #c9cbe8;
  font-size: 0.9rem;
}

.site-header .controls {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.site-header .search-box,
.site-header .category-filter {
  padding: 8px 12px;
  border-radius: 6px;
  border: none;
  font-size: 0.95rem;
}

.site-header .search-box {
  min-width: 220px;
}
</style>
