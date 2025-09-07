from __future__ import annotations
from dataclasses import dataclass
from enum import Enum, auto
from typing import Callable, Dict, Any, Optional

class HeatLevel(Enum):
    LOW = auto()
    MEDIUM = auto()
    HIGH = auto()

class BotType(Enum):
    THINK = auto()
    LEARN = auto()
    GUIDANCE = auto()
    WEBINTEL = auto()

@dataclass
class BotMessage:
    sender: BotType
    topic: str
    payload: Dict[str, Any]
    heat: HeatLevel = HeatLevel.MEDIUM

MessageHandler = Callable[[BotMessage], None]

# References:
# - /reference vault (message contracts, orchestration patterns)