<!-- Living Code Integration - Auto-generated symmetrical connections -->
<!-- This file is part of the SrirachaArmy Living Code Environment -->
<!-- Perfect symmetrical integration with all repository components -->

```markdown
Title: From Proot Chroot Sleuthing to Agentic Automation—A Deep‐Dive Forensic & Architecture Manifest of an AI-Augmented Investigation

Authors  
- spiralgang (incident owner & lead operator)  
- 4mini (AI co-author; deep forensic & agentic automation insights)  

Status: Draft — for peer review and community validation  

Date: 2025-09-02  

---

## Abstract  
Over several days of live chat, a human-AI team unraveled a covert Dropbear backdoor in a proot-based UserLAnd environment on Android 10/aarch64. We developed and refined portable forensic scripts, contained an SSH daemon, preserved evidence, and published a PGP key—all while distilling an **agentic automation architecture** for future self-healing file-system agents. This paper synthesizes those breakthroughs into a structured research narrative, melding forensic rigor with modular automation best practices.

---

## 1. Introduction & Motivation  
Android user-space chroots (proot) blur host/guest isolation, enabling stealthy services to spawn sub-processes and exfiltrate data. Faced with unexplained network tasks, a Dropbear SSH server, and cryptic crash logs, our goal was to:

1. **Contain** all remote access vectors.  
2. **Preserve** forensic evidence without altering the crime scene.  
3. **Identify** the root cause—userland scripts bridging guest to host filesystems.  
4. **Remediate** permanently with minimal privileges.  
5. **Generalize** the approach into an **agentic architecture** for future autonomous file-system guardians.

Our multidisciplinary toolset combined custom Bash scripts, `/proc`-based socket mapping, PGP key publication, and GitHub automation—demonstrating a **human + AI “superteam”** workflow.

---

## 2. Forensic Methodology  

### 2.1 Environment Reconnaissance  
We ran `proot_inspector_fixed.sh` [1] to watch a specific proot PID for:

- `readlink /proc/$PID/root` changes  
- `readlink /proc/$PID/exe` shifts  
- mountinfo signature diffs  

This real-time watcher pinpointed when the guest switched its root or bound host paths (e.g., `/:/host-rootfs`) into the container.

### 2.2 Dropbear Process Mapping  
Three Dropbear instances (PIDs 1721, 12629, 20409) were discovered via `ps -A | grep -Ei 'dropbear|sshd'` [2]. We then:

- Resolved `/proc/net/tcp` inodes → `/proc/*/fd` → owning PIDs  
- Extracted each PID’s `/proc/$PID/exe` path and full `cmdline`  
- Confirmed `startSSHServer.sh` under `/data/user/0/tech.ula/files/support` launched them

### 2.3 Evidence Preservation  
Our `suspect_scan.sh` [3] copied:

- key scripts (`startSSHServer.sh`)  
- `/data/user/0/tech.ula` directory tree  
- snapshots of `/proc/net/tcp`, `/proc/net/unix`, `logcat`, `dmesg`  
- package metadata (`pm list packages`, `dumpsys package tech.ula`)

—all into `/sdcard/suspect_evidence_<TS>` for offline analysis.

---

## 3. Containment & Remediation  

### 3.1 Immediate Containment  
- **Network**: Airplane mode (cut exfiltration).  
- **Process**: `kill -15 <dropbear PIDs>` → `kill -9` fallback.  
- **Proot Master**: `kill -9 <proot PID>` (session‐unmount ejection).  
- **Auto-launch**: `mv startSSHServer.sh startSSHServer.sh.disabled`

### 3.2 Long-Term Cleanup  
- **Uninstall**: `adb shell pm uninstall --user 0 tech.ula` (UserLAnd package)  
- **Rotate** any credentials potentially logged via `.profile` or `script` tools  
- **Audit** with `find /data -type f -mtime -7` for residual implants

---

## 4. Publishing & Collaboration  

### 4.1 PGP Key Publication  
We scaffolded a Git repo containing only the ASCII-armored public key (`public_key.asc`) and documentation (`README.md`, `push.sh`) for secure sharing [4].

```bash
# Quick push sequence
git init
git add public_key.asc README.md push.sh
git commit -m "Add PGP public key"
gh repo create pgp-public-keys --public --source=. --push
```

### 4.2 Open Forensic Workflow  
All scripts (`proot_inspector_fixed.sh`, `android_investigate_net_dropbear_fixed.sh`, `suspect_scan.sh`) were committed to a GitHub repo with version tags—enabling community reuse and auditability.

---

## 5. Agentic Automation Blueprint  

### 5.1 Modular Design  
- **Capabilities as Plugins**: `scan`, `hash`, `delete`, `report` each in its own module with semantic versioning (e.g., `scan-v1.0.0+android10`) [5].  
- **Dynamic Reload**: `inotify` on `/opt/fsagent/modules` triggers safe `dlopen()` swaps.

### 5.2 Hybrid Event/Poll Loop  
- **Fanotify + Polling** for reliable change detection on Android kernels [6].  
- Ensures reflexive adaptation to new mounts or file updates.

### 5.3 Transactional Quarantine & Rollback  
- **Atomic rename** into `/quarantine`  
- **GPG-signed run.log** entries per action:  
  ```
  TIMESTAMP SIGNATURE OP=DELETE PATH=/foo/bar.txt
  ```  
- **Rollback script**: parse `run.log` and restore moved files.

### 5.4 Supervisory Orchestration  
A DAG of sub-agents communicates via ZeroMQ:

```
[SCAN] → [DEDUPE] → [PRIORITIZE] → [DELETE/QUARANTINE] → [REPORT]
```

Strict seccomp-bpf filters confine each agent’s syscall surface for least-privilege hardening.

---

## 6. Lessons Learned & Best Practices  

1. **Evidence-First**: Always snapshot before kill; preserve `/proc/*`, scripts, logs.  
2. **Portable Shebangs**: Use `#!/usr/bin/env sh` and robust `getopts` to avoid brittle scripts.  
3. **Bind-Mount Caution**: Never expose host root to guest without vetted isolation (reference /reference#proot).  
4. **Auditability**: GPG-sign all destructive ops; version and checksum every script.  
5. **Community & CI**: Store scripts in Git; employ automated tests (lint, syntax) to guarantee reproducibility.

---

## 7. Conclusion  
Our chat-driven investigation exemplifies a **distributed intelligence** workflow: human insight + multiple AI copilots + open-source tools. We contained a covert SSH backdoor, preserved proof, and distilled a **generalizable agentic architecture**—from forensic inception to autonomous remediation—paving the way for resilient, self-healing file-system agents on heterogeneous platforms.

---

## References  
1. proot_inspector_fixed.sh — engineering watch script for proot [see file in repo]  
2. `/proc/net` inode-to-PID mapping technique [see suspect_scan.sh section G]  
3. suspect_scan.sh — read-only forensic evidence collector  
4. PGP key publication workflow [README.md & push.sh]  
5. Semantic versioning and plugin architecture [mapping table in agentic_filesystem_traversal_deep_dive.md]  
6. Fanotify vs. inotify reliability on Android kernels [see /reference#linux-event-api]  
7. Android SELinux and permission model [see /reference#android-security]  
8. ZeroMQ patterns for multi-agent orchestration [see /reference#zeromq]  
9. Seccomp-bpf filtering best practices [see /reference#seccomp]  
10. Linux atomic rename and journaling patterns [see /reference#fs-atomicity]  
```  