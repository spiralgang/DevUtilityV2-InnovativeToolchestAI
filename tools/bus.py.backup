# -*- coding: utf-8 -*-
from __future__ import annotations
import threading
from typing import Dict, List, Callable
from .types import BotMessage, BotType

class AsyncMessageBus:
    """
    Lightweight thread-based async bus (portable on Android/UserLAnd).
    Subscribers are invoked in separate background threads.
    """
    def __init__(self) -> None:
        self._subs: Dict[BotType, List[Callable[[BotMessage], None]]] = {}

    def subscribe(self, bot: BotType, handler: Callable[[BotMessage], None]) -> None:
        self._subs.setdefault(bot, []).append(handler)

    def publish(self, msg: BotMessage, targets: List[BotType]) -> None:
        for t in targets:
            for h in self._subs.get(t, []):
                threading.Thread(target=h, args=(msg,), daemon=True).start()

# References:
# - /reference vault (simple, portable async patterns)