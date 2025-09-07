```markdown
# Puter.js integration — concise guide

Summary
- A minimal, client-first Puter integration plus a secure server fallback.
- Client path: integrations/nnimm_v2/puter-integration.html — tries Puter client first.
- Server path: src/server/puter-proxy.js — uses OPENAI_API_KEY to call OpenAI when configured.

Design choices (short)
- Single demo file to avoid duplication and reviewer fatigue.
- Feature flag: window.FEAT_PUTER toggles client usage without code changes.
- Fallback returns 502 when no server key so client can handle user-pay flows.

How to enable
- For client demos only: ensure integrations/nnimm_v2/puter-integration.html is served and the Puter script is reachable.
- For production-safe fallback: set environment variable OPENAI_API_KEY and register the proxy in your Express server:
  require('./src/server/puter-proxy')(app)

Security & policy (short)
- Never put official API keys in client code.
- Evaluate Puter terms and privacy. Inform users clearly who pays, and require explicit opt-in if users will be billed via Puter.
- Protect server endpoint with authentication, rate-limiting, and monitoring.
- Consider telemetry to detect failed Puter calls and fallback rates.

Testing checklist (short)
- Client-only: load example while Puter script is available; verify responses.
- No server key: verify example falls back with 502 message and instructs client to use Puter.
- With OPENAI_API_KEY: start server, POST /api/ai/chat with { prompt, model } and verify JSON { text } response.
- Smoke: validate error paths (network failure, invalid prompt).

Acceptance criteria
- Demo runs with Puter when client script is reachable.
- Server returns a clear 502 when no OPENAI_API_KEY (so clients can route to Puter).
- Documentation added and integration gated behind feature flag.

References
- Puter docs (canonical): confirm js.puter.com/v2 before production.
- OpenAI API docs: https://platform.openai.com/docs
```