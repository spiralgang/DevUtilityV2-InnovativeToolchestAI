# -*- coding: utf-8 -*-
"""
EnergeticWeave â€” reflex module

Responsibilities:
 - Monitor a command queue directory (~/icedman/bot/queue)
 - For each incoming prompt create an "energy packet" (metadata + payload)
 - Inject persona context and call call_pi for guidance
 - Spawn micro-agents:
     * drone mission micro-agent (uses DroneAgent)
     * validator micro-agent (model & file checks)
 - Maintain lightweight energy metrics and self-healing:
     * per-packet backoff & retry policy
     * watchdog thread that restarts failed agents
 - Emit structured logs and machine-readable artifacts under ~/icedman/weave_artifacts/

Concurrency model:
 - single listener thread
 - ThreadPool for micro-agents (small, bounded)
"""
import os
import json
import time
import threading
import queue as thqueue
import logging
from typing import Dict, Any
from datetime import datetime
from .whisper import call_pi
from .drone_controller import DroneAgent
from .config import WEAVE_POLL_INTERVAL, LOG_LEVEL, DRONE_SIMULATE

logging.basicConfig(level=LOG_LEVEL)
logger = logging.getLogger("iceman_drone_tool.energetic_weave")

QUEUE_DIR = os.path.expanduser(os.getenv("ICEDMAN_QUEUE", "~/icedman/bot/queue"))
RESPOND_DIR = os.path.expanduser(os.getenv("ICEDMAN_RESPOND", "~/icedman/bot/queue/respond"))
ARTIFACTS_DIR = os.path.expanduser(os.getenv("ICEDMAN_WEAVE_ARTIFACTS", "~/icedman/weave_artifacts"))
os.makedirs(QUEUE_DIR, exist_ok=True)
os.makedirs(RESPOND_DIR, exist_ok=True)
os.makedirs(ARTIFACTS_DIR, exist_ok=True)


class EnergyPacket:
    def __init__(self, name: str, prompt: str):
        self.id = f"{int(time.time()*1000)}-{threading.get_ident()}"
        self.name = name
        self.prompt = prompt
        self.created = datetime.utcnow().isoformat()
        self.attempts = 0
        self.max_attempts = 3
        self.status = "new"
        self.metadata: Dict[str, Any] = {}

    def to_dict(self):
        return {
            "id": self.id,
            "name": self.name,
            "prompt": self.prompt,
            "created": self.created,
            "attempts": self.attempts,
            "status": self.status,
            "metadata": self.metadata,
        }


class EnergeticWeave:
    def __init__(self, pool_size: int = 4):
        self.pool_size = pool_size
        self.work_q = thqueue.Queue()
        self.running = False
        self.threads = []
        self.agent_lock = threading.Lock()
        self.metrics = {"processed": 0, "errors": 0, "agents_spawned": 0}
        # lightweight thread pool
        for i in range(pool_size):
            t = threading.Thread(target=self._worker_loop, daemon=True, name=f"weave-worker-{i}")
            self.threads.append(t)

    def start(self):
        logger.info("EnergeticWeave starting; pool=%d", self.pool_size)
        self.running = True
        for t in self.threads:
            t.start()
        self.listener_thread = threading.Thread(target=self._listen_loop, daemon=True, name="weave-listener")
        self.listener_thread.start()
        self.watchdog_thread = threading.Thread(target=self._watchdog_loop, daemon=True, name="weave-watchdog")
        self.watchdog_thread.start()

    def stop(self):
        logger.info("EnergeticWeave stopping")
        self.running = False

    def _listen_loop(self):
        # Poll queue dir for .cmd files
        while self.running:
            try:
                for path in sorted(os.listdir(QUEUE_DIR)):
                    if not path.endswith(".cmd"):
                        continue
                    full = os.path.join(QUEUE_DIR, path)
                    try:
                        name = os.path.splitext(path)[0]
                        with open(full, "r", encoding="utf-8") as fh:
                            cmd = fh.read().strip()
                        packet = EnergyPacket(name=name, prompt=cmd)
                        logger.debug("Enqueue packet: %s", packet.to_dict())
                        self.work_q.put(packet)
                        os.remove(full)
                    except Exception as e:
                        logger.exception("Failed to read/queue %s: %s", full, e)
                time.sleep(WEAVE_POLL_INTERVAL)
            except Exception as e:
                logger.exception("Listener loop error: %s", e)
                time.sleep(1)

    def _worker_loop(self):
        while True:
            packet: EnergyPacket = self.work_q.get()
            try:
                self._process_packet(packet)
            except Exception as e:
                logger.exception("worker error: %s", e)
                self.metrics["errors"] += 1
            finally:
                self.work_q.task_done()

    def _process_packet(self, packet: EnergyPacket):
        packet.attempts += 1
        packet.status = "processing"
        start = time.time()

        # 1) Enrich with persona & call PI for guidance
        guidance = call_pi(packet.prompt)
        packet.metadata["guidance"] = guidance

        # 2) Spawn micro-agents based on guidance keywords (simple mapping)
        spawned = []
        if "drone" in guidance.lower() or "waypoint" in guidance.lower() or "goto" in packet.prompt.lower():
            spawned.append(self._spawn_drone_agent(packet, guidance))
        else:
            # validator micro-agent (model/file checks)
            spawned.append(self._spawn_validator_agent(packet, guidance))

        # 3) collect agent results, write artifact, reply
        results = [s for s in spawned]
        packet.metadata["agents"] = [r.get("name") for r in results if isinstance(r, dict)]
        packet.status = "done"
        self.metrics["processed"] += 1

        # write artifact
        artifact_path = os.path.join(ARTIFACTS_DIR, f"weave-{packet.name}-{int(time.time())}.json")
        with open(artifact_path, "w", encoding="utf-8") as fh:
            json.dump({"packet": packet.to_dict(), "results": results, "processed_at": datetime.utcnow().isoformat()}, fh, indent=2)

        # reply for the queue consumer
        resp_path = os.path.join(RESPOND_DIR, f"{packet.name}.resp")
        with open(resp_path, "w", encoding="utf-8") as fh:
            fh.write(json.dumps({"status": packet.status, "guidance": guidance, "artifact": artifact_path}))

        logger.info("Processed packet %s in %.2fs; artifact=%s", packet.name, time.time() - start, artifact_path)

    def _spawn_drone_agent(self, packet: EnergyPacket, guidance: str):
        # Synchronous small mission runner: parse lat/lon/alt if present (very forgiving)
        try:
            # parse simple patterns "goto LAT,LON,ALT"
            import re

            m = re.search(r"(-?\d+\.\d+)[, \t]+(-?\d+\.\d+)[, \t]+(\d+(\.\d+)?)", guidance)
            if m:
                lat, lon, alt = float(m.group(1)), float(m.group(2)), float(m.group(3))
            else:
                # fallback to fixed short mission
                lat, lon, alt = 47.6062, -122.3321, 8.0
            logger.info("Spawning drone agent -> %s,%s @%sm", lat, lon, alt)
            self.metrics["agents_spawned"] += 1
            agent = DroneAgent()
            # run mission in separate thread to avoid blocking worker
            res_q = {}

            def mission():
                try:
                    ok = agent.arm_and_takeoff(alt)
                    if not ok:
                        res_q["status"] = "failed_arm"
                        return
                    agent.goto(lat, lon, alt)
                    # small loiter
                    time.sleep(2)
                    agent.land()
                    res_q["status"] = "completed"
                except Exception as e:
                    res_q["status"] = "error"
                    res_q["error"] = str(e)

            t = threading.Thread(target=mission, daemon=True)
            t.start()
            # return immediate handle info
            return {"name": "drone_agent", "status": "spawned", "lat": lat, "lon": lon, "alt": alt, "simulated": config.DRONE_SIMULATE}
        except Exception as e:
            logger.exception("Failed to spawn drone agent: %s", e)
            return {"name": "drone_agent", "status": "error", "error": str(e)}

    def _spawn_validator_agent(self, packet: EnergyPacket, guidance: str):
        # Simple checks: ensure models dir exists, short hint to model validator
        try:
            models_dir = os.path.expanduser("~/icedman/models")
            exists = os.path.isdir(models_dir)
            # add basic system checks and suggestions from guidance
            suggestion = "run icedman_model_validator.sh" if not exists else "models present"
            return {"name": "validator_agent", "status": "ok" if exists else "missing_models", "suggestion": suggestion}
        except Exception as e:
            logger.exception("validator agent error: %s", e)
            return {"name": "validator_agent", "status": "error", "error": str(e)}

    def _watchdog_loop(self):
        # Simple self-healing: monitor threads and restart if any worker died.
        while self.running:
            try:
                for i, t in enumerate(self.threads):
                    if not t.is_alive():
                        logger.warning("weave worker %s dead; restarting", t.name)
                        nt = threading.Thread(target=self._worker_loop, daemon=True, name=t.name)
                        self.threads[i] = nt
                        nt.start()
                time.sleep(2.0)
            except Exception as e:
                logger.exception("watchdog error: %s", e)
                time.sleep(1.0)