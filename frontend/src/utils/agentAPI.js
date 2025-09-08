// Frontend API integration module for DevUtility agent system
import agentRegistry from './agentRegistry.js';

class AgentAPI {
  constructor() {
    this.baseUrl = process.env.VUE_APP_API_BASE || '/api';
    this.offline = process.env.TRANSFORMERS_OFFLINE === '1';
  }

  async activateAgent(agentName) {
    try {
      if (this.offline) {
        // Offline mode - use local registry
        return agentRegistry.activateAgent(agentName);
      }

      const response = await fetch(`${this.baseUrl}/agents/${agentName}/activate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (response.ok) {
        agentRegistry.activateAgent(agentName);
        return true;
      }
      return false;
    } catch (error) {
      console.error(`Failed to activate agent ${agentName}:`, error);
      return false;
    }
  }

  async deactivateAgent(agentName) {
    try {
      if (this.offline) {
        return agentRegistry.deactivateAgent(agentName);
      }

      const response = await fetch(`${this.baseUrl}/agents/${agentName}/deactivate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (response.ok) {
        agentRegistry.deactivateAgent(agentName);
        return true;
      }
      return false;
    } catch (error) {
      console.error(`Failed to deactivate agent ${agentName}:`, error);
      return false;
    }
  }

  async getAgentStatus(agentName) {
    try {
      if (this.offline) {
        const agent = agentRegistry.agents.get(agentName);
        return agent ? agent.status : 'unknown';
      }

      const response = await fetch(`${this.baseUrl}/agents/${agentName}/status`);
      if (response.ok) {
        const data = await response.json();
        return data.status;
      }
      return 'unknown';
    } catch (error) {
      console.error(`Failed to get status for agent ${agentName}:`, error);
      return 'error';
    }
  }

  async executeAgentPlan(planData) {
    try {
      if (this.offline) {
        console.log('[API] Offline mode - plan would be executed locally');
        return { success: true, message: 'Plan queued for local execution' };
      }

      const response = await fetch(`${this.baseUrl}/agents/execute-plan`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(planData)
      });

      if (response.ok) {
        return await response.json();
      }
      return { success: false, message: 'Plan execution failed' };
    } catch (error) {
      console.error('Failed to execute agent plan:', error);
      return { success: false, message: error.message };
    }
  }

  async getBindingRules() {
    try {
      if (this.offline) {
        return {
          frontend_globs: ["frontend/", "web/", "app/src/main/res/"],
          backend_globs: ["app/src/", "server/", "ai/"],
          extensions: [".js", ".ts", ".vue", ".kt", ".java", ".py", ".sh"],
          bind_rules: [
            { from_ext: ".vue", bind_hint: "ModuleUi" },
            { from_ext: ".js", bind_hint: "Registry" },
            { from_ext: ".ts", bind_hint: "Registry" },
            { from_ext: ".kt", bind_hint: "di/" },
            { from_ext: ".py", bind_hint: "scripts/" }
          ]
        };
      }

      const response = await fetch(`${this.baseUrl}/agents/binding-rules`);
      if (response.ok) {
        return await response.json();
      }
      return null;
    } catch (error) {
      console.error('Failed to fetch binding rules:', error);
      return null;
    }
  }
}

export default new AgentAPI();