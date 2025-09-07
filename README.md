# Local Index Bots (Tech.Ula)

Rationale
- Eliminate remote LLM/API. Use your indexed 13 GB UserLAnd data as ground truth.
- Multiple bots, zero cross-contamination: each bot gets its own index view, roots, and policies.
- Modular retrieval: exact-dup (SHA-256), full-text (FTS5), near-dup (SimHash), and CDC chunk overlap. Router picks the best index per query.
- Incremental growth: reuses your techula_index.db and content indices; cheap polling or on-demand refresh.
- Android 10+/UserLAnd compatible: stdlib sqlite, async TG bot, optional local LLM.

Quick Start (uv)
- uv sync --all-extras --all-groups --locked
- Export the state (index) dir:
  - export STATE_DIR=/path/to/state  # where techula_index.db lives (from your caretaker)
- Configure bots:
  - cp config/bots.example.yaml config/bots.yaml and edit tokens/roots.
- Run Telegram bot (long polling):
  - uv run -p 3.13 server/run_bot.py
- Run HTTP API (for local apps):
  - uv run -p 3.13 server/app.py

Notes
- No LLM installed: the bot answers by extractive retrieval (ranked snippets from your corpus).
- Optional local LLM: set llm.provider to "llama_cpp" or "transformers" in config; drops in automatically.

Folders
- config/: per-bot isolation and index views.
- index/: FTS5, router, and adapters over your existing state/DB.
- bot/: retrieval, reranking, and answer composition; pluggable LLM.
- server/: Telegram runner and FastAPI app.
- scripts/: helpers (health check).

References
- /reference vault
- SQLite FTS5
- python-telegram-bot async architecture