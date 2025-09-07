```markdown
Title: From Proot to Dropbear: A Practical Forensics and Remediation Discography for Linux on Android (Android 10 / aarch64)

Authors
- spiralgang (lead investigator, operator)
- Assistant (technical co-investigator, scripts & analysis)
- Windows GPT-5 / Microsoft Copilot / community helpers (tools & inspiration)

Status
- Draft — peer-reviewed by the incident owner (spiralgang). This document synthesizes the investigation, forensics, containment, remediation, and durable recommendations arising from the multi-day diagnostic described in the chat session.

Short congratulatory note
We won. Teamwork, persistence, and systematic diagnostics uncovered the root cause and containment path: congratulations to the human-plus-AI team — spiralgang, Windows-side copilots, and the assistant. This document memorializes the technical story, the scripts used, and the professional recommendations for operators running Linux userlands (proot) on Android 10 aarch64 devices.

Abstract
You experienced unexplained network activity, unexpected SSH server instances (Dropbear), and suspicious filesystem bindings while running a UserLAnd/VM-like environment on Android 10 (aarch64). This paper documents evidence collection, root‑cause analysis, quick containment, and long‑term remediation and hardening. The material is practical, reproducible, and emphasizes forensic preservation, minimal-impact containment, and secure cleanup for operators on Android devices hosting proot-based userlands.

1. Background and context
- Environment: Android 10 (aarch64), device with a userland app (UserLAnd / tech.ula) that runs proot to provide a Linux distribution in user space.
- Primary concerns raised:
  - Many network task artifacts in /proc for a proot child process.
  - Presence of Dropbear processes (small SSH daemon) and unexpected SSH listening/established sockets.
  - Strange references to freedesktop.org across system files and a resolved IP contact (131.252.210.176).
  - A crash log mentioning VMOS/system auth ambiguity.
  - A .profile or startup logs that may be recording shell input (password exposure risk).

2. Investigation goals and principles
- Contain first: limit network exfiltration by cutting network connectivity or killing offending processes.
- Preserve second: collect logs, scripts, and binaries for later forensic review; do not overwrite or delete evidence.
- Identify third: map sockets → PIDs → binaries → packages; inspect provenance (system package vs app-bundled).
- Remediate last: remove or disable persistent attackers/services, rotate secrets, harden future deployments.

3. Key artifacts and recurring clues
- Proot master command line showed explicit bind mounts (-b) including:
  - -b /:/host-rootfs  (host root exposed inside guest)
  - -b /data ... -b /storage/emulated/0/Android/data/tech.ula/files/storage:/storage/internal
  - Multiple support/ld.so.preload and /etc/profile.d overlays
- Three concurrent dropbear processes (example PIDs reported in session: 1721, 12629, 20409).
- No immediate ESTABLISHED socket output from ss (ss not always present on Android); fallback /proc scans gave no definitive remote owner mapping for freedesktop IP at time of capture.
- Strong evidence that proot (started by the UserLAnd app package tech.ula) launched an SSH server (startSSHServer.sh) inside the guest. The guest's bind of host filesystem paths into the container made the situation riskier: guest could read host files and, depending on permissions and overlays, operate on host-visible data.

4. Technical analysis / what happened (mechanics)
- Proot: proot is a user-space utility that emulates chroot and namespace-like behavior by binding paths and intercepting syscalls. When run with -b binds and given support scripts, it can expose host paths to the guest. That explains why /proc/<proot-pid>/... lists many net-related files: the guest has its own /proc namespaces and exposed network interfaces.
- Bind mount implications: -b /:/host-rootfs allows the guest to read (and sometimes write) host root content via an alternate root. This breaks conventional host/guest isolation and elevates the risk if the guest is untrusted.
- Dropbear: Dropbear is a compact SSH server often bundled with userland packages. If launched from guest scripts, it creates listening sockets and can be controlled by guest-level actors. If keys/logs are stored on shared/exposed storage, they might persist across reboots and be retrievable.
- freedesktop.org references: package metadata, systemd, AppArmor, and many desktop libraries reference freedesktop.org in documentation and package fields. These are benign in themselves; seeing them listed during a grep of the filesystem is expected on a Debian-like image, and not evidence of a remote "hijack" unless paired with active sockets / packet captures showing outbound connections.
- VMOS/system auth log: third-party VM apps or mis-signed packages may be flagged by the system when they request privileged APIs. The crash text you saw is consistent with a VM-like app being recognized but not officially authorized as a system service — an informational/authorization mismatch, not necessarily exploitative by itself.

5. Evidence collection methodology (commands & scripts used)
We used conservative, reproducible scripts to gather evidence with minimal side effects. Key tools/one-liners used:
- Resolve and list sockets:
  - getent ahosts freedesktop.org || dig +short freedesktop.org
  - ss -tunap || netstat -tunap || fallback parse /proc/net/tcp
- Map sockets → processes:
  - lsof -i (if present) or grep socket:inode entries in /proc/*/fd cross-referenced against /proc/net/tcp to find the owning PID
- Verify proot command line and mounts:
  - tr '\0' ' ' < /proc/<proot-pid>/cmdline
  - sed -n '1,200p' /proc/<proot-pid>/mountinfo
- Inspect start scripts and support files:
  - sed -n '1,240p' /data/user/0/tech.ula/files/support/startSSHServer.sh
- Search for logging/persistence:
  - grep -R --line-number -I "exec .*>|tee|script|logger" /data /home /sdcard
- A conservative suspect_scan.sh was written to copy evidence (start script, directory listings, /proc snapshots, logcat snippets) into /sdcard for safe retrieval without deleting anything.

6. Findings (synthesis)
- The UserLAnd app (tech.ula) is present and launched proot with numerous binds including -b /:/host-rootfs. The proot process (example PID 20367 in session) started an included support script startSSHServer.sh that spawned Dropbear processes.
- Dropbear instances were active inside the guest and were visible in host ps listings; they were likely launched from the UserLAnd support scripts.
- freedesktop.org strings are common in Debian/desktop metadata; a DNS resolution returned 131.252.210.176 but there was no conclusive ESTABLISHED socket linking a local PID to that IP at the time of capture. Additional packet capture (tcpdump) would definitively prove outbound connections and SNI/TLS hosts.
- A .profile or logging line capable of capturing stdin/output was suspected; search commands were used to find redirections and logging utilities (script, tee, logger) — if present, any typed passwords must be considered compromised.
- Killing the proot master or child processes closed your shell because the shell session was running inside that proot-mounted guest. That is expected behavior — when you kill the proot backing process, guest namespace and mounts disappear.

7. Immediate containment and remediation (ordered, minimal-impact)
- If possible, disconnect network (Airplane mode) to prevent ongoing exfiltration.
- Preserve evidence: do not delete or overwrite /data/user/0/tech.ula and related support directories until you have an extracted copy.
- Pull the evidence package from /sdcard (suspect_scan outputs) to a trusted workstation for analysis using adb pull.
- Stop the SSH server and proot master cleanly before deleting:
  - Identify PIDs: ps -A | egrep -i 'dropbear|proot|startSSHServer'
  - Graceful stop: kill -15 <pid> ; fallback kill -9 if necessary
  - Prevent relaunch: mv startSSHServer.sh startSSHServer.sh.disabled (do not delete yet)
- Rotate credentials typed during the suspect session (passwords, SSH keys, API tokens). Treat them as compromised if .profile logging or script logging was active.
- Uninstall the app (UserLAnd / VMOS) if you do not trust it, after evidence collection:
  - adb shell pm uninstall --user 0 tech.ula  (or uninstall via Settings)
- For persistent compromise or unknown implants in /data, consider a factory reset and restore from verified backups.

8. Hardening & long‑term recommendations
- Never bind-mount host root (/:) into a guest. Avoid -b /:/host-rootfs or similar binds that expose host filesystem to guest processes.
- Treat proot/userland apps as untrusted. Run unprivileged and avoid storing keys or secrets in shared storage (sdcard or exposed /data paths).
- Disable auto-launch scripts that spawn network-accessible services inside the guest. Keep services (ssh, web) confined to debugging sessions and ephemeral keys.
- Use Android work profile / guest user isolation for any development/test apps. Prefer remote Linux on a trusted host for critical tasks.
- Log hygiene: avoid adding exec > /sdcard/log redirections in shell startup files. If logging is required, use audited, append-only, and access-controlled logging on a different host.
- Monitor network with host-based firewall apps and use per-app network permissions (Android 10+ gives good app-level controls).
- Use verified packages from trusted repos; prefer distribution packages over app-bundled binaries where possible.

9. Forensics & reproducibility
- The suspect_scan.sh and proot_inspector scripts are included in the appendix (short descriptions here). Collect /sdcard/suspect_scan_TIMESTAMP.log and /sdcard/suspect_evidence_TIMESTAMP for offline analysis.
- If packet capture is needed, capture on device or upstream router:
  - tcpdump -i any -s 0 -w /sdcard/capture.pcap host 131.252.210.176  (requires root)
  - Analyze with Wireshark on a trusted workstation.

10. Ethical/procedural notes
- Public sharing: PGP public key publication is safe; do not publish private keys or passphrases. If you will publish logs, redact secrets and private keys first.
- If you suspect a third-party compromise, consider contacting the app developer (UserLAnd / VMOS) with your sanitized forensic data.
- If you find evidence of unauthorized access to accounts (banking, social), notify affected providers and rotate credentials immediately from a trusted device.

Appendix A — Scripts used (descriptions)
- proot_inspector_fixed.sh
  - Purpose: watch a specific proot PID for changes to /proc/<pid>/root, /proc/<pid>/exe, and mountinfo; log diffs and heartbeats.
  - Safe usage: run with -p PID to watch a proot master; logs to /tmp/proot_inspector.log by default.
- android_investigate_net_dropbear_fixed.sh
  - Purpose: portable evidence collector for UserLAnd/Termux-like environments; enumerates dropbear processes, finds binaries, resolves freedesktop.org, and dumps ps/ss snapshots.
  - Safe usage: run as shell script (sh ./android_investigate_net_dropbear_fixed.sh) and collect produced log (prints path).
- suspect_scan.sh
  - Purpose: conservative forensic collector that copies startSSHServer.sh and a snapshot of /data/user/0/tech.ula into /sdcard for later analysis via adb pull.
  - Safe usage: run once; it does not delete files, only copies.

Appendix B — Minimal commands (cheat sheet)
- Identify proot/dropbear:
  - ps -A | egrep -i 'proot|dropbear|startSSH|userland|tech.ula'
- Show proot cmdline:
  - tr '\0' ' ' < /proc/<proot-pid>/cmdline
- Show mountinfo excerpt:
  - sed -n '1,120p' /proc/<proot-pid>/mountinfo
- Map socket inode → PID (fallback):
  - awk 'NR>1 {print $2,$10}' /proc/net/tcp | while read r inode; do grep -H "socket:\\[$inode\\]" /proc/*/fd 2>/dev/null || true; done
- Copy evidence to PC:
  - adb pull /sdcard/suspect_scan_<TS>.log

References
- proot (project): https://proot.me/ and https://github.com/termux/proot
- UserLAnd project (Android userlands): https://github.com/CypherpunkArmory/userland
- Dropbear SSH: https://matt.ucc.asn.au/dropbear/dropbear.html
- freedesktop.org — standards and projects: https://www.freedesktop.org/
- /proc and mountinfo documentation: "proc(5)" and Linux kernel proc files — https://man7.org/linux/man-pages/man5/proc.5.html ; https://www.kernel.org/doc/Documentation/filesystems/proc.txt
- Android package manager and adb docs: https://developer.android.com/studio/command-line/adb and https://developer.android.com/reference/android/content/pm/PackageManager
- AppArmor and systemd documentation (contextual references to freedesktop usage): https://gitlab.com/apparmor/apparmor/-/wikis/home ; https://www.freedesktop.org/wiki/Software/systemd/
- Forensics and secure practices: NIST SP 800-61 (Computer Security Incident Handling Guide) — https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-61r2.pdf

What I did for you and what's next
I synthesized the entire investigative narrative from your session logs, produced a procedural forensics and remediation paper, distilled the crucial commands and scripts you executed or should execute, and codified recommendations to prevent recurrence. If you'd like, I will:
- produce a Git-ready repo containing this document and the vetted scripts (safe, with private material scrubbed), or
- export this document to a downloadable ZIP, or
- create a short “incident report” styled for disclosure to vendors (UserLAnd/VMOS) or internal triage teams.

If you want me to create the repo or push files to GitHub, tell me the repo name and whether it should be public or private and I will produce the exact commands (or create it for you if you want me to run the push).
```
```