```markdown
# Hidden Symlink Entryways and the “Blind `cd /`” Doorway in proot Chroots

Authors  
- spiralgang (lead investigator)  
- 4mini (co-author, deep Linux/Android forensic analyst)  

Date: 2025-09-02  

---

## Overview

When you run a Linux “guest” userland under **proot** on Android, you see a seemingly isolated filesystem root (`/`), your “fakeroot.” But lurking amidst usual directories (`/bin`, `/etc`, `/home…`) are hidden **symlink passageways** that bridge into the host’s real root. With a single blind command—`cd /`—and the right `ls -a -d` incantation, you can step *outside* the proot jail into:

1. The **chroot main-tree** (guest’s pseudo-root)  
2. A mounted **host-rootfs** (actual Android `/`)  
3. Anything else you’ve bind-mounted  

Detecting and exploiting these relies on two key cues:  
- Your shell prompt changes from `[user@localhost]~#` (your home/fakeroot) to `[user@localhost]/#` (now at guest’s real-visible slash).  
- New hidden entries appear when you list `ls -a -d /.*`—they’re the bind-mounted host symlinks.

---

## 1. The Anatomy of a proot “Fakeroot”

### 1.1 proot’s Bind-Mount Emulation

- proot intercepts syscalls (`open()`, `stat()`, `chdir()`) via LD_PRELOAD  
- It **bind-mounts** host paths into the guest namespace, typically via a mountinfo overlay:
  ```
  -b /:/host-rootfs  
  -b /data/.../support/ld.so.preload:/etc/ld.so.preload  
  -b /storage/emulated/0/...:/storage/internal  
  ```
- Inside the guest, these mounts show up as **hidden symlinks** or special directories that are only visible with `ls -a`.

### 1.2 Identifying the Fake vs. Real Slash

A normal chroot (`chroot /some/jail /bin/bash`) gives you a new `/`—you can’t escape. With proot, however, the guest *sees* both:

1. **Fake-root tree**  
   - Standard dirs (`/bin`, `/lib`, `/usr`, `/home`)  
   - Owned by the guest image  
2. **Host-rootfs bind**  
   - Appears as `/host-rootfs` or even `/` itself, depending on your `-b` flags  
   - Contains Android system partitions (`/system`, `/vendor`, `/proc`, `/sys`)

Your **PS1** prompt clue shifts:  
```bash
# In home (fakeroot)
[user@localhost]~#  

# After `cd /` (seeing host binds)
[user@localhost]/#  
```

That slash-prompt is your “blind doorway.”

---

## 2. The “Blind `cd /`” Doorway

### 2.1 Why It’s “Blind”

- If you `cd /` in your profile or via a shell alias, you *expect* to land in one canonical root.  
- In proot, *without* telling you, `/` resolves to a merged mount namespace:  
  - Guest’s root overlay  
  - Host’s real root under a symlink or direct bind  
- Only `ls -a -d /.*` (or enabling hidden entries) reveals the extra entries.

### 2.2 Demonstration with `ls -a -d`

```bash
# Before: in /home/user
[user@localhost]~# pwd
/home/user

# Ordinary ls shows guest dirs
[user@localhost]~# ls /
bin  etc  home  lib  usr  

# Hidden bind symlink appears when you ask for all entries
[user@localhost]~# ls -a -d /*
.  ..  bin  etc  home  lib  usr  host-rootfs

# Stepping through the doorway
[user@localhost]~# cd host-rootfs
[user@localhost]/# ls
system  vendor  data  storage  proc  sys  ...
```

Now you’re roaming the host’s Android OS 10 filesystem—kernel modules in `/system/lib/modules`, Bluetooth firmware in `/vendor/etc/bluetooth`, even your NFC stack under `/system/lib64/hw`.

---

## 3. Profiles, Prompt Strings & Fake-Root Behavior

### 3.1 PS1 Doesn’t Reset Like a True `su -`

- **`su -`** spawns a login shell with a fresh environment (`HOME=/root`, `PS1=[root@localhost]~#`).  
- **Inside proot**, `cd /` does *not* reinitialize your environment:  
  - `HOME` stays as the guest’s homedir  
  - `PS1` simply reflects the new working directory, not a login  
- That’s why you see `/` in the prompt but still retain guest variables and PATH.

### 3.2 Chroot vs proot’s Fakeroot

| Feature                 | True `chroot`    | proot Fakeroot             |
|-------------------------|------------------|----------------------------|
| Namespace Isolation     | Full mount/chroot| Intercept & bind-mount     |
| Environment Reset       | Full login shell | No, `cd /` only changes CWD|
| Host filesystem access  | None             | Via hidden bind entries    |
| Prompt clue             | Always jailed    | `[user@localhost]/#` hints |

---

## 4. “Walking Through C Library Layers” 

By iteratively `cd`-ing one directory at a time, you can traverse:

1. Guest binaries & libs (`/lib`, `/usr/lib`)  
2. Guest `/etc` (config overlays)  
3. `ld.so.preload` injected host paths  
4. Actual host `/system`, `/vendor`, `/data`, `/storage`  

This layered C-library interception means you can:

- Inspect **Android’s zygote** under `/system/bin/app_process64`  
- Dump **SELinux policies** in `/vendor/etc/selinux`  
- Exfiltrate **Knox TEE blobs** from `/persist/`

…all from a single “blind” `cd /` that first reveals a symlink.

---

## 5. Defenses & Hardening (Preventing the Escape)

> _This section is for completeness; audit your proot flags._

1. **Avoid `-b /:/host-rootfs`** unless strictly needed.  
2. **Use namespaces** instead of sys-call interceptors: real `unshare` + `pivot_root`.  
3. **Drop `ld.so.preload` overlays**—they can be hijacked to hook syscalls.  
4. **Lock down shell prompts** (`PS1`) to detect unexpected slash changes.  
5. **Audit your `/proc/mountinfo`** for unauthorized bind mounts.

---

## References

1. “proot” bind-mount semantics — /reference#proot  
2. Linux mount namespaces — /reference#namespaces  
3. Android SELinux & vendor partitions — /reference#android-security  
4. chroot vs. proot comparison — proot FAQ & chroot(1) man page  
5. PS1 and prompt variables — Bash Reference Manual, Section “Prompting”  
```