<script setup>
import { ref } from 'vue'
import EventList from "./components/EventList.vue"
import RegistrationForm from './components/RegistrationForm.vue'

const events = ref([
  { eventName: 'Sample Event', description: 'This is a default event.' },
  { eventName: 'Sample Event 2', description: 'This is a default event 2.' }
])

function handleRegistration(registrationData) {
  events.value.push(registrationData)
}

</script>

<template>
  <main>
    <h1>Event Registration</h1>
    <RegistrationForm @submit="handleRegistration" />
    <EventList :events="events" />
  </main>
</template>

<style scoped></style>
