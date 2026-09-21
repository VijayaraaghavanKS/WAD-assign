<script setup>
import { computed, reactive, ref } from 'vue'
import Header from './components/Header.vue'
import EventList from './components/EventList.vue'
import EventDetails from './components/EventDetails.vue'
import Footer from './components/Footer.vue'

// Event data lives here in App.vue and is passed down to children via props.
const events = reactive([
  {
    id: 'EV01',
    name: 'Code Sprint',
    category: 'Technical',
    date: '2026-09-10',
    venue: 'CSE Lab 1',
    registrationFee: 100,
    maxParticipants: 60,
    registeredParticipants: 15,
    status: 'Registration Open'
  },
  {
    id: 'EV02',
    name: 'Paper Presentation',
    category: 'Technical',
    date: '2026-09-10',
    venue: 'Seminar Hall A',
    registrationFee: 150,
    maxParticipants: 40,
    registeredParticipants: 32,
    status: 'Almost Full'
  },
  {
    id: 'EV03',
    name: 'Battle of Bands',
    category: 'Cultural',
    date: '2026-09-11',
    venue: 'Open Air Theatre',
    registrationFee: 50,
    maxParticipants: 30,
    registeredParticipants: 30,
    status: 'Registration Closed'
  },
  {
    id: 'EV04',
    name: 'Dance Fiesta',
    category: 'Cultural',
    date: '2026-09-11',
    venue: 'Auditorium',
    registrationFee: 50,
    maxParticipants: 50,
    registeredParticipants: 12,
    status: 'Registration Open'
  },
  {
    id: 'EV05',
    name: 'AI/ML Workshop',
    category: 'Workshop',
    date: '2026-09-12',
    venue: 'CSE Lab 2',
    registrationFee: 200,
    maxParticipants: 35,
    registeredParticipants: 27,
    status: 'Almost Full'
  },
  {
    id: 'EV06',
    name: 'Cloud Computing Workshop',
    category: 'Workshop',
    date: '2026-09-12',
    venue: 'IT Lab 1',
    registrationFee: 200,
    maxParticipants: 30,
    registeredParticipants: 8,
    status: 'Registration Open'
  },
  {
    id: 'EV07',
    name: 'Tech Quiz',
    category: 'Competition',
    date: '2026-09-10',
    venue: 'Seminar Hall B',
    registrationFee: 0,
    maxParticipants: 80,
    registeredParticipants: 45,
    status: 'Registration Open'
  },
  {
    id: 'EV08',
    name: 'Robo Race',
    category: 'Competition',
    date: '2026-09-11',
    venue: 'Ground Floor Arena',
    registrationFee: 250,
    maxParticipants: 20,
    registeredParticipants: 20,
    status: 'Registration Closed'
  }
])

const categories = ['All Events', 'Technical', 'Cultural', 'Workshop', 'Competition']
const activeCategory = ref('All Events')
const searchTerm = ref('')
const selectedEventId = ref(null)

// Computed property: filters by category and searches by event name.
const filteredEvents = computed(() => {
  return events.filter((event) => {
    const matchesCategory =
      activeCategory.value === 'All Events' || event.category === activeCategory.value
    const matchesSearch = event.name
      .toLowerCase()
      .includes(searchTerm.value.trim().toLowerCase())
    return matchesCategory && matchesSearch
  })
})

const selectedEvent = computed(
  () => events.find((event) => event.id === selectedEventId.value) || null
)

function handleSelectCategory(category) {
  activeCategory.value = category
}

function handleSearch(term) {
  searchTerm.value = term
}

function handleViewDetails(eventId) {
  selectedEventId.value = eventId
}

function handleCloseDetails() {
  selectedEventId.value = null
}

function handleRegister({ eventId }) {
  const event = events.find((e) => e.id === eventId)
  if (!event) return

  event.registeredParticipants += 1

  const availableSeats = event.maxParticipants - event.registeredParticipants
  if (availableSeats <= 0) {
    event.status = 'Registration Closed'
  } else if (availableSeats <= 10) {
    event.status = 'Almost Full'
  } else {
    event.status = 'Registration Open'
  }
}
</script>

<template>
  <div class="app-shell">
    <Header
      :categories="categories"
      :active-category="activeCategory"
      :search-term="searchTerm"
      @select-category="handleSelectCategory"
      @search="handleSearch"
    />

    <EventDetails
      v-if="selectedEvent"
      :event="selectedEvent"
      @register="handleRegister"
      @close="handleCloseDetails"
    />

    <EventList :events="filteredEvents" @view-details="handleViewDetails" />

    <Footer />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
</style>
