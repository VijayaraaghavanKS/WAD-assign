<script setup>


defineProps({ events: {type: Array, required: true}})

</script>

<template>
    <div class="event-list">

        <h3> Events List</h3>

        <p v-if = "events.length == 0"> No events registered yet</p>

        <ul v-else>
            <li v-for="(event,index) in events" :key="index">
                <h4>{{event.eventName}}</h4>
                <p>{{ event.description }}</p>
            </li>
        </ul>
    </div>
</template>