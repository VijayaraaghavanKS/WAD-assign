<template>
  <section>
    <h2>Order</h2>
    <!-- Sub-navigation for the nested /order/menu and /order/cart routes -->
    <nav class="sub-nav">
      <RouterLink to="/order/menu">Food Menu</RouterLink>
      <RouterLink to="/order/cart">Cart</RouterLink>
    </nav>

    <!-- Renders FoodMenu.vue or Cart.vue depending on the matched child route -->
    <RouterView />
  </section>
</template>

<style scoped>
.sub-nav {
  display: flex;
  gap: 16px;
  margin: 12px 0;
}

.sub-nav a {
  text-decoration: none;
  color: #2563eb;
}

.sub-nav a.router-link-active {
  font-weight: bold;
  text-decoration: underline;
}
</style>
