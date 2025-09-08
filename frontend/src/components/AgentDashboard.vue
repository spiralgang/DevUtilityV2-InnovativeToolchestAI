<template>
  <div class="agent-dashboard">
    <h1>DevUtility Agent System</h1>
    <div class="agent-status">
      <div v-for="agent in agents" :key="agent.name" class="agent-card">
        <h3>{{ agent.name }}</h3>
        <p>Status: {{ agent.status }}</p>
        <button @click="toggleAgent(agent.name)" :class="agent.active ? 'stop' : 'start'">
          {{ agent.active ? 'Stop' : 'Start' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AgentDashboard',
  data() {
    return {
      agents: [
        { name: 'Integration Agent', status: 'idle', active: false },
        { name: 'Glue-Code Agent', status: 'idle', active: false },
        { name: 'Code Review Agent', status: 'idle', active: false },
        { name: 'Optimization Agent', status: 'idle', active: false }
      ]
    }
  },
  methods: {
    toggleAgent(agentName) {
      const agent = this.agents.find(a => a.name === agentName);
      if (agent) {
        agent.active = !agent.active;
        agent.status = agent.active ? 'running' : 'idle';
        this.$emit('agent-toggled', { name: agentName, active: agent.active });
      }
    }
  }
}
</script>

<style scoped>
.agent-dashboard {
  padding: 20px;
}

.agent-status {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.agent-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  background: #f9f9f9;
}

.agent-card h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.agent-card p {
  margin: 5px 0;
  color: #666;
}

button.start {
  background: #4CAF50;
  color: white;
}

button.stop {
  background: #f44336;
  color: white;
}

button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
</style>