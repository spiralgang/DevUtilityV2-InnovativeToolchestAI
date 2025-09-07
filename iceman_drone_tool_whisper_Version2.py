"""
call_pi -> unified AI provider bridge

Replaces older Inflection-only wrapper.
Now routes to iceman_drone_tool.providers.send_prompt which supports many free-tier
and local providers. Keeps tokens in env and does not print secrets.

Environment knobs (examples)
- ICEDMAN_AI_PROVIDER=auto|openai|azure|hf|anthropic|ollama|vertex|zhipu|custom
- OPENAI_API_KEY, AZURE_OPENAI_KEY, HUGGINGFACE_API_KEY, ANTHROPIC_API_KEY, OLLAMA_HOST, OLLAMA_API_KEY, ZHIPU_API_KEY, PROVIDER_CUSTOM_URL
- Optional model names: OPENAI_MODEL, HF_MODEL, ANTHROPIC_MODEL, OLLAMA_MODEL, ZHIPU_MODEL
"""
import logging
from .config import PERSONA_FILE
from .providers import send_prompt

logger = logging.getLogger("iceman_drone_tool.whisper")

def _read_persona():
    try:
        with open(PERSONA_FILE, "r", encoding="utf-8") as fh:
            return fh.read().strip()
    except Exception:
        return "icedman"

def call_pi(prompt: str, provider: str = None, model: str = None, timeout: float = 10.0) -> str:
    """
    High-level call to any configured provider.
    - provider: optional override (see providers.send_prompt)
    - model: provider-specific model name
    - timeout: advisory only; adapters have internal timeouts
    Returns: best-effort single-line guidance (string); internal errors returned as [provider-error] tags.
    """
    persona = _read_persona()
    # Compose a persona-aware prompt wrapper for higher-signal responses
    composite = f"Persona: {persona}\n\nTask: {prompt}\n\nPlease reply with concise guidance."
    try:
        # send_prompt handles provider selection & error-wrapping
        res = send_prompt(composite, provider=provider or None, model=model, temperature=0.0, max_tokens=1024)
        # Normalize to a string; ensure we return concise output
        if isinstance(res, str):
            return res.strip()
        # fallback stringify
        return str(res)
    except Exception as e:
        logger.exception("call_pi failed: %s", e)
        return f"[pi-error] {type(e).__name__}: check config"