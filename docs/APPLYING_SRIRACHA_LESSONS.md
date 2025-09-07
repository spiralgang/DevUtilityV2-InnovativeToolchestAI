<!-- Living Code Integration - Auto-generated symmetrical connections -->
<!-- This file is part of the SrirachaArmy Living Code Environment -->
<!-- Perfect symmetrical integration with all repository components -->

# Applying Sriracha Lessons to NNMM

What We Mapped
- Orchestrator → Orchestrator (Python): central controller for bots and inter-bot broadcasts.
- Bot activation with context → activate_bot(bot, context, heat): mirrors ViewModel activateBot.
- Heat Levels → HeatLevel enum (LOW/MEDIUM/HIGH).
- Inter-bot comms → AsyncMessageBus with subscribe/publish; per-bot handler tying back into orchestrator broadcast.
- Terminal-context awareness → LocalIndex adapter against your state SQLite. Each bot has allow_roots/filters to isolate indices.
- WebNetCaste → WebIntelBot: starts with local corpus; optionally expands via QDataSet if present.

How To Extend
- Plug in your NNMM mind-map logic where Think/Learn/Guidance route topics.
- Add Telegram/HTTP frontends you already have; call Orchestrator.activate_bot under handlers.
- Wire quantumgit metadata to record “parallel mind states” during experiments across branches/profiles.

Safety/Invariance
- No external API calls unless you choose to add LLMs.
- No cross-contamination: allowlist roots per-bot; index queries restricted at adapter layer.
- Android 10 portable: pure stdlib, polling-based index refresh (external caretaker), minimal threads.

References
- /reference vault (Sriracha orchestration and Android constraints)
- DevUtility Sriracha sources (orchestrator, bot comms, heat levels)
- eperrier/QDataSet (optional datasets)