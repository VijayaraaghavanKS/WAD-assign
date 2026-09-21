<script setup>
import { computed } from 'vue'

const props = defineProps({
  event: {
    type: Object,
    required: true
  }
})

defineEmits(['view-details'])

const availableSeats = computed(
  () => props.event.maxParticipants - props.event.registeredParticipants
)

const status = computed(() => {
  if (availableSeats.value <= 0) return 'Registration Closed'
  if (availableSeats.value <= 10) return 'Almost Full'
  return 'Registration Open'
})

const titleStyle = computed(() => {
  const seats = availableSeats.value
  if (seats > 20) return { fontSize: '1.6rem' }
  if (seats >= 10) return { fontSize: '1.2rem' }
  return { fontSize: '0.95rem' }
})
</script>

<template>
  <div class="event-card" :class="event.category.toLowerCase()">
    <h3 class="event-title" :style="titleStyle">{{ event.name }}</h3>

    <span class="badge" :class="event.category.toLowerCase()">
      {{ event.category }}
    </span>

    <p class="meta"><strong>Date:</strong> {{ event.date }}</p>
    <p class="meta"><strong>Venue:</strong> {{ event.venue }}</p>
    <p class="meta"><strong>Fee:</strong> &#8377;{{ event.registrationFee }}</p>
    <p class="meta">
      <strong>Seats:</strong> {{ availableSeats > 0 ? availableSeats : 0 }} / {{ event.maxParticipants }} available
    </p>

    <p v-if="status === 'Registration Open'" class="status open">Registration Open</p>
    <p v-else-if="status === 'Almost Full'" class="status almost">Almost Full</p>
    <p v-else class="status closed">Registration Closed</p>

    <button class="details-btn" @click="$emit('view-details', event.id)">
      View Details
    </button>
  </div>
</template>

<style scoped>
.event-card {
  background: #fff;
  border-radius: 10px;
  padding: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-top: 5px solid #999;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.event-card.technical {
  border-top-color: #2563eb;
}
.event-card.cultural {
  border-top-color: #db2777;
}
.event-card.workshop {
  border-top-color: #059669;
}
.event-card.competition {
  border-top-color: #d97706;
}

.event-title {
  margin: 0 0 4px;
  transition: font-size 0.2s ease;
}

.badge {
  align-self: flex-start;
  font-size: 0.75rem;
  padding: 3px 10px;
  border-radius: 999px;
  color: #fff;
  margin-bottom: 6px;
}

.badge.technical {
  background: #2563eb;
}
.badge.cultural {
  background: #db2777;
}
.badge.workshop {
  background: #059669;
}
.badge.competition {
  background: #d97706;
}

.meta {
  margin: 0;
  font-size: 0.9rem;
  color: #444;
}

.status {
  font-weight: 600;
  margin: 6px 0;
}

.status.open {
  color: #059669;
}
.status.almost {
  color: #d97706;
}
.status.closed {
  color: #dc2626;
}

.details-btn {
  margin-top: 8px;
  padding: 8px 14px;
  border: none;
  border-radius: 6px;
  background: #23264a;
  color: #fff;
  cursor: pointer;
  font-size: 0.9rem;
}

.details-btn:hover {
  background: #34386e;
}
</style>
