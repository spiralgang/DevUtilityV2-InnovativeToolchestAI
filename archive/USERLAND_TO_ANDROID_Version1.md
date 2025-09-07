<!-- Living Code Integration - Auto-generated symmetrical connections -->
<!-- This file is part of the SrirachaArmy Living Code Environment -->
<!-- Perfect symmetrical integration with all repository components -->

```markdown
# UserLAnd → Android app storage (concise guide)

Context
- Android 10 (you): Scoped storage prevents ordinary apps from writing directly to Android/data/* without user consent (SAF).
- No PC available. Partial/rooted but most host folders are protected. We use network bridging (SFTP/HTTP) and the Storage Access Framework (SAF) from a file manager.

Recommended high-level flow (non-root)
1. In UserLAnd, run prepare_transfer.sh on the directory you want to move.
2. Use a SAF-capable Android file manager (Solid Explorer, CX File Explorer, Total Commander with Android 11 plugin).
3. Connect to UserLAnd with SFTP (host 127.0.0.1 port 8022) OR download via HTTP served by prepare_transfer.sh.
4. In the file manager, copy the file(s) and when choosing destination, use the Document picker (grant access) to select:
   /storage/emulated/0/Android/data/tech.ula/files/storage
   The file manager will use SAF to write into that folder.

Solid Explorer (preferred) UI steps
- Install Solid Explorer (or CX File Explorer).
- Add a new SFTP connection:
  - Host: 127.0.0.1
  - Port: try 8022 (or 22)
  - Username: your UserLAnd username
  - Password: the linux password
- Browse to the files, long-press to select.
- Tap Copy.
- Navigate to Local → three-dots → "Open Document" or "Open as Document" (this triggers SAF).
- Choose "Android/data" then tech.ula → files → storage and Grant access.
- Paste. Solid Explorer will copy via SAF into the protected folder.

If SFTP fails → HTTP fallback
- Start the HTTP server printed by prepare_transfer.sh:
  cd /tmp && python3 -m http.server 8000 --bind 127.0.0.1 &
- Open Chrome or Solid Explorer's browser: http://127.0.0.1:8000/your-archive.tar.gz
- Download to Downloads.
- In Solid Explorer: Select the downloaded file → Copy → Local → Open Document → pick Android/data/tech.ula/files/storage → Paste.

Root path (only if host Android root is available)
- If you have true host root (not just root inside chroot), you can su in Android shell and:
  cp -a /path/in/userland/* /storage/emulated/0/Android/data/tech.ula/files/storage/
- Be careful: messing with permissions or SELinux contexts can break the app. Prefer SAF when possible.

Symlink notes
- Emulated external storage (sdcard FUSE) does not always honor POSIX symlinks for apps. Copy files where possible.
- If you must symlink, only rely on it if both reader/writer live in same environment and you have host root.

Troubleshooting
- Permission denied when pasting → re-open the Document picker and ensure you granted access to the Android/data/tech.ula folder.
- SFTP connection refused → ensure sshd running in UserLAnd and use correct port (UserLAnd commonly proxies 8022).
- Browser can't reach HTTP server → ensure server bind used 127.0.0.1 and you're browsing inside the same device (loopback).

Security
- Do not expose sshd to external network. Bind to 127.0.0.1 only.
- Revoke folder access from file manager when done (Solid Explorer settings).
```