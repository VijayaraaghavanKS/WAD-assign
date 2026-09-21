<script setup>
import { ref } from 'vue'

const emit = defineEmits(['submit', 'cancel'])

const studentName = ref('')
const registerNumber = ref('')
const email = ref('')
const department = ref('')
const errorMessage = ref('')

function handleSubmit() {
  if (
    !studentName.value.trim() ||
    !registerNumber.value.trim() ||
    !email.value.trim() ||
    !department.value.trim()
  ) {
    errorMessage.value = 'Please fill in all fields before submitting.'
    return
  }

  errorMessage.value = ''
  emit('submit', {
    studentName: studentName.value,
    registerNumber: registerNumber.value,
    email: email.value,
    department: department.value
  })
}
</script>

<template>
  <form class="registration-form" @submit.prevent="handleSubmit">
    <h4>Register for this Event</h4>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <label>
      Student Name
      <input v-model="studentName" type="text" placeholder="Enter your name" />
    </label>

    <label>
      Register Number
      <input v-model="registerNumber" type="text" placeholder="Enter register number" />
    </label>

    <label>
      Email
      <input v-model="email" type="email" placeholder="Enter your email" />
    </label>

    <label>
      Department
      <input v-model="department" type="text" placeholder="Enter your department" />
    </label>

    <div class="form-actions">
      <button type="submit" class="submit-btn">Submit Registration</button>
      <button type="button" class="cancel-btn" @click="$emit('cancel')">Cancel</button>
    </div>
  </form>
</template>

<style scoped>
.registration-form {
  margin-top: 16px;
  padding: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.registration-form h4 {
  margin: 0 0 4px;
}

.registration-form label {
  display: flex;
  flex-direction: column;
  font-size: 0.85rem;
  color: #333;
  gap: 4px;
}

.registration-form input {
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.9rem;
}

.error {
  color: #dc2626;
  font-size: 0.85rem;
  margin: 0;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}

.submit-btn {
  background: #059669;
  color: #fff;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
}

.submit-btn:hover {
  background: #047857;
}

.cancel-btn {
  background: #e5e7eb;
  color: #333;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #d1d5db;
}
</style>
