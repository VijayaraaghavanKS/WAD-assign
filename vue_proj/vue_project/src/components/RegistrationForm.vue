<script setup>
import { ref } from 'vue'

const emit = defineEmits(['submit'])

const eventName = ref('')

const description = ref('')

const errorMessage = ref('')

function handleSubmit(){
    if(
        !eventName.value.trim() || !description.value.trim()
    ){
        errorMessage.value = 'Please fill all the fields'
        return
    }

    errorMessage.value = ''
    emit('submit', {
    eventName: eventName.value,
    description: description.value
    })

    eventName.value = ''
    description.value = ''

}

</script>

<template>
<form class="registration-form" @submit.prevent="handleSubmit">
    <h4>Register for this Event</h4>

    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

    <label>
      Event Name
      <input v-model="eventName" type="text" placeholder="Enter event name" />
    </label>

    <label>
      Description
      <input v-model="description" type="text" placeholder="Enter description" />
    </label>

    <br>

    <div class="form-actions">
      <button type="submit" class="submit-btn">Submit Registration</button>
      
    </div>
  </form>
</template>
