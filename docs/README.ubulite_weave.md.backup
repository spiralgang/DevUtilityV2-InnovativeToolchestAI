```md
# ICEDMAN EnergeticWeave — AI Provider Configuration (Updated)

Summary
-------
The ICEDMAN runtime now supports multiple free / generous-tier AI providers via a pluggable adapter layer (iceman_drone_tool.providers). The old Inflection-only flow has been removed.

Providers & env vars (examples)
- OpenAI
  - OPENAI_API_KEY
  - OPTIONAL: OPENAI_MODEL (e.g. gpt-4o-mini)
- Azure OpenAI
  - AZURE_OPENAI_KEY
  - AZURE_OPENAI_RESOURCE (resource name)
  - AZURE_OPENAI_DEPLOYMENT (deployment name) or AZURE_OPENAI_MODEL
- Hugging Face Inference
  - HUGGINGFACE_API_KEY (or HF_TOKEN)
  - HF_MODEL (e.g. meta-llama/Llama-2-7b)
- Anthropic
  - ANTHROPIC_API_KEY
  - ANTHROPIC_MODEL
- Ollama (local/hosted)
  - OLLAMA_HOST (e.g. http://localhost:11434)
  - OLLAMA_API_KEY (optional)
  - OLLAMA_MODEL
- Google Vertex AI
  - GOOGLE_OAUTH_ACCESS_TOKEN or GCP_ACCESS_TOKEN
  - VERTEX_MODEL_ENDPOINT or GEMINI_API_ENDPOINT (full REST URL)
- Zhipu / GLM / BigModel
  - ZHIPU_API_KEY or BIGMODEL_KEY
  - ZHIPU_ENDPOINT (optional)
- Generic / Community providers (HackLiberty, DeepSeek, Perplexity, Qualcomm QAI)
  - PROVIDER_CUSTOM_URL (for custom endpoints)
  - PROVIDER_CUSTOM_KEY_NAME (optional env var name used for authorization)
  - HACKLIBERTY_KEY, DEEPSEEK_KEY, PERPLEXITY_KEY, QAI_TOKEN (provider-specific)

How to choose provider
- Set ICEDMAN_AI_PROVIDER to one of: auto|openai|azure|hf|anthropic|ollama|vertex|zhipu|custom
- "auto" will pick the first provider with a detected token in a sane order (ollama -> openai -> hf -> anthropic -> azure -> vertex -> zhipu -> others)

Security notes
- Tokens MUST be set as environment variables; do not hardcode or print them.
- On devices, set tokens in systemd user unit, shell profile, or app config — never in public scripts.

Example (useful for testing on-device with Ollama local server):
```bash
export ICEDMAN_AI_PROVIDER=ollama
export OLLAMA_HOST=http://localhost:11434
export OLLAMA_MODEL=claude-local
python3 -c "from iceman_drone_tool.whisper import call_pi; print(call_pi('Assess wind drift at current position.'))"
```

Operational guidance
- Start with DRONE_SIMULATE=1 when testing any drone mission flows.
- Validate provider responses locally and inspect artifacts under `~/icedman/weave_artifacts/` before enabling any automated physical actions.
- If integrating an odd API that speaks "puter.js" or proprietary JSON, set PROVIDER_CUSTOM_URL and PROVIDER_CUSTOM_KEY_NAME and ensure the endpoint accepts { "prompt": "..." }.

Reference
- See /reference vault for mapping provider endpoints and sample curl requests.
```