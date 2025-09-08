<template>
  <div class="agent-dashboard">
    <!-- Header -->
    <div class="dashboard-header">
      <h1>🤖 CODE-REAVER Local Agent Interface</h1>
      <p class="subtitle">No Copilot • Pure Local AI • DeepSeek • Phi-2 • Ollama</p>
    </div>

    <!-- Agent Selection -->
    <div class="agent-selector">
      <h2>Select Agent</h2>
      <div class="agent-grid">
        <div 
          v-for="agent in availableAgents" 
          :key="agent.id"
          class="agent-card"
          :class="{ active: chosenAgent === agent.id }"
          @click="selectAgent(agent.id)"
        >
          <div class="agent-icon">{{ agent.icon }}</div>
          <div class="agent-name">{{ agent.name }}</div>
          <div class="agent-desc">{{ agent.description }}</div>
          <div class="agent-status" :class="agentStatuses[agent.id]?.online ? 'online' : 'offline'">
            {{ agentStatuses[agent.id]?.online ? '🟢 Online' : '🔴 Offline' }}
          </div>
        </div>
      </div>
    </div>

    <!-- Interaction Mode -->
    <div class="interaction-mode">
      <h2>Interaction Mode</h2>
      <div class="mode-buttons">
        <button 
          v-for="mode in ['prompt', 'file', 'status', 'info']"
          :key="mode"
          :class="{ active: selectedMode === mode }"
          @click="selectedMode = mode"
          class="mode-btn"
        >
          {{ mode.charAt(0).toUpperCase() + mode.slice(1) }}
        </button>
      </div>
    </div>

    <!-- Input Area -->
    <div class="input-area" v-if="selectedMode === 'prompt'">
      <h2>Send Prompt</h2>
      <textarea 
        v-model="prompt" 
        placeholder="Enter your prompt here... (e.g., 'Write a TypeScript registry module for integrations')"
        class="prompt-input"
        rows="4"
      ></textarea>
      <button @click="sendPrompt" :disabled="!prompt.trim() || loading" class="send-btn">
        {{ loading ? '🔄 Processing...' : '🚀 Send to ' + (chosenAgent || 'Agent') }}
      </button>
    </div>

    <!-- File Upload Area -->
    <div class="input-area" v-if="selectedMode === 'file'">
      <h2>Upload File</h2>
      <div class="file-upload">
        <input 
          type="file" 
          ref="fileInput"
          @change="handleFileUpload"
          accept=".js,.ts,.vue,.py,.java,.kt,.md,.txt,.json"
          class="file-input"
        >
        <div class="file-info" v-if="selectedFile">
          📄 {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
        </div>
        <button 
          @click="sendFile" 
          :disabled="!selectedFile || loading" 
          class="send-btn"
        >
          {{ loading ? '🔄 Analyzing...' : '📄 Analyze File' }}
        </button>
      </div>
    </div>

    <!-- Status/Info Display -->
    <div class="input-area" v-if="selectedMode === 'status' || selectedMode === 'info'">
      <h2>{{ selectedMode === 'status' ? 'Agent Status' : 'Agent Information' }}</h2>
      <button @click="getAgentInfo" :disabled="loading" class="send-btn">
        {{ loading ? '🔄 Loading...' : '📊 Get ' + selectedMode }}
      </button>
    </div>

    <!-- Response Area -->
    <div class="response-area" v-if="response">
      <h2>Agent Response</h2>
      <div class="response-header">
        <span class="agent-badge" :style="{ backgroundColor: getAgentColor(lastUsedAgent) }">
          {{ getAgentIcon(lastUsedAgent) }} {{ getAgentName(lastUsedAgent) }}
        </span>
        <span class="timestamp">{{ formatTimestamp(responseTimestamp) }}</span>
      </div>
      <div class="response-content">
        <pre v-if="typeof response === 'string'">{{ response }}</pre>
        <div v-else class="json-response">
          <div v-for="(value, key) in response" :key="key" class="json-item">
            <strong>{{ key }}:</strong> {{ formatJsonValue(value) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Error Display -->
    <div class="error-area" v-if="error">
      <h2>❌ Error</h2>
      <div class="error-content">{{ error }}</div>
      <button @click="clearError" class="clear-btn">Clear</button>
    </div>

    <!-- Multi-Agent Section -->
    <div class="multi-agent-section">
      <h2>🚀 Multi-Agent Consensus</h2>
      <div class="multi-agent-controls">
        <div class="agent-checkboxes">
          <label v-for="agent in availableAgents" :key="agent.id" class="agent-checkbox">
            <input type="checkbox" v-model="selectedAgents" :value="agent.id">
            {{ agent.icon }} {{ agent.name }}
          </label>
        </div>
        <textarea 
          v-model="consensusPrompt" 
          placeholder="Enter prompt to send to multiple agents..."
          class="consensus-input"
          rows="3"
        ></textarea>
        <button 
          @click="getConsensus" 
          :disabled="selectedAgents.length < 2 || !consensusPrompt.trim() || loading"
          class="consensus-btn"
        >
          {{ loading ? '🔄 Getting Consensus...' : `🌟 Get Consensus (${selectedAgents.length} agents)` }}
        </button>
      </div>
    </div>

    <!-- Interaction History -->
    <div class="history-section">
      <h2>📋 Interaction History</h2>
      <div class="history-controls">
        <button @click="loadHistory" class="history-btn">🔄 Refresh</button>
        <button @click="clearHistory" class="history-btn">🗑️ Clear</button>
        <button @click="exportHistory" class="history-btn">💾 Export</button>
      </div>
      <div class="history-list">
        <div 
          v-for="(interaction, index) in interactionHistory" 
          :key="index"
          class="history-item"
        >
          <div class="history-header">
            <span class="agent-badge" :style="{ backgroundColor: getAgentColor(interaction.agent) }">
              {{ getAgentIcon(interaction.agent) }} {{ getAgentName(interaction.agent) }}
            </span>
            <span class="history-timestamp">{{ formatTimestamp(interaction.timestamp) }}</span>
          </div>
          <div class="history-content">
            <strong>{{ interaction.mode }}:</strong> {{ interaction.prompt }}
            <div class="history-status" :class="interaction.success ? 'success' : 'error'">
              {{ interaction.success ? '✅ Success' : '❌ Failed' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import agentApi from '../utils/agentApi.js';

export default {
  name: 'AgentDashboard',
  data() {
    return {
      chosenAgent: 'deepseek',
      selectedMode: 'prompt',
      prompt: '',
      response: null,
      error: null,
      loading: false,
      availableAgents: [],
      agentStatuses: {},
      selectedFile: null,
      lastUsedAgent: null,
      responseTimestamp: null,
      selectedAgents: [],
      consensusPrompt: '',
      consensusResponse: null,
      interactionHistory: []
    };
  },
  
  async mounted() {
    this.loadAvailableAgents();
    this.checkAllAgentStatuses();
    this.loadHistory();
  },
  
  methods: {
    loadAvailableAgents() {
      this.availableAgents = agentApi.getAvailableAgents();
    },
    
    async checkAllAgentStatuses() {
      for (const agent of this.availableAgents) {
        try {
          const status = await agentApi.getAgentStatus(agent.id);
          this.$set(this.agentStatuses, agent.id, status);
        } catch (error) {
          this.$set(this.agentStatuses, agent.id, { online: false, error: error.message });
        }
      }
    },
    
    selectAgent(agentId) {
      this.chosenAgent = agentId;
      this.clearError();
    },
    
    async sendPrompt() {
      if (!this.prompt.trim()) return;
      
      this.loading = true;
      this.clearError();
      
      try {
        const result = await agentApi.sendPromptToAgent({
          agent: this.chosenAgent,
          prompt: this.prompt,
          mode: 'prompt'
        });
        
        this.response = result.reply || result.result || result;
        this.lastUsedAgent = this.chosenAgent;
        this.responseTimestamp = new Date().toISOString();
        
      } catch (error) {
        this.error = error.message;
      } finally {
        this.loading = false;
      }
    },
    
    handleFileUpload(event) {
      this.selectedFile = event.target.files[0];
    },
    
    async sendFile() {
      if (!this.selectedFile) return;
      
      this.loading = true;
      this.clearError();
      
      try {
        const reader = new FileReader();
        reader.onload = async (e) => {
          try {
            const result = await agentApi.sendPromptToAgent({
              agent: this.chosenAgent,
              prompt: `Analyze this file: ${this.selectedFile.name}`,
              fileContent: e.target.result,
              mode: 'file',
              context: {
                fileName: this.selectedFile.name,
                fileSize: this.selectedFile.size,
                fileType: this.selectedFile.type
              }
            });
            
            this.response = result.reply || result.result || result;
            this.lastUsedAgent = this.chosenAgent;
            this.responseTimestamp = new Date().toISOString();
            
          } catch (error) {
            this.error = error.message;
          } finally {
            this.loading = false;
          }
        };
        reader.readAsText(this.selectedFile);
        
      } catch (error) {
        this.error = error.message;
        this.loading = false;
      }
    },
    
    async getAgentInfo() {
      this.loading = true;
      this.clearError();
      
      try {
        const result = await agentApi.sendPromptToAgent({
          agent: this.chosenAgent,
          mode: this.selectedMode,
          prompt: ''
        });
        
        this.response = result;
        this.lastUsedAgent = this.chosenAgent;
        this.responseTimestamp = new Date().toISOString();
        
      } catch (error) {
        this.error = error.message;
      } finally {
        this.loading = false;
      }
    },
    
    async getConsensus() {
      if (this.selectedAgents.length < 2 || !this.consensusPrompt.trim()) return;
      
      this.loading = true;
      this.clearError();
      
      try {
        // Send to multiple agents sequentially
        const responses = [];
        for (const agent of this.selectedAgents) {
          try {
            const result = await agentApi.sendPromptToAgent({
              agent: agent,
              prompt: this.consensusPrompt,
              mode: 'prompt'
            });
            responses.push({
              agent: agent,
              response: result.reply || result.result || result,
              success: true
            });
          } catch (error) {
            responses.push({
              agent: agent,
              error: error.message,
              success: false
            });
          }
        }
        
        this.response = {
          consensusMode: true,
          prompt: this.consensusPrompt,
          agents: this.selectedAgents,
          responses: responses,
          timestamp: new Date().toISOString()
        };
        this.lastUsedAgent = 'consensus';
        this.responseTimestamp = new Date().toISOString();
        
      } catch (error) {
        this.error = error.message;
      } finally {
        this.loading = false;
      }
    },
    
    loadHistory() {
      // Load from localStorage
      this.interactionHistory = JSON.parse(localStorage.getItem('agentLogs') || '[]');
    },
    
    clearHistory() {
      localStorage.removeItem('agentLogs');
      this.interactionHistory = [];
    },
    
    exportHistory() {
      const data = {
        exported: new Date().toISOString(),
        interactions: this.interactionHistory
      };
      
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `agent-history-${new Date().toISOString().split('T')[0]}.json`;
      a.click();
      URL.revokeObjectURL(url);
    },
    
    clearError() {
      this.error = null;
    },
    
    getAgentColor(agentId) {
      return agentApi.AGENT_CAPABILITIES[agentId]?.color || '#666666';
    },
    
    getAgentIcon(agentId) {
      return agentApi.AGENT_CAPABILITIES[agentId]?.icon || '🤖';
    },
    
    getAgentName(agentId) {
      return agentApi.AGENT_CAPABILITIES[agentId]?.name || agentId;
    },
    
    formatTimestamp(timestamp) {
      return new Date(timestamp).toLocaleString();
    },
    
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },
    
    formatJsonValue(value) {
      if (typeof value === 'object') {
        return JSON.stringify(value, null, 2);
      }
      return String(value);
    }
  }
};
</script>

<style scoped>
.agent-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  background: #1e1e1e;
  color: #ffffff;
  min-height: 100vh;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #00ff00;
  padding-bottom: 20px;
}

.dashboard-header h1 {
  color: #00ff00;
  font-size: 2.5em;
  margin: 0;
  text-shadow: 0 0 10px #00ff00;
}

.subtitle {
  color: #888;
  margin: 10px 0 0 0;
  font-size: 1.1em;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 30px;
}

.agent-card {
  background: #2d2d2d;
  border: 2px solid #444;
  border-radius: 10px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.agent-card:hover {
  border-color: #00ff00;
  transform: translateY(-2px);
}

.agent-card.active {
  border-color: #00ff00;
  background: #003300;
  box-shadow: 0 0 15px rgba(0, 255, 0, 0.3);
}

.agent-icon {
  font-size: 2em;
  margin-bottom: 10px;
}

.agent-name {
  font-weight: bold;
  color: #00ff00;
  margin-bottom: 5px;
}

.agent-desc {
  font-size: 0.9em;
  color: #ccc;
  margin-bottom: 10px;
}

.agent-status.online {
  color: #00ff00;
}

.agent-status.offline {
  color: #ff4444;
}

.mode-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.mode-btn {
  background: #2d2d2d;
  border: 2px solid #444;
  color: white;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mode-btn:hover {
  border-color: #00ff00;
}

.mode-btn.active {
  background: #00ff00;
  color: #000;
  border-color: #00ff00;
}

.input-area {
  background: #2d2d2d;
  border: 2px solid #444;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.prompt-input, .consensus-input {
  width: 100%;
  background: #1e1e1e;
  border: 2px solid #444;
  border-radius: 5px;
  padding: 15px;
  color: white;
  font-family: inherit;
  font-size: 14px;
  resize: vertical;
  margin-bottom: 15px;
}

.prompt-input:focus, .consensus-input:focus {
  outline: none;
  border-color: #00ff00;
}

.send-btn, .consensus-btn, .history-btn, .clear-btn {
  background: #00ff00;
  color: #000;
  border: none;
  padding: 12px 24px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.3s ease;
}

.send-btn:hover, .consensus-btn:hover, .history-btn:hover, .clear-btn:hover {
  background: #00cc00;
  transform: translateY(-1px);
}

.send-btn:disabled, .consensus-btn:disabled {
  background: #666;
  color: #999;
  cursor: not-allowed;
  transform: none;
}

.response-area, .error-area {
  background: #2d2d2d;
  border: 2px solid #444;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.response-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.agent-badge {
  background: #00ff00;
  color: #000;
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 0.9em;
  font-weight: bold;
}

.timestamp {
  color: #888;
  font-size: 0.9em;
}

.response-content {
  background: #1e1e1e;
  border: 1px solid #444;
  border-radius: 5px;
  padding: 15px;
  overflow-x: auto;
}

.response-content pre {
  margin: 0;
  white-space: pre-wrap;
  color: #00ff00;
}

.error-area {
  border-color: #ff4444;
}

.error-content {
  background: #1e1e1e;
  border: 1px solid #ff4444;
  border-radius: 5px;
  padding: 15px;
  color: #ff4444;
  margin-bottom: 15px;
}

.multi-agent-section, .history-section {
  background: #2d2d2d;
  border: 2px solid #444;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.agent-checkboxes {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.agent-checkbox {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.history-controls {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  background: #1e1e1e;
  border: 1px solid #444;
  border-radius: 5px;
  padding: 10px;
  margin-bottom: 10px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.history-content {
  font-size: 0.9em;
}

.history-status.success {
  color: #00ff00;
}

.history-status.error {
  color: #ff4444;
}

h2 {
  color: #00ff00;
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 1.3em;
}
</style>