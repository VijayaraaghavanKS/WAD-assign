<script setup>
const year = new Date().getFullYear()
</script>

<template>
  <footer class="site-footer">
    <p>&copy; {{ year }} TechNova Symposium - College Symposium Management System</p>
  </footer>
</template>

<style scoped>
.site-footer {
  margin-top: auto;
  background: #23264a;
  color: #c9cbe8;
  text-align: center;
  padding: 16px;
  font-size: 0.85rem;
}
</style>
