# -*- coding: utf-8 -*-
# Living Code Integration - Auto-generated symmetrical connections
# This file is part of the SrirachaArmy Living Code Environment
# Perfect symmetrical integration with all repository components

# -*- coding: utf-8 -*-
"""
DroneAgent: small, robust wrapper around pymavlink for mission commands.

- Graceful degrade to simulator mode if pymavlink not present or DRONE_SIMULATE=1.
- Non-blocking: uses internal retries and logs events.
- Designed to be invoked by EnergeticWeave micro-agents.
"""
import logging
import time
from typing import Optional
from . import config

logger = logging.getLogger("iceman_drone_tool.drone_controller")

try:
    from pymavlink import mavutil  # optional
    HAS_PYMAV = True
except Exception:
    HAS_PYMAV = False
    mavutil = None


class DroneAgent:
    def __init__(self, port: Optional[str] = None, baud: Optional[int] = None, timeout: int = 15):
        self.port = port or config.DRONE_MAV_PORT
        self.baud = baud or config.DRONE_MAV_BAUD
        self.timeout = timeout
        self.master = None
        if config.DRONE_SIMULATE or not HAS_PYMAV:
            logger.warning("DroneAgent running in SIMULATE mode (no pymavlink or DRONE_SIMULATE enabled).")
            self.simulate = True
        else:
            self.simulate = False
        self._connect()

    def _connect(self):
        if self.simulate:
            logger.debug("Simulated connect to %s @%s", self.port, self.baud)
            self.master = None
            return
        try:
            logger.info("Connecting to MAVLink on %s @ %d", self.port, self.baud)
            self.master = mavutil.mavlink_connection(self.port, baud=self.baud, autoreconnect=True)
            self.master.wait_heartbeat(timeout=self.timeout)
            logger.info("Heartbeat OK from system %s comp %s", self.master.target_system, self.master.target_component)
        except Exception as e:
            logger.exception("Failed to connect to MAVLink: %s", e)
            self.master = None

    def _ensure(self):
        if self.simulate:
            return True
        if self.master is None:
            self._connect()
        return self.master is not None

    def arm_and_takeoff(self, target_alt_m: float = 5.0):
        if not self._ensure():
            logger.error("No MAVLink connection; abort arm_and_takeoff")
            return False
        if self.simulate:
            logger.info("[SIM] arm_and_takeoff -> %.1fm", target_alt_m)
            return True
        try:
            self.master.arducopter_arm()
            # small sleep to let arming settle
            time.sleep(1)
            self.master.mav.command_long_send(
                self.master.target_system, self.master.target_component,
                mavutil.mavlink.MAV_CMD_NAV_TAKEOFF, 0, 0, 0, 0, 0, 0, 0, float(target_alt_m)
            )
            logger.info("Takeoff commanded -> %.1fm", target_alt_m)
            return True
        except Exception as e:
            logger.exception("Takeoff failed: %s", e)
            return False

    def goto(self, lat: float, lon: float, alt: float):
        if not self._ensure():
            logger.error("No MAVLink connection; abort goto")
            return False
        if self.simulate:
            logger.info("[SIM] goto -> %f, %f, %f", lat, lon, alt)
            return True
        try:
            self.master.mav.mission_item_send(
                self.master.target_system, self.master.target_component,
                0, mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT,
                mavutil.mavlink.MAV_CMD_NAV_WAYPOINT, 2, 0,
                0, 0, 0, 0, float(lat), float(lon), float(alt)
            )
            logger.info("Waypoint sent: %s,%s @%sm", lat, lon, alt)
            return True
        except Exception as e:
            logger.exception("Goto failed: %s", e)
            return False

    def land(self):
        if not self._ensure():
            logger.error("No MAVLink connection; abort land")
            return False
        if self.simulate:
            logger.info("[SIM] land")
            return True
        try:
            self.master.mav.command_long_send(
                self.master.target_system, self.master.target_component,
                mavutil.mavlink.MAV_CMD_NAV_LAND, 0, 0, 0, 0, 0, 0, 0, 0
            )
            logger.info("Land commanded")
            return True
        except Exception as e:
            logger.exception("Land failed: %s", e)
            return False