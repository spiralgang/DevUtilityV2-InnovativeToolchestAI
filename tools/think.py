# -*- coding: utf-8 -*-
from __future__ import annotations
from typing import List
from .base import BaseBot
from ..types import BotType, HeatLevel, BotMessage
from ..adapters.index_adapter import LocalIndex

class ThinkBot(BaseBot):
    def __init__(self, index: LocalIndex) -> None:
        super().__init__(BotType.THINK)
        self.index = index

    def activate(self, context: str, heat: HeatLevel) -> str:
        q = context if context.strip() else "optimization OR design"
        hits = self.index.search(q, limit=6 if heat != HeatLevel.HIGH else 12)
        if not hits:
            return "No relevant context in local corpus."
        self.emit(BotMessage(self.bot_type, "context.topics", {"query": q, "hits": hits}, heat))
        return f"Found {len(hits)} relevant docs. Query='{q}'."

    def receive(self, msg: BotMessage) -> None:
        # react to guidance or learning outputs (placeholder)
        if msg.topic.startswith("guidance."):
            pass

# References:
# - /reference vault