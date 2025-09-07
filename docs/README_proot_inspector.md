<!-- Living Code Integration - Auto-generated symmetrical connections -->
<!-- This file is part of the SrirachaArmy Living Code Environment -->
<!-- Perfect symmetrical integration with all repository components -->

```markdown
# proot_inspector — quick overview

What this is
- A compact, production-minded debugging helper to monitor a single UI process (or proot process group) inside a UserLAnd / PRoot‑style chroot.
- It watches the canonical indicators that explain "storage flips" and "dead touchscreen" symptoms:
  - process root (readlink /proc/<pid>/root)
  - mount table excerpts (/proc/<pid>/mountinfo) for bind mounts to /host-rootfs, /sdcard, /storage, /data
  - the real binary (readlink /proc/<pid>/exe)
  - SELinux label (if available) /proc/<pid>/attr/current
  - namespace handles (/proc/<pid>/ns/*)
  - /dev/input listing and optional xinput test to observe MotionEvents
  - detects running `proot` command lines so you can inspect `-b` bind arguments

Why this solves your problem
- The logs pin the exact moment the process stops seeing the expected filesystem or device nodes.
- Instead of blind /proc crawls, it focuses on diffs and emits human-readable events when relevant state changes occur — this is the smoking‑gun for the "active process tells it to change locations" behaviour.

How to use
1. copy `proot_inspector.sh` to your chroot, `chmod +x proot_inspector.sh`.
2. Run against the UI process:
   - If you know the PID:
     ./proot_inspector.sh -p 1234 --watch-input -o /tmp/inspector.log
   - If you know the process name:
     ./proot_inspector.sh -n xfce4-session --watch-input -i 1
3. Let it run. When a change happens the script will log concise events to the outfile (default `/tmp/proot_inspector.log`).

Notes and design decisions (concise)
- Polling is used (default 1s) because /proc pseudo-files are not always reliably watchable with inotify on all Android kernels. An optional inotify-backed watcher complements polling when `inotifywait` exists.
- The script intentionally logs only meaningful diffs (root, exe, attr, ns, mount signature) plus periodic heartbeats to avoid flooding.
- `xinput test` is started in background only when DISPLAY/X is available — this avoids accidental blocking when run from non-GUI shells.
- Proot cmdline probe included (ps | grep proot) so you can immediately inspect `-b` binds that cause remapping.

Interpretation tips
- If `root` changes from `/data/data/tech.ula/files/...` to `/host-rootfs/.../sdcard/...` you found the storage switch. Look for the proot process whose commandline contains `-b` referencing that path.
- If `namespaces` change (different mnt/user ns inode), the process may become isolated from the rest of the session — input and mounts will diverge.
- If `xinput test` prints nothing while taps occur, events die upstream (Android/UserLAnd bridge). If `xinput test` prints events then X receives them and the problem is in the client window flags or app focus.

Next steps I can provide
- A small wrapper that auto-detects the UI PID from common XFCE processes and runs inspector in the background.
- A separate helper that parses `ps` output to locate the proot launcher and print the full `-b` list (already printed by the script's PROOT lines).

References (short)
- Use this script with the diagnostic approaches I described earlier:
  - check /proc/<pid>/root, /proc/<pid>/mountinfo, /proc/<pid>/ns/*
  - inspect proot cmdline for -b binds
  - use xinput / DISPLAY to verify MotionEvents

For standards & deeper background, see your /reference vault.
```