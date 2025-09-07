# NNMM Orchestrator (Sriracha-style, Local-First)

Rationale
- Mirrors SrirachaArmy patterns (orchestrator, multi-bot messaging, “heat levels,” context activation) in Python.
- Local-only by default: queries your Tech.Ula state SQLite index; no external APIs.
- Isolation: each bot has allowlist roots/filters to avoid cross-contamination.
- Optional: quantum datasets via QDataSet (if installed); quantum-git metadata for parallel “mind states”.

Key Ideas Adopted From DevUtility Sriracha
- Central orchestrator coordinating multiple specialized bots.
- Inter-bot messaging bus with selective broadcast/delivery.
- Heat level tuning for aggressiveness/depth.
- Context-aware activation (terminal/index context in DevUtility → local index/data context here).

Quick Start
1) Place this package in your NNMM repo root (or as a sibling utility repo).
2) Ensure your Tech.Ula index exists: state/techula_index.db with content_fts or tokens (from your caretaker).
3) Configure bots:
   cp config/nnmm-bots.example.yaml config/nnmm-bots.yaml
4) Run:
   STATE_DIR=/absolute/path/to/state python3 scripts/run_nnmm_bots.py --config config/nnmm-bots.yaml

What You Get
- Orchestrator, message bus, and 4 bots:
  - ThinkBot (analysis)
  - LearnBot (pattern learning hints)
  - GuidanceBot (actionable steps)
  - WebIntelBot (local-doc “web intel”; can be extended)
- Local index adapter (FTS5 with tokens fallback).
- Optional QDataSet adapter (loads quantum datasets if library is present).
- Quantum-inspired version control metadata (quantumgit.py) to track parallel “mind” states.

Android 10 Notes
- Pure stdlib except optional PyYAML; falls back to JSON if YAML not available.
- No inotify; your caretaker’s poller keeps the index fresh.
- No symlink assumptions outside your visible tree.

References
- /reference vault (Sriracha orchestration patterns, SQLite FTS5, Android 10 constraints, PRoot bind semantics)
- DevUtility Sriracha sources (bot orchestration, inter-bot comms, heat levels)
- eperrier/QDataSet (optional quantum datasets)