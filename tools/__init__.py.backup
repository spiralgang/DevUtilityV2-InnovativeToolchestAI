# -*- coding: utf-8 -*-
"""
iceman_drone_tool
Light adapter package for ICEDMAN drone & Pi interaction.

Exports:
  - DroneAgent (drone_controller)
  - call_pi (whisper)
  - EnergeticWeave (energetic_weave)

Design notes:
- Pure-Python, stdlib-first. Optional deps (pymavlink, requests) loaded lazily
  so the package can be inspected on-device without full native toolchain.
- All external tokens read from environment; never printed.
"""
from .drone_controller import DroneAgent
from .whisper import call_pi
from .energetic_weave import EnergeticWeave

__all__ = ["DroneAgent", "call_pi", "EnergeticWeave"]