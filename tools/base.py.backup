# -*- coding: utf-8 -*-
from __future__ import annotations
from abc import ABC, abstractmethod
from typing import Optional
from ..types import BotMessage, HeatLevel, BotType, MessageHandler

class BaseBot(ABC):
    def __init__(self, bot_type: BotType) -> None:
        self.bot_type = bot_type
        self._handler: Optional[MessageHandler] = None

    def set_message_handler(self, h: MessageHandler) -> None:
        self._handler = h

    def emit(self, msg: BotMessage) -> None:
        if self._handler:
            self._handler(msg)

    @abstractmethod
    def activate(self, context: str, heat: HeatLevel) -> str:
        ...

    @abstractmethod
    def receive(self, msg: BotMessage) -> None:
        ...

# References:
# - /reference vault (bot interface contracts)