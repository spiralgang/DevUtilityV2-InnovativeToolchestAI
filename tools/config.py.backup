# -*- coding: utf-8 -*-
"""
Runtime configuration for iceman_drone_tool.

Read-only constants and env helpers. Do not hardcode secrets here;
read them via environment variables in the workflow or device config.
"""
import os

# MAVLink / drone device
DRONE_MAV_PORT = os.getenv("DRONE_MAV_PORT", "/dev/ttyS1")
DRONE_MAV_BAUD = int(os.getenv("DRONE_MAV_BAUD", "57600"))

# Pi / model endpoint (recommended set via repo secret or device env)
PI_ENDPOINT = os.getenv("PI_ENDPOINT", "https://layercake.pubwestus3.inf7ks8.com/external/api/inference")
# Token name in the environment; callers should set INFLECTION_KEY or GEMINI
PI_TOKEN_ENV_NAMES = ("INFLECTION_KEY", "GEMINI", "GEMINI_API_KEY")

PERSONA_FILE = os.path.expanduser(os.getenv("PERSONA_FILE", "~/icedman/current_persona.txt"))

# Operational knobs
DRONE_SIMULATE = os.getenv("DRONE_SIMULATE", "false").lower() in ("1", "true", "yes")
WEAVE_POLL_INTERVAL = float(os.getenv("WEAVE_POLL_INTERVAL", "1.5"))
LOG_LEVEL = os.getenv("ICEDMAN_LOG_LEVEL", "INFO")