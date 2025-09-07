// Minimal provider adapters (Node) — environment-driven, no token printing.
// Supports: openai, azure-openai, huggingface, anthropic, ollama, vertex, zhipu, custom.
// Each adapter returns a normalized string (or JSON string if structured).

const fetch = require('node-fetch');

function safeEnv(names) {
  for (const n of names) {
    if (process.env[n]) return process.env[n];
  }
  return null;
}

async function openai(prompt, opts = {}) {
  const key = process.env.OPENAI_API_KEY;
  if (!key) throw new Error('OPENAI_API_KEY missing');
  const model = opts.model || process.env.OPENAI_MODEL || 'gpt-4o-mini';
  const res = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({
      model,
      messages: [{ role: 'system', content: opts.system || '' }, { role: 'user', content: prompt }],
      temperature: opts.temperature ?? 0.0,
      max_tokens: opts.max_tokens ?? 800
    })
  });
  const json = await res.json();
  if (json?.choices && json.choices[0]) return json.choices[0].message?.content || JSON.stringify(json);
  return JSON.stringify(json);
}

async function azureOpenAI(prompt, opts = {}) {
  const key = safeEnv(['AZURE_OPENAI_KEY', 'AZURE_API_KEY']);
  const resource = process.env.AZURE_OPENAI_RESOURCE;
  const deployment = opts.deployment || process.env.AZURE_OPENAI_DEPLOYMENT || process.env.AZURE_OPENAI_MODEL;
  if (!key || !resource || !deployment) throw new Error('Azure OpenAI env incomplete');
  const url = `https://${resource}.openai.azure.com/openai/deployments/${deployment}/chat/completions?api-version=2023-10-01-preview`;
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'api-key': key, 'Content-Type': 'application/json' },
    body: JSON.stringify({
      messages: [{ role: 'system', content: opts.system || '' }, { role: 'user', content: prompt }],
      temperature: opts.temperature ?? 0.0,
      max_tokens: opts.max_tokens ?? 800
    })
  });
  const json = await res.json();
  if (json?.choices && json.choices[0]) return json.choices[0].message?.content || JSON.stringify(json);
  return JSON.stringify(json);
}

async function huggingface(prompt, opts = {}) {
  const key = safeEnv(['HUGGINGFACE_API_KEY', 'HF_TOKEN']);
  if (!key) throw new Error('HUGGINGFACE_API_KEY missing');
  const model = opts.model || process.env.HF_MODEL || 'google/flan-t5-small';
  const url = `https://api-inference.huggingface.co/models/${encodeURIComponent(model)}`;
  const res = await fetch(url, {
    method: 'POST',
    headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/json' },
    body: JSON.stringify({ inputs: prompt, parameters: { max_new_tokens: opts.max_tokens || 256 } })
  });
  const json = await res.json();
  if (Array.isArray(json) && json[0]?.generated_text) return json[0].generated_text;
  if (json?.generated_text) return json.generated_text;
  return JSON.stringify(json);
}

async function anthropic(prompt, opts = {}) {
  const key = safeEnv(['ANTHROPIC_API_KEY', 'ANTHROPIC_KEY']);
  if (!key) throw new Error('ANTHROPIC_API_KEY missing');
  const model = opts.model || process.env.ANTHROPIC_MODEL || 'claude-2.1';
  const url = 'https://api.anthropic.com/v1/complete';
  const payload = {
    model,
    prompt: `Assistant: You are helpful.\n\nHuman: ${prompt}\n\nAssistant:`,
    max_tokens_to_sample: opts.max_tokens || 512,
    temperature: opts.temperature ?? 0.0
  };
  const res = await fetch(url, { method: 'POST', headers: { 'x-api-key': key, 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
  const json = await res.json();
  return json?.completion || JSON.stringify(json);
}

async function ollama(prompt, opts = {}) {
  // Ollama local or hosted. OLLAMA_HOST env example: http://localhost:11434
  const host = process.env.OLLAMA_HOST || 'http://localhost:11434';
  const model = opts.model || process.env.OLLAMA_MODEL || 'ollama';
  const url = `${host.replace(/\/$/, '')}/api/generate`;
  const apiKey = process.env.OLLAMA_API_KEY;
  const headers = { 'Content-Type': 'application/json' };
  if (apiKey) headers.Authorization = `Bearer ${apiKey}`;
  const res = await fetch(url, { method: 'POST', headers, body: JSON.stringify({ model, prompt, max_tokens: opts.max_tokens || 512 }) });
  const json = await res.json();
  if (json?.text) return json.text;
  return JSON.stringify(json);
}

async function vertex(prompt, opts = {}) {
  const token = safeEnv(['GOOGLE_OAUTH_ACCESS_TOKEN', 'GCP_ACCESS_TOKEN']);
  const endpoint = process.env.GEMINI_API_ENDPOINT || process.env.VERTEX_MODEL_ENDPOINT;
  if (!token || !endpoint) throw new Error('Vertex token or endpoint missing');
  const res = await fetch(endpoint, { method: 'POST', headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ input: prompt, parameters: { maxOutputTokens: opts.max_tokens || 1024, temperature: opts.temperature ?? 0.0 } }) });
  const json = await res.json();
  return JSON.stringify(json);
}

async function zhipu(prompt, opts = {}) {
  const key = safeEnv(['ZHIPU_API_KEY', 'BIGMODEL_KEY']);
  if (!key) throw new Error('ZHIPU_API_KEY missing');
  const endpoint = process.env.ZHIPU_ENDPOINT || 'https://api.zhipu.ai/endpoint';
  const res = await fetch(endpoint, { method: 'POST', headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/json' }, body: JSON.stringify({ text: prompt, model: opts.model || process.env.ZHIPU_MODEL }) });
  const json = await res.json();
  return json?.text || JSON.stringify(json);
}

async function custom(prompt, opts = {}) {
  const url = process.env.PROVIDER_CUSTOM_URL;
  if (!url) throw new Error('PROVIDER_CUSTOM_URL missing');
  const keyEnv = process.env.PROVIDER_CUSTOM_KEY_NAME;
  const headers = { 'Content-Type': 'application/json' };
  if (keyEnv && process.env[keyEnv]) headers.Authorization = `Bearer ${process.env[keyEnv]}`;
  const res = await fetch(url, { method: 'POST', headers, body: JSON.stringify({ prompt, ...opts }) });
  const json = await res.json();
  if (json?.response) return json.response;
  return JSON.stringify(json);
}

function autoDetect() {
  if (process.env.OLLAMA_HOST) return 'ollama';
  if (process.env.OPENAI_API_KEY) return 'openai';
  if (process.env.HUGGINGFACE_API_KEY || process.env.HF_TOKEN) return 'hf';
  if (process.env.ANTHROPIC_API_KEY) return 'anthropic';
  if (process.env.AZURE_OPENAI_KEY) return 'azure';
  if (process.env.GOOGLE_OAUTH_ACCESS_TOKEN || process.env.GCP_ACCESS_TOKEN) return 'vertex';
  if (process.env.ZHIPU_API_KEY) return 'zhipu';
  if (process.env.PROVIDER_CUSTOM_URL) return 'custom';
  return 'openai';
}

async function sendPrompt(prompt, options = {}) {
  const provider = (options.provider || process.env.ICEDMAN_AI_PROVIDER || 'auto').toLowerCase();
  const p = provider === 'auto' ? autoDetect() : provider;
  try {
    switch (p) {
      case 'openai': return await openai(prompt, options);
      case 'azure': return await azureOpenAI(prompt, options);
      case 'hf':
      case 'huggingface': return await huggingface(prompt, options);
      case 'anthropic': return await anthropic(prompt, options);
      case 'ollama': return await ollama(prompt, options);
      case 'vertex': return await vertex(prompt, options);
      case 'zhipu':
      case 'glm':
      case 'bigmodel': return await zhipu(prompt, options);
      case 'custom': return await custom(prompt, options);
      default: throw new Error(`Unknown provider: ${p}`);
    }
  } catch (e) {
    // normalized error string (no tokens leaked)
    return `[provider-error] ${p}: ${e.message}`;
  }
}

module.exports = { sendPrompt };