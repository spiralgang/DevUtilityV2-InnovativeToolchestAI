# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# -*- coding: utf-8 -*-
from __future__ import annotations
from dataclasses import dataclass
from typing import Dict, Any, List
from .types import BotType, HeatLevel, BotMessage
from .bus import AsyncMessageBus
from .adapters.index_adapter import LocalIndex
from .adapters.qdataset_adapter import QDataSetAdapter
from .bots.think import ThinkBot
from .bots.learn import LearnBot
from .bots.guidance import GuidanceBot
from .bots.webintel import WebIntelBot

@dataclass
class BotSpec:
    allow_roots: List[str]
    include_ext: List[str]
    exclude_glob: List[str]

class Orchestrator:
    """
    Central coordinator, mirroring SrirachaArmy patterns:
    - Activate bots with context + heat
    - Establish inter-bot messaging via bus
    - Broadcast based on relevance
    """
    def __init__(self, state_dir: str, specs: Dict[BotType, BotSpec]) -> None:
        self.bus = AsyncMessageBus()
        # Adapters
        think_index = LocalIndex(state_dir, specs[BotType.THINK].allow_roots, specs[BotType.THINK].include_ext, specs[BotType.THINK].exclude_glob)
        self.qd = QDataSetAdapter()

        # Bots
        self.think = ThinkBot(think_index)
        self.learn = LearnBot()
        self.guidance = GuidanceBot()
        self.webintel = WebIntelBot(self.qd)

        # Wire message handlers (broadcast model)
        self.think.set_message_handler(lambda m: self._broadcast(m, sender=BotType.THINK))
        self.learn.set_message_handler(lambda m: self._broadcast(m, sender=BotType.LEARN))
        self.guidance.set_message_handler(lambda m: self._broadcast(m, sender=BotType.GUIDANCE))
        self.webintel.set_message_handler(lambda m: self._broadcast(m, sender=BotType.WEBINTEL))

        # Bus subscriptions
        self.bus.subscribe(BotType.THINK, self.think.receive)
        self.bus.subscribe(BotType.LEARN, self.learn.receive)
        self.bus.subscribe(BotType.GUIDANCE, self.guidance.receive)
        self.bus.subscribe(BotType.WEBINTEL, self.webintel.receive)

    def _broadcast(self, msg: BotMessage, sender: BotType) -> None:
        targets = [bt for bt in BotType if bt != sender]
        self.bus.publish(msg, targets)

    def activate_bot(self, bot: BotType, context: str, heat: HeatLevel) -> str:
        if bot == BotType.THINK:
            return self.think.activate(context, heat)
        if bot == BotType.LEARN:
            return self.learn.activate(context, heat)
        if bot == BotType.GUIDANCE:
            return self.guidance.activate(context, heat)
        if bot == BotType.WEBINTEL:
            return self.webintel.activate(context, heat)
        return "Unknown bot."

# References:
# - /reference vault (Sriracha Orchestrator analog)