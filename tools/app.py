# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# -*- coding: utf-8 -*-
from __future__ import annotations
import os, yaml
from typing import Dict, Any
from fastapi import FastAPI
from pydantic import BaseModel

from bot.local_brain import LocalBrain, BotView
from bot.llm import LLMConfig

CONFIG_PATH = os.environ.get("BOTS_CONFIG", "config/bots.yaml")
STATE_DIR = os.environ.get("STATE_DIR", "state")

def load_config() -> Dict[str, Any]:
    with open(CONFIG_PATH, "r", encoding="utf-8") as fh:
        return yaml.safe_load(fh)

def make_view(name: str, spec: Dict[str, Any]) -> BotView:
    llm = spec.get("llm", {})
    return BotView(
        name=name,
        allow_roots=spec.get("allow_roots", []),
        include_ext=spec.get("filters", {}).get("include_ext", []),
        exclude_glob=spec.get("filters", {}).get("exclude_glob", []),
        indices=spec.get("indices", []),
        llm=LLMConfig(
            provider=llm.get("provider", "none"),
            model=llm.get("model", ""),
            max_new_tokens=int(llm.get("max_new_tokens", 256)),
        ),
    )

cfg = load_config()
views: Dict[str, BotView] = {n: make_view(n, s) for n, s in cfg.get("bots", {}).items()}
brains: Dict[str, LocalBrain] = {n: LocalBrain(STATE_DIR, v) for n, v in views.items()}

class Query(BaseModel):
    bot: str
    q: str
    top_k: int = 6

app = FastAPI()

@app.get("/health")
def health():
    return {"ok": True, "bots": list(views.keys())}

@app.post("/query")
def query(payload: Query):
    bot = payload.bot
    if bot not in brains:
        return {"error": f"unknown bot: {bot}"}
    ans = brains[bot].answer(payload.q, payload.top_k)
    return {"bot": bot, "answer": ans}

# References:
# - /reference vault (FastAPI usage for local integrations)