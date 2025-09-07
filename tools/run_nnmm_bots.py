#!/usr/bin/env python3
from __future__ import annotations
import os, sys, json
from typing import Dict, Any
from nnmm_orchestrator.orchestrator import Orchestrator, BotSpec
from nnmm_orchestrator.types import BotType, HeatLevel

def load_config(path: str) -> Dict[str, Any]:
    if path.endswith(".json"):
        with open(path, "r", encoding="utf-8") as fh:
            return json.load(fh)
    try:
        import yaml  # optional
        with open(path, "r", encoding="utf-8") as fh:
            return yaml.safe_load(fh)
    except Exception:
        raise SystemExit("Install PyYAML or provide JSON config.")

def main():
    import argparse
    ap = argparse.ArgumentParser()
    ap.add_argument("--config", required=True)
    ap.add_argument("--bot", choices=[b.name for b in BotType], default="THINK")
    ap.add_argument("--heat", choices=[h.name for h in HeatLevel], default="MEDIUM")
    ap.add_argument("--context", default="")
    args = ap.parse_args()

    cfg = load_config(args.config)
    state_dir = cfg.get("state_dir") or os.environ.get("STATE_DIR")
    if not state_dir:
        raise SystemExit("Provide state_dir in config or set STATE_DIR env var.")

    specs: Dict[BotType, BotSpec] = {}
    for btype in BotType:
        bcfg = (cfg.get("bots") or {}).get(btype.name, {}) or {}
        specs[btype] = BotSpec(
            allow_roots=bcfg.get("allow_roots", []),
            include_ext=bcfg.get("include_ext", []),
            exclude_glob=bcfg.get("exclude_glob", []),
        )

    orch = Orchestrator(state_dir, specs)
    bot = BotType[args.bot]
    heat = HeatLevel[args.heat]
    out = orch.activate_bot(bot, args.context, heat)
    print(f"{bot.name}@{heat.name}: {out}")

if __name__ == "__main__":
    main()

# References:
# - /reference vault (CLI wiring; Android portability)