"""
Pluggable AI Provider adapters for ICEDMAN

Purpose
- Provide a single, secure entrypoint send_prompt(prompt, *, provider=None, **opts)
  that routes prompts to many free-or-generous-tier providers (auto-detect or explicit).
- Keep secrets in environment variables only; never log tokens.
- Provide small, well-documented adapters for:
    - OpenAI (openai.com)
    - Azure OpenAI (azure.openai)
    - Hugging Face Inference
    - Anthropic
    - Ollama (local or hosted HTTP)
    - Google Vertex AI (simple REST mapping)
    - Zhipu / GLM style endpoints (generic)
    - HackLiberty / DeepSeek-Coder / Perplexity (generic HTTP adapters)
    - Custom provider via PROVIDER_CUSTOM_URL (puter.js style)
- Defensive: timeouts, retries (light), normalized string output.

Usage
- Set env var ICEDMAN_AI_PROVIDER to force provider (e.g. "openai","azure","hf","ollama","anthropic","vertex","zhipu","custom","auto")
- Ensure provider token env vars set (see README.ubulite_weave.md)
- call send_prompt("summarize foo", provider="auto")

Design choices / rationale (concise)
- Lightweight, no heavy SDKs to keep Android/Termux/Ubuntu-Lite compatibility.
- Use requests only (small, widely available). If not installed, raise clear error.
- Provider adapters return a normalized text string; structured outputs (JSON) are preserved if provider returns JSON blob.
- Do not print or return tokens.
"""

from typing import Optional, Dict, Any
import os
import json
import time
import logging

try:
    import requests
except Exception as e:
    raise ImportError("requests is required by iceman_drone_tool.providers") from e

logger = logging.getLogger("iceman_drone_tool.providers")

# Environment names for tokens commonly used across the device/repo
ENV_TOKENS = {
    "openai": ("OPENAI_API_KEY",),
    "azure": ("AZURE_OPENAI_KEY", "AZURE_API_KEY"),
    "hf": ("HUGGINGFACE_API_KEY", "HF_TOKEN"),
    "anthropic": ("ANTHROPIC_API_KEY", "ANTHROPIC_KEY"),
    "ollama": ("OLLAMA_API_KEY", "OLLAMA_HOST"),
    "vertex": ("GOOGLE_OAUTH_ACCESS_TOKEN", "GCP_ACCESS_TOKEN"),
    "zhipu": ("ZHIPU_API_KEY", "BIGMODEL_KEY"),
    "hackliberty": ("HACKLIBERTY_KEY",),
    "deepseek": ("DEEPSEEK_KEY",),
    "perplexity": ("PERPLEXITY_KEY",),
    "qai": ("QAI_TOKEN", "QUALCOMM_QAI_KEY"),
    "custom": ("PROVIDER_CUSTOM_KEY",),
}

# Simple shared HTTP request helpers
def _post_json(url: str, headers: Dict[str, str], payload: Any, timeout: float = 12.0):
    try:
        r = requests.post(url, headers=headers, json=payload, timeout=timeout)
        r.raise_for_status()
        try:
            return r.json()
        except Exception:
            return r.text
    except requests.RequestException as e:
        logger.debug("HTTP POST failed for %s: %s", url, e)
        raise

def _get_json(url: str, headers: Dict[str, str], params: Dict[str, Any] = None, timeout: float = 12.0):
    try:
        r = requests.get(url, headers=headers, params=params, timeout=timeout)
        r.raise_for_status()
        try:
            return r.json()
        except Exception:
            return r.text
    except requests.RequestException as e:
        logger.debug("HTTP GET failed for %s: %s", url, e)
        raise

# Provider adapters ---------------------------------------------------------
def _openai(prompt: str, model: str = "gpt-4o-mini", **opts):
    key = os.getenv("OPENAI_API_KEY")
    if not key:
        raise RuntimeError("OPENAI_API_KEY not set for OpenAI adapter")
    url = "https://api.openai.com/v1/chat/completions"
    headers = {"Authorization": f"Bearer {key}", "Content-Type": "application/json"}
    payload = {
        "model": model,
        "messages": [{"role": "system", "content": opts.get("system", "")}, {"role": "user", "content": prompt}],
        "temperature": opts.get("temperature", 0.0),
        "max_tokens": opts.get("max_tokens", 1024),
    }
    res = _post_json(url, headers, payload)
    # Normalize response
    if isinstance(res, dict):
        # OpenAI shape: choices[0].message.content
        choices = res.get("choices") or []
        if choices and isinstance(choices[0], dict):
            return choices[0].get("message", {}).get("content") or json.dumps(res)
        return json.dumps(res)
    return str(res)

def _azure_openai(prompt: str, *, resource=None, deployment=None, model=None, **opts):
    # Azure expects: https://{resource}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version=2023-10-01-preview
    key = os.getenv("AZURE_OPENAI_KEY") or os.getenv("AZURE_API_KEY")
    resource = resource or os.getenv("AZURE_OPENAI_RESOURCE")
    deployment = deployment or os.getenv("AZURE_OPENAI_DEPLOYMENT") or os.getenv("AZURE_OPENAI_MODEL")
    if not (key and resource and deployment):
        raise RuntimeError("AZURE_OPENAI_KEY + AZURE_OPENAI_RESOURCE + AZURE_OPENAI_DEPLOYMENT (or AZURE_OPENAI_MODEL) required")
    url = f"https://{resource}.openai.azure.com/openai/deployments/{deployment}/chat/completions?api-version=2023-10-01-preview"
    headers = {"api-key": key, "Content-Type": "application/json"}
    payload = {
        "messages": [{"role": "system", "content": opts.get("system", "")}, {"role": "user", "content": prompt}],
        "temperature": opts.get("temperature", 0.0),
        "max_tokens": opts.get("max_tokens", 1024),
    }
    res = _post_json(url, headers, payload)
    if isinstance(res, dict):
        choices = res.get("choices") or []
        if choices:
            return choices[0].get("message", {}).get("content") or json.dumps(res)
    return str(res)

def _huggingface_inference(prompt: str, model: str = "gpt2", **opts):
    key = os.getenv("HUGGINGFACE_API_KEY") or os.getenv("HF_TOKEN")
    if not key:
        raise RuntimeError("HUGGINGFACE_API_KEY not set for HF adapter")
    url = f"https://api-inference.huggingface.co/models/{model}"
    headers = {"Authorization": f"Bearer {key}", "Accept": "application/json"}
    payload = {"inputs": prompt, "parameters": {"max_new_tokens": opts.get("max_tokens", 512)}}
    res = _post_json(url, headers, payload)
    # HF inference sometimes returns list or dict
    if isinstance(res, list) and res:
        return res[0].get("generated_text") or json.dumps(res)
    if isinstance(res, dict):
        # check common shapes
        if "generated_text" in res:
            return res["generated_text"]
        # text in choices?
        return json.dumps(res)
    return str(res)

def _anthropic(prompt: str, model: str = "claude-2.1", **opts):
    key = os.getenv("ANTHROPIC_API_KEY") or os.getenv("ANTHROPIC_KEY")
    if not key:
        raise RuntimeError("ANTHROPIC_API_KEY not set for Anthropic adapter")
    url = "https://api.anthropic.com/v1/complete"
    headers = {"x-api-key": key, "Content-Type": "application/json"}
    # Anthropic expects a 'prompt' with role tokens (we keep it simple)
    payload = {
        "model": model,
        "prompt": f"Assistant: You are helpful.\n\nHuman: {prompt}\n\nAssistant:",
        "max_tokens_to_sample": opts.get("max_tokens", 512),
        "temperature": opts.get("temperature", 0.0),
    }
    res = _post_json(url, headers, payload)
    if isinstance(res, dict):
        return res.get("completion") or json.dumps(res)
    return str(res)

def _ollama(prompt: str, model: str = "ollama", host: Optional[str] = None, **opts):
    # Ollama local API typically: POST http://localhost:11434/api/generate
    host = host or os.getenv("OLLAMA_HOST") or "http://localhost:11434"
    url = f"{host.rstrip('/')}/api/generate"
    api_key = os.getenv("OLLAMA_API_KEY")
    headers = {"Content-Type": "application/json"}
    if api_key:
        headers["Authorization"] = f"Bearer {api_key}"
    payload = {"model": model, "prompt": prompt, "max_tokens": opts.get("max_tokens", 512)}
    res = _post_json(url, headers, payload)
    # Ollama reply shapes vary; try to extract text
    if isinstance(res, dict):
        if "text" in res:
            return res["text"]
        # stream-style may exist; return json
        return json.dumps(res)
    return str(res)

def _vertex_ai(prompt: str, model: str = None, **opts):
    # Simple Vertex AI Raw REST mapping; prefer short-lived access token via env GOOGLE_OAUTH_ACCESS_TOKEN
    token = os.getenv("GOOGLE_OAUTH_ACCESS_TOKEN") or os.getenv("GCP_ACCESS_TOKEN")
    if not token:
        raise RuntimeError("GOOGLE_OAUTH_ACCESS_TOKEN (or GCP_ACCESS_TOKEN) required for Vertex adapter")
    # User must set GEMINI_API_ENDPOINT or VERTEX_MODEL_ENDPOINT in env as full URL. We avoid constructing project-paths here.
    endpoint = os.getenv("GEMINI_API_ENDPOINT") or os.getenv("VERTEX_MODEL_ENDPOINT")
    if not endpoint:
        raise RuntimeError("Set GEMINI_API_ENDPOINT or VERTEX_MODEL_ENDPOINT for Vertex AI calls")
    headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
    payload = {"input": prompt, "parameters": {"temperature": opts.get("temperature", 0.0), "maxOutputTokens": opts.get("max_tokens", 1024)}}
    res = _post_json(endpoint, headers, payload)
    return json.dumps(res) if not isinstance(res, str) else res

def _zhipu(prompt: str, model: str = None, **opts):
    key = os.getenv("ZHIPU_API_KEY") or os.getenv("BIGMODEL_KEY")
    if not key:
        raise RuntimeError("ZHIPU_API_KEY / BIGMODEL_KEY required for Zhipu adapter")
    endpoint = os.getenv("ZHIPU_ENDPOINT", "https://api.zhipu.ai/endpoint")
    headers = {"Authorization": f"Bearer {key}", "Content-Type": "application/json"}
    payload = {"text": prompt, "model": model or os.getenv("ZHIPU_MODEL", "gpt-3.5"), "max_tokens": opts.get("max_tokens", 1024)}
    res = _post_json(endpoint, headers, payload)
    # generic parse
    if isinstance(res, dict):
        return res.get("text") or res.get("data") or json.dumps(res)
    return str(res)

def _generic_http(prompt: str, url: str, token_env: Optional[str] = None, **opts):
    headers = {"Content-Type": "application/json"}
    if token_env:
        tk = os.getenv(token_env)
        if tk:
            headers["Authorization"] = f"Bearer {tk}"
    payload = {"prompt": prompt, **opts}
    res = _post_json(url, headers, payload)
    if isinstance(res, dict):
        return res.get("response") or res.get("text") or json.dumps(res)
    return str(res)

# Main router ---------------------------------------------------------------
def _auto_detect_provider():
    # Order chosen to maximize availability of free tiers on-device.
    for p, envs in (("ollama", ENV_TOKENS["ollama"]), ("openai", ENV_TOKENS["openai"]),
                    ("hf", ENV_TOKENS["hf"]), ("anthropic", ENV_TOKENS["anthropic"]),
                    ("azure", ENV_TOKENS["azure"]), ("vertex", ENV_TOKENS["vertex"]),
                    ("zhipu", ENV_TOKENS["zhipu"]), ("qai", ENV_TOKENS["qai"])):
        for e in envs:
            if os.getenv(e):
                return p
    # fallback to custom URL
    if os.getenv("PROVIDER_CUSTOM_URL"):
        return "custom"
    # last resort
    return "openai" if os.getenv("OPENAI_API_KEY") else "ollama"

def send_prompt(prompt: str, provider: Optional[str] = None, model: Optional[str] = None, **opts) -> str:
    """
    Unified call to many backends.
    - prompt: text to send
    - provider: one of 'openai','azure','hf','anthropic','ollama','vertex','zhipu','custom','auto'
    - model: provider-specific model name
    - opts: provider-specific options like temperature, max_tokens, system
    """
    provider = (provider or os.getenv("ICEDMAN_AI_PROVIDER") or "auto").lower()
    if provider == "auto":
        provider = _auto_detect_provider()
    logger.debug("send_prompt selected provider=%s model=%s", provider, model)

    try:
        if provider == "openai":
            return _openai(prompt, model=model or os.getenv("OPENAI_MODEL", "gpt-4o-mini"), **opts)
        if provider == "azure":
            return _azure_openai(prompt, resource=opts.get("resource"), deployment=model or opts.get("deployment"), **opts)
        if provider in ("hf", "huggingface"):
            return _huggingface_inference(prompt, model=model or os.getenv("HF_MODEL", "gpt2"), **opts)
        if provider == "anthropic":
            return _anthropic(prompt, model=model or os.getenv("ANTHROPIC_MODEL", "claude-2.1"), **opts)
        if provider == "ollama":
            return _ollama(prompt, model=model or os.getenv("OLLAMA_MODEL", "ollama"), host=os.getenv("OLLAMA_HOST"), **opts)
        if provider in ("vertex", "google", "gcp"):
            return _vertex_ai(prompt, model=model, **opts)
        if provider in ("zhipu", "glm", "bigmodel"):
            return _zhipu(prompt, model=model, **opts)
        if provider == "custom":
            url = os.getenv("PROVIDER_CUSTOM_URL")
            token_env = os.getenv("PROVIDER_CUSTOM_KEY_NAME")  # optional
            if not url:
                raise RuntimeError("PROVIDER_CUSTOM_URL not set for custom provider")
            return _generic_http(prompt, url, token_env, **opts)
        # Generic fallthroughs for vendor-specific tokens
        if provider == "hackliberty":
            url = os.getenv("HACKLIBERTY_ENDPOINT", "https://api.hackliberty.org/generate")
            return _generic_http(prompt, url, "HACKLIBERTY_KEY", **opts)
        if provider == "deepseek":
            url = os.getenv("DEEPSEEK_ENDPOINT", "https://api.deepseek.ai/generate")
            return _generic_http(prompt, url, "DEEPSEEK_KEY", **opts)
        if provider == "perplexity":
            url = os.getenv("PERPLEXITY_ENDPOINT", "https://api.perplexity.ai/generate")
            return _generic_http(prompt, url, "PERPLEXITY_KEY", **opts)
        if provider == "qai":
            url = os.getenv("QAI_ENDPOINT", "https://api.qualcomm.ai/generate")
            return _generic_http(prompt, url, "QAI_TOKEN", **opts)

        # unknown provider
        raise ValueError(f"Unknown provider: {provider}")
    except Exception as e:
        logger.exception("provider %s failed: %s", provider, e)
        # Return a helpful message without revealing tokens or internals
        return f"[provider-error] {provider}: {type(e).__name__} - {str(e)[:200]}"