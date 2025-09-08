// agentApi.js – Frontend for local agent interaction
// CODE-REAVER: Local agent communication - NO COPILOT

const LOCAL_AGENT_ENDPOINTS = {
  deepseek: '/api/agents/deepseek',
  phi2: '/api/agents/phi2',
  ollama: '/api/agents/ollama',
  qwen: '/api/agents/qwen',
  mixtral: '/api/agents/mixtral'
};

// Agent capability mappings
const AGENT_CAPABILITIES = {
  deepseek: {
    name: 'DeepSeek R1',
    description: 'Advanced reasoning and primary coding intelligence',
    capabilities: ['advanced_reasoning', 'primary_coding', 'devutility_ai', 'deep_code_analysis'],
    color: '#00ff00',
    icon: '🧠'
  },
  phi2: {
    name: 'Phi-2',
    description: 'Lightweight integration agent for frontend tasks',
    capabilities: ['code_generation', 'frontend_integration', 'lightweight_tasks'],
    color: '#0084ff',
    icon: '🔧'
  },
  ollama: {
    name: 'Ollama',
    description: 'Local model serving with REST API interface',
    capabilities: ['local_serving', 'multiple_models', 'rest_api'],
    color: '#ff6b35',
    icon: '🦙'
  },
  qwen: {
    name: 'Qwen 2.5',
    description: 'Multilingual glue code and integration logic',
    capabilities: ['multilingual', 'glue_code', 'integration_logic'],
    color: '#9c27b0',
    icon: '🌟'
  },
  mixtral: {
    name: 'Mixtral 8x7B',
    description: 'High-performance reasoning for complex tasks',
    capabilities: ['high_performance', 'complex_reasoning', 'advanced_generation'],
    color: '#ff4444',
    icon: '🚀'
  }
};

/**
 * Send prompt to local agent (replaces all Copilot interactions)
 */
export async function sendPromptToAgent({ 
  agent = 'deepseek', 
  prompt = '', 
  fileContent = null, 
  mode = 'prompt',
  context = {} 
}) {
  if (!LOCAL_AGENT_ENDPOINTS[agent]) {
    throw new Error(`Invalid agent: ${agent}. Available: ${Object.keys(LOCAL_AGENT_ENDPOINTS).join(', ')}`);
  }
  
  const payload = {
    prompt: prompt,
    file: fileContent || null,
    mode: mode,
    context: context,
    timestamp: new Date().toISOString(),
    source: 'frontend-api'
  };
  
  const response = await fetch(LOCAL_AGENT_ENDPOINTS[agent], {
    method: 'POST',
    headers: { 
      'Content-Type': 'application/json',
      'X-Agent-Source': 'local-frontend',
      'X-No-Copilot': 'true'
    },
    body: JSON.stringify(payload)
  });
  
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Agent response failed: ${response.status} - ${errorText}`);
  }
  
  return await response.json();
}

export async function getAgentStatus(agent) {
  try {
    const response = await sendPromptToAgent({
      agent: agent,
      mode: 'status',
      prompt: ''
    });
    
    return {
      ...response,
      capabilities: AGENT_CAPABILITIES[agent] || {},
      online: true
    };
  } catch (error) {
    return {
      online: false,
      error: error.message,
      capabilities: AGENT_CAPABILITIES[agent] || {}
    };
  }
}

export function getAvailableAgents() {
  return Object.entries(AGENT_CAPABILITIES).map(([key, info]) => ({
    id: key,
    endpoint: LOCAL_AGENT_ENDPOINTS[key],
    ...info
  }));
}

export default {
  sendPromptToAgent,
  getAgentStatus,
  getAvailableAgents,
  AGENT_CAPABILITIES,
  LOCAL_AGENT_ENDPOINTS
};