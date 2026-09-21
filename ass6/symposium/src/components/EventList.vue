<script setup>
import EventCard from './EventCard.vue'

defineProps({
  events: {
    type: Array,
    required: true
  }
})

defineEmits(['view-details'])
</script>

<template>
  <section class="event-list">
    <p v-if="events.length === 0" class="no-events">
      No events match your search / filter.
    </p>

    <EventCard
      v-for="event in events"
      :key="event.id"
      :event="event"
      @view-details="$emit('view-details', $event)"
    />
  </section>
</template>

<style scoped>
.event-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  padding: 24px 32px;
}

.no-events {
  grid-column: 1 / -1;
  text-align: center;
  color: #777;
  padding: 40px 0;
}
</style>
