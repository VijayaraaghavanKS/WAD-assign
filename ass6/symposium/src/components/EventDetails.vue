<script setup>
import { computed, ref } from 'vue'
import RegistrationForm from './RegistrationForm.vue'

const props = defineProps({
  event: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['register', 'close'])

const showForm = ref(false)
const successMessage = ref('')

const availableSeats = computed(
  () => props.event.maxParticipants - props.event.registeredParticipants
)

const status = computed(() => {
  if (availableSeats.value <= 0) return 'Registration Closed'
  if (availableSeats.value <= 10) return 'Almost Full'
  return 'Registration Open'
})

const canRegister = computed(
  () => status.value !== 'Registration Closed' && availableSeats.value > 0
)

function openForm() {
  showForm.value = true
  successMessage.value = ''
}

function handleFormSubmit(studentData) {
  emit('register', { eventId: props.event.id, studentData })
  showForm.value = false
  successMessage.value = `Thank you, ${studentData.studentName}! You have successfully registered for "${props.event.name}".`
}
</script>

<template>
  <div class="event-details">
    <button class="close-btn" @click="$emit('close')">&times; Close</button>

    <h2>{{ event.name }}</h2>
    <span class="badge" :class="event.category.toLowerCase()">{{ event.category }}</span>

    <ul class="detail-list">
      <li><strong>Event ID:</strong> {{ event.id }}</li>
      <li><strong>Date:</strong> {{ event.date }}</li>
      <li><strong>Venue:</strong> {{ event.venue }}</li>
      <li><strong>Registration Fee:</strong> &#8377;{{ event.registrationFee }}</li>
      <li><strong>Maximum Participants:</strong> {{ event.maxParticipants }}</li>
      <li><strong>Registered Participants:</strong> {{ event.registeredParticipants }}</li>
      <li><strong>Available Seats:</strong> {{ availableSeats > 0 ? availableSeats : 0 }}</li>
    </ul>

    <p v-if="status === 'Registration Open'" class="status open">Registration Open</p>
    <p v-else-if="status === 'Almost Full'" class="status almost">Almost Full</p>
    <p v-else class="status closed">Registration Closed</p>

    <p v-if="successMessage" class="success">{{ successMessage }}</p>

    <button v-if="canRegister && !showForm" class="register-btn" @click="openForm">
      Register
    </button>

    <RegistrationForm
      v-if="showForm"
      @submit="handleFormSubmit"
      @cancel="showForm = false"
    />
  </div>
</template>

<style scoped>
.event-details {
  position: relative;
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  margin: 24px 32px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  border: none;
  background: none;
  font-size: 0.95rem;
  color: #555;
  cursor: pointer;
}

.badge {
  display: inline-block;
  font-size: 0.75rem;
  padding: 3px 10px;
  border-radius: 999px;
  color: #fff;
  margin-bottom: 12px;
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

.detail-list {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
}

.detail-list li {
  padding: 4px 0;
  border-bottom: 1px solid #eee;
  font-size: 0.95rem;
}

.status {
  font-weight: 600;
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

.success {
  background: #ecfdf5;
  color: #047857;
  padding: 10px 14px;
  border-radius: 6px;
  font-weight: 500;
}

.register-btn {
  padding: 10px 18px;
  border: none;
  border-radius: 6px;
  background: #2563eb;
  color: #fff;
  font-size: 0.95rem;
  cursor: pointer;
}

.register-btn:hover {
  background: #1d4ed8;
}
</style>
