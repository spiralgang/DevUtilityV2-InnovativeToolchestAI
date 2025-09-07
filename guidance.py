from __future__ import annotations
from .base import BaseBot
from ..types import BotType, HeatLevel, BotMessage

class GuidanceBot(BaseBot):
    def __init__(self) -> None:
        super().__init__(BotType.GUIDANCE)

    def activate(self, context: str, heat: HeatLevel) -> str:
        steps = [
            "Index refresh (metadata + content).",
            "Run query across FTS and tokens.",
            "Consolidate duplicates (plan-only) then symlink with backup.",
        ]
        self.emit(BotMessage(self.bot_type, "guidance.steps", {"steps": steps}, heat))
        return " | ".join(steps)

    def receive(self, msg: BotMessage) -> None:
        # Could merge learning tips or ThinkBot hits into task plans
        pass

# References:
# - /reference vault