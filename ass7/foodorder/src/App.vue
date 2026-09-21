<script setup>
</script>

<template>
  <div id="app-shell">
    <nav class="main-nav">
      <RouterLink to="/">Home</RouterLink>
      <RouterLink to="/order">Order</RouterLink>
      <RouterLink to="/about">About</RouterLink>
    </nav>

    <main>
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.main-nav {
  display: flex;
  gap: 20px;
  padding: 16px 24px;
  background: #23264a;
}

.main-nav a {
  color: #fff;
  text-decoration: none;
  font-weight: 500;
}

.main-nav a.router-link-active {
  text-decoration: underline;
}

main {
  padding: 24px;
  max-width: 700px;
  margin: 0 auto;
}
</style>
