// Agent Registry for managing frontend-backend integration
class AgentRegistry {
  constructor() {
    this.agents = new Map();
    this.activeAgents = new Set();
    this.bindingRules = new Map();
  }

  registerAgent(name, config) {
    this.agents.set(name, {
      name,
      config,
      status: 'registered',
      lastActivity: null,
      bindTargets: []
    });
    console.log(`[Registry] Agent ${name} registered`);
  }

  activateAgent(name) {
    if (this.agents.has(name)) {
      this.activeAgents.add(name);
      const agent = this.agents.get(name);
      agent.status = 'active';
      agent.lastActivity = new Date();
      console.log(`[Registry] Agent ${name} activated`);
      return true;
    }
    return false;
  }

  deactivateAgent(name) {
    if (this.activeAgents.has(name)) {
      this.activeAgents.delete(name);
      const agent = this.agents.get(name);
      agent.status = 'inactive';
      console.log(`[Registry] Agent ${name} deactivated`);
      return true;
    }
    return false;
  }

  getActiveAgents() {
    return Array.from(this.activeAgents).map(name => this.agents.get(name));
  }

  addBindingRule(fromExt, bindHint) {
    if (!this.bindingRules.has(fromExt)) {
      this.bindingRules.set(fromExt, []);
    }
    this.bindingRules.get(fromExt).push(bindHint);
    console.log(`[Registry] Binding rule added: ${fromExt} -> ${bindHint}`);
  }

  getBindingHints(fileExtension) {
    return this.bindingRules.get(fileExtension) || [];
  }

  sendToBackend(agentName, data) {
    // Simulate backend communication
    console.log(`[Registry] Sending to backend agent ${agentName}:`, data);
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ status: 'success', response: `Agent ${agentName} processed request` });
      }, 1000);
    });
  }
}

// Global registry instance
const agentRegistry = new AgentRegistry();

// Initialize default agents
agentRegistry.registerAgent('Integration Agent', {
  type: 'phi2',
  endpoint: '/api/agents/integration',
  capabilities: ['file-mapping', 'integration-points']
});

agentRegistry.registerAgent('Glue-Code Agent', {
  type: 'qwen',
  endpoint: '/api/agents/glue-code', 
  capabilities: ['missing-logic', 'code-injection']
});

agentRegistry.registerAgent('Code Review Agent', {
  type: 'deepseek',
  endpoint: '/api/agents/code-review',
  capabilities: ['security-scan', 'best-practices']
});

agentRegistry.registerAgent('Optimization Agent', {
  type: 'ollama',
  endpoint: '/api/agents/optimization',
  capabilities: ['performance', 'resource-optimization']
});

// Setup default binding rules
agentRegistry.addBindingRule('.vue', 'ModuleUi');
agentRegistry.addBindingRule('.js', 'Registry');
agentRegistry.addBindingRule('.ts', 'Registry');

export default agentRegistry;